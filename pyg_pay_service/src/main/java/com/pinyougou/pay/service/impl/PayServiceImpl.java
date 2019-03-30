package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PayServiceImpl implements PayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String mch_id;  //商家号

    @Value("${notifyurl}")
    private String notifyurl;  //通知地址

    @Value("${partnerkey}")
    private String partnerkey;   //签名

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;
    @Override
    public TbPayLog searchPayLogFromRedis(String key) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(key);
    }

    /**
     * 修改支付状态
     * @param out_trade_no
     * @param transaction_id 微信支付返回的业务代码
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //根据支付单号获得支付单对象
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        //根据支付单号获取支付单对象
        payLog.setPayTime(new Date());  //付款时间
        payLog.setTradeState("1");   //0未付款  1已付款
        payLog.setTransactionId(transaction_id);  //微信返回的业务代码
        payLogMapper.updateByPrimaryKey(payLog);  //更新数据库
        //更改完支付单的还要修改order的信息、
        String[] orderIds = payLog.getOrderList().split(",");
        for (String orderId : orderIds) {
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            order.setPaymentTime(new Date());  //订单的支付时间
            order.setStatus("2");  //'状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            orderMapper.updateByPrimaryKey(order);
        }
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }

    //查询订单状态
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //发hhtp请求，拿到http工具类
            HttpClientUtil client = new HttpClientUtil("https://api.mch.weixin.qq.com/pay/orderquery");
            //封装参数
            Map responseMap = new HashMap<>();
            responseMap.put("appid", appid);
            responseMap.put("mch_id", mch_id);
            responseMap.put("out_trade_no", out_trade_no);
            responseMap.put("nonce_str", WXPayUtil.generateUUID());
            //签名通过httpclien
            //将map转成xml格式，并带上签名
            String signedXml = WXPayUtil.generateSignedXml(responseMap, partnerkey);
            System.out.println("发送的内容" + signedXml);
            //准备发送的参数(需要string类型的xml）
            client.setXmlParam(signedXml);
            //需要设置https的协议
            client.setHttps(true);
            //发送post请求
            client.post();
            //接受string类型的xml格式返回值
            String content = client.getContent();
            System.out.println("返回的" + content);
            //通过工具类将String的xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        try {
            //发hhtp请求，拿到http工具类
            HttpClientUtil client = new HttpClientUtil("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //封装参数
            Map responseMap = new HashMap<>();
            responseMap.put("appid", appid);
            responseMap.put("mch_id", mch_id);
            responseMap.put("nonce_str", WXPayUtil.generateUUID());
            //签名通过httpclien
            responseMap.put("body", "测试商品");
            responseMap.put("out_trade_no", out_trade_no);
            responseMap.put("total_fee", total_fee);
            responseMap.put("spbill_create_ip", "1270.0.1");//终端ip，获取设备的当前地址
            //通知地址
            responseMap.put("notify_url", notifyurl);
            responseMap.put("trade_type", "NATIVE");
            //将map转成xml格式，并带上签名
            String signedXml = WXPayUtil.generateSignedXml(responseMap, partnerkey);
            System.out.println("发送的内容" + signedXml);
            //准备发送的参数(需要string类型的xml）
            client.setXmlParam(signedXml);
            //需要设置https的协议
            client.setHttps(true);
            //发送post请求
            client.post();
            //接受string类型的xml格式返回值
            String content = client.getContent();
            System.out.println("返回的" + content);
            //通过工具类将String的xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(content);
            //提取出想要的数据
            //map提取想要的属性code_url,支付地址，还需要支付单号和支付总金额
            Map reponseMap = new HashMap<>();
            reponseMap.put("code_url", map.get("code_url")); //支付地址
            reponseMap.put("out_trade_no", out_trade_no);  //支付单号
            reponseMap.put("total_fee", total_fee);  //支付总金额
            return reponseMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

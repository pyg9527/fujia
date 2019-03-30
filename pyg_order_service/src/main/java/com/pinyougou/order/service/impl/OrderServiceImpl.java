package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.IdWorker;
import entity.Cart;
import org.apache.commons.collections.OrderedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TbOrderMapper orderedMap;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;

    @Override
    public void add(TbOrder order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        double payMoney = 0.0;
        List orderIds = new ArrayList<>();
        for (Cart cart : cartList) {
            double totalMoney = 0.0;
            TbOrder tbOrder = new TbOrder();
            tbOrder.setUserId(order.getUserId()); //保存谁下的订单
            tbOrder.setSourceType("2");  //'订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
            tbOrder.setReceiverMobile(order.getReceiverMobile()); //收货人电话
            tbOrder.setReceiver(order.getReceiver());    //收货人
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());  //收货地址
            tbOrder.setCreateTime(new Date());
            tbOrder.setPaymentType(order.getPaymentType());  //支付方式
            tbOrder.setUpdateTime(new Date());
            //雪花算法设置orderid
            long orderId = idWorker.nextId();
            tbOrder.setOrderId(orderId);
            //把订单id放入集合中
            orderIds.add(orderId);
            //保存订单明细
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);
                totalMoney += orderItem.getTotalFee().doubleValue();
                orderItemMapper.insert(orderItem);

            }
            payMoney += totalMoney; //累加每张订单的总金额，就是支付单总金额
            tbOrder.setPayment(new BigDecimal(totalMoney));
            tbOrder.setSellerId(cart.getSellerId());
            tbOrder.setStatus("1");  // '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            orderedMap.insert(tbOrder);
        }
        //清空购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

        //生成订单对象，存入数据库
        TbPayLog payLog = new TbPayLog();
        payLog.setOutTradeNo(idWorker.nextId() + ""); //支付单号采用雪花算法
        payLog.setCreateTime(new Date());
        payLog.setTotalFee((long) (payMoney * 100));  //这里是分
        payLog.setUserId(order.getUserId());  //哪个用户的支付单
        payLog.setTradeState("0");       //0未支付  1已支付

        payLog.setOrderList(orderIds.toString().replace("[","").replace("]","").replaceAll(" ",""));
        payLog.setPayType("1");     //1，微信支付  2.货到付款

        payLogMapper.insert(payLog);  //将未支付的支付单保存数据库
        //将支付信息存入redis，也可以用userID差
        redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
    }
}

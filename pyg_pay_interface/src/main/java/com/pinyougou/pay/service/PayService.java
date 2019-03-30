package com.pinyougou.pay.service;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

public interface PayService {
    Map createNative(String out_trade_no, String total_fee);

    //查询订单状态
    Map queryPayStatus(String out_trade_no);

    //从redis中查询订单
    TbPayLog searchPayLogFromRedis(String key);

    //支付成功后修改状态
    public void updateOrderStatus(String out_trade_no, String transaction_id);
}

package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    @RequestMapping("/createNative")
    public Map createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = payService.searchPayLogFromRedis(userId);
        if (tbPayLog != null) {
            System.out.println("支付金额"+tbPayLog.getTotalFee().toString());
            Map map = payService.createNative(tbPayLog.getOutTradeNo(), "1");
            return map;
        }
        return null;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        int timer = 1;
        while (true) {
            Map map = payService.queryPayStatus(out_trade_no);
            if (map == null) {
                return new Result(false, "支付失败");
            }
            if ("SUCCESS".equals(map.get("return_code"))) {
                //成功后修改状态
                payService.updateOrderStatus(out_trade_no,map.get("transaction_id").toString());
                return new Result(true, "支付成功");
            }
            //同时超过30秒重新刷新二维码
            if (timer > 6) {
                return new Result(true, "timeout");
            }
            timer++;
            //需要5s地查询支付状态，
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

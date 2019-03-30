package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component

public class CreateOrder implements Runnable {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper goodsMapper;
    @Autowired
    private TbSeckillOrderMapper orderMapper;

    @Override
    public void run() {
        //从redis中获取
        TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundListOps("seckill_order").rightPop();
        //保存订单
        orderMapper.insert(order);
        //修改数量
        TbSeckillGoods goods = goodsMapper.selectByPrimaryKey(order.getSeckillId());
        goods.setStockCount(goods.getStockCount()-1);
        goodsMapper.updateByPrimaryKey(goods);
    }
}

package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private CreateOrder createOrder;

    /**
     * 保存秒杀订单
     *
     * @param goodsId
     * @param userId
     */
    @Override
    public void saveSeckillOrder(Long goodsId, String userId) {
        //解决用户重复购买的问题
        Boolean user_id = redisTemplate.boundSetOps("seckill_log_" + goodsId).isMember(userId);
        if (user_id) {
            throw new RuntimeException("用户已经购买过该商品");
        }
        //解决超卖问题
        Object o = redisTemplate.boundListOps("seckill_goods_queue_" +goodsId).rightPop();
        if (o == null) {
            throw new RuntimeException("活动结束");
        }

        TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(goodsId);

        if (goods == null || goods.getStockCount() < 1) {
            throw new RuntimeException("活动结束");
        }
        //将库存数量减1,走线程池
//        goods.setStockCount(goods.getStockCount()-1);
        //保存到redis中
        redisTemplate.boundHashOps("seckill_goods").put(goodsId, goods);
        //将userId存入redis中的set中
        redisTemplate.boundSetOps("seckill_log_" + goodsId).add(userId);
        //若库存数量为0后，清除缓存中的该商品
        if (goods.getStockCount() < 1) {
            redisTemplate.boundHashOps("seckill_goods").delete(goodsId);
        }
        TbSeckillOrder order = new TbSeckillOrder();
        order.setSellerId(goods.getSellerId()); //商家id
        order.setCreateTime(new Date());  //创建秒杀单时间
        order.setMoney(goods.getCostPrice());  //秒杀价格
        order.setSeckillId(goods.getId());
        order.setStatus("0");    //0未付款  1已付款
        order.setUserId(userId);   //哪个用户下的单
        order.setId(idWorker.nextId());   //采用雪花算法
//        seckillOrderMapper.insert(order);
        redisTemplate.boundListOps("seckill_order").leftPush(order);
        executor.execute(createOrder);
    }
}

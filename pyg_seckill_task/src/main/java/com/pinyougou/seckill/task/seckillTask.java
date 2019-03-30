package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class seckillTask    {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/5 * * * * ?")
    public void seckillGoodsToReids(){
        //查询出符合条件的秒杀商品， 开始时间<=当前时间 结束时间大于当前时间，剩余库存大于0，状态为1
        TbSeckillGoodsExample example=new TbSeckillGoodsExample();
        example.createCriteria().andStartTimeLessThanOrEqualTo(new Date()).
                andEndTimeGreaterThanOrEqualTo(new Date()).
                andStatusEqualTo("1").andStockCountGreaterThan(0);
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        for (TbSeckillGoods tbSeckillGood : seckillGoodsList) {
            redisTemplate.boundHashOps("seckill_goods").put(tbSeckillGood.getId(),tbSeckillGood);
            //循环剩余的数量，将其存入redis的list中
            for(int i=0; i<tbSeckillGood.getStockCount();i++){
                redisTemplate.boundListOps("seckill_goods_queue_" + tbSeckillGood.getId()).leftPush(tbSeckillGood.getId());
            }
        }
        System.out.println("存入的个数是"+seckillGoodsList.size());
    }
}

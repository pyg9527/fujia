package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ItemPageServiceImpl implements ItemPageService {
    @Autowired
    private FreeMarkerConfigurer configurer;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public void createHtml(Long goodsId) {
        //获取配置信息
        Configuration configuration = configurer.getConfiguration();
        try {
            //获得模板
            Template template = configuration.getTemplate("item.ftl");
            FileWriter writer=new FileWriter(new File("D:\\00myproject\\day12\\item\\"+goodsId+".html"));
            //根据商品id获得商品信息
            HashMap  map =new HashMap<>();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            TbItemExample example=new TbItemExample();
            example.createCriteria().andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc"); //以默认1 倒序排列，
            List<TbItem> items = itemMapper.selectByExample(example);
            String category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("goods",goods);
            map.put("goodsDesc",goodsDesc);
            map.put("items",items);
            map.put("category1Id",category1);
            map.put("category2Id",category2);
            map.put("category3Id",category3);
            //准备导出数据
            //执行
            template.process(map,writer);
            //关闭
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteHtml(Long[] goodsIds) {
        for (Long goodsId : goodsIds) {
            new File("D:\\00myproject\\day12\\item\\"+goodsId+".html").delete();
            System.out.println("删除了"+goodsId+"页面");
        }

    }
}

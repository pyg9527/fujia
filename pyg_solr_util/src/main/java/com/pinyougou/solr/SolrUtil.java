package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    //一次性将tb_item导入solr库
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper tbItemMapper;

    public  void importItemToSolr(){
        //可以通过example写条件
        List<TbItem> items = tbItemMapper.selectByExample(null);

        for (TbItem item : items) {
            System.out.println(item.getTitle()+" " +item.getBrand()+" "+item.getPrice());
            Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);//从数据库中提取规格json字符串转换为map
            item.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(items);

        solrTemplate.commit();
    }

    public static void main(String[] args) {
        //写*号导入了solr的spring配置还导入了dao的spring配置，因为要用mapper查数据库
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil=  (SolrUtil) context.getBean("solrUtil");//需要注解@compent,并且配置文件要自动导报
        solrUtil.importItemToSolr();

    }

}



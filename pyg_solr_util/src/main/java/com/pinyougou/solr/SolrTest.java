package com.pinyougou.solr;

import com.pinyougou.pojo.TbItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
//加载配置文件
@ContextConfiguration("classpath:spring/applicationContext*.xml")

public class SolrTest {
    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void Query() {
        //有参构造，里面是条件表达式
        SimpleQuery query = new SimpleQuery("*:*");
        //增加查询条件
        Criteria criteria = new Criteria("item_title");
        criteria = criteria.contains("三星");
        query.addCriteria(criteria);

        //查询返回的是tbitem的集合
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        List<TbItem> list = items.getContent();

        for (TbItem tbItem : list) {
            System.out.println(tbItem.getTitle());
        }
    }

    @Test
    public void deleQuery() {
        SimpleQuery simpleQuery = new SimpleQuery("*:*");
        solrTemplate.delete(simpleQuery);
        solrTemplate.commit();
    }
}

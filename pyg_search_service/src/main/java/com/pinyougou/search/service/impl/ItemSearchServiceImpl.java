package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 将商品引入solr库中.注意这个id是商品id，并不是itemId
     *
     * @param ids
     */
    @Override
    public void importItemToSolr(Long[] ids) {
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdIn(Arrays.asList(ids));
        List<TbItem> items = itemMapper.selectByExample(example);
        for (TbItem item : items) {
            System.out.println("添加到了solr库" + item.getTitle());
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Override
    public void removeItemFromSolr(Long[] ids) {
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdIn(Arrays.asList(ids));
        List<TbItem> items = itemMapper.selectByExample(example);
        for (TbItem item : items) {
            solrTemplate.deleteById(item.getId().toString());
            System.out.println("从solr库中移除" + item.getTitle());
        }
        solrTemplate.commit();
    }

    @Override
    public Map search(Map searchMap) {

        String keywords = (String) searchMap.get("keywords");//获得传进来参数的值
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions options = new HighlightOptions();///创建高亮属性
        options.addField("item_title"); //设置高亮显示的域
        options.setSimplePrefix("<em style='color:red'>");
        options.setSimplePostfix("\"</em>\"");
        query.setHighlightOptions(options);//将属性加入到查询条件中
        //查询关键字 检索
        if (keywords != null && keywords.length() > 0) {
            Criteria criteria = new Criteria("item_keywords");
            criteria = criteria.contains(keywords);
            query.addCriteria(criteria);
        }else {
            Criteria criteria=new Criteria().expression("*:*");
            query.addCriteria(criteria);
        }
        //查询分类检索
        String category = (String) searchMap.get("category");//获取分类
        if (category != null && category.length() > 0) {
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_category");
            criteria = criteria.contains(category);
            simpleFilterQuery.addCriteria(criteria);
            query.addFilterQuery(simpleFilterQuery);
        }
        //对品牌进行检索
        String brand = (String) searchMap.get("brand");//获取分类
        if (brand != null && brand.length() > 0) {
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_brand");
            criteria = criteria.contains(brand);
            simpleFilterQuery.addCriteria(criteria);
            query.addFilterQuery(simpleFilterQuery);
        }
        //对规格进行检索
        Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
        if (spec != null) {
            Set<String> keys = spec.keySet();
            for (String key : keys) {
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                Criteria criteria = new Criteria("item_spec_" + key);
                criteria = criteria.is(spec.get(key));
                simpleFilterQuery.addCriteria(criteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }
        //对价格进行检索
        String price = (String) searchMap.get("price");
        if (price != null && price.length() > 0) {
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_price");
            String[] split = price.split("-");
            criteria = criteria.greaterThanEqual(split[0]);
            simpleFilterQuery.addCriteria(criteria);
            query.addFilterQuery(simpleFilterQuery);
            if (!"*".equals(split[1])) {//说明不是3000以上
                SimpleFilterQuery simpleFilterQuery2 = new SimpleFilterQuery();
                Criteria criteria2 = new Criteria("item_price");

                criteria2 = criteria2.greaterThanEqual(split[0]);
                simpleFilterQuery2.addCriteria(criteria2);
                query.addFilterQuery(simpleFilterQuery2);
            }
        }
        //进行排序
        String sortStr = (String) searchMap.get("sort");
        if("ASC".equals(sortStr)){
            Sort sort=new Sort(Sort.Direction.ASC,"item_price");
            query.addSort(sort);
        }else {
            Sort sort=new Sort(Sort.Direction.DESC,"item_price");
            query.addSort(sort);
        }

        //获取pageSize，pageNo
        Integer pageNum = (Integer) searchMap.get("pageNum");
        Integer pageSize = (Integer) searchMap.get("pageSize");

        query.setRows(pageSize);//一页记录数
        query.setOffset((pageNum-1)*pageSize); //一页起始值

        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<TbItem> content = tbItems.getContent();//获取list集合
        //遍历集合，插入修改后的title
        for (TbItem tbItem : content) {
            List<HighlightEntry.Highlight> highlights = tbItems.getHighlights(tbItem);//直接跳过Highlightes,拿到highlights这个集合
            if (highlights.size() > 0) {
                List<String> snipplets = highlights.get(0).getSnipplets();//若Highlightes不为0，取到snipplets集合
                if (snipplets.size() > 0) {
                    String highTitle = snipplets.get(0);
                    tbItem.setTitle(highTitle);
                }
            }
        }
        //创建一个map，存入查询出来的list

        Map map = new HashMap();

        map.put("content", content);
        //存入总记录数
        map.put("total" ,tbItems.getTotalElements());

        return map;
    }

}

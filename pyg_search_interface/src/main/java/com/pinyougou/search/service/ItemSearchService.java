package com.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {


    //参数是map的原因是因为可能用品牌查，可能用关键字查询，所以封装一个map，keyvalue的形势
    //返回值是map的原因是 若要分页的话，返回一个rows，total，所以可以用map，类似pageResult
    public Map search(Map searchMap);
    //上架商品引入solr库
    public void importItemToSolr(Long[] ids);

    //下架商品删除solr库
    public  void removeItemFromSolr(Long[] ids);
}

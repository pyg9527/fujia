package com.pinyougou.content.service.impl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;

//查询相同商品分类的集合(不走缓存)
/*	@Override
	public List<TbContent> findByCategoryId(Long id) {
		TbContentExample example=new TbContentExample() ;
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(id);
		criteria.andStatusEqualTo("1");//只显示开启状态的
		example.setOrderByClause("sort_order desc");//排序
		List<TbContent> tbContents = contentMapper.selectByExample(example);
		return tbContents;
	}*/

//走缓存查询，没有的话先走mysql，有的话走redis
	@Override
	public List<TbContent> findByCategoryId(Long id) {
		List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(id);
		if (contents == null) {
			TbContentExample example=new TbContentExample() ;
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(id);
			criteria.andStatusEqualTo("1");//只显示开启状态的
			example.setOrderByClause("sort_order desc");//排序
			contents= contentMapper.selectByExample(example);
			redisTemplate.boundHashOps("content").put(id,contents);
			System.out.println("从mysql中取");
		}else{
			System.out.println("从redis中获取");
		}

		return contents;
	}


	//修改状态
	@Override
	public void updateStatus(Long[] ids, String status) {
		HashSet<Long> set = new HashSet<>();

		for (Long id : ids) {
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			Long categoryId = tbContent.getCategoryId();
			set.add(categoryId);
			tbContent.setStatus(status);
			contentMapper.updateByPrimaryKey(tbContent);
			System.out.println("从mysql查询");
		}
		for (Long aLong : set) {
			redisTemplate.boundHashOps("content").delete(aLong);
			System.out.println("清除缓存了");
		}

	}
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//查询出更新前的该对象数据
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		//判断时候改变了广告分支
		if(tbContent.getCategoryId()!=content.getCategoryId()){
			//如果不等于就清除之前的广告分类
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
		}
		contentMapper.updateByPrimaryKey(content);
		//不管有没有修改当前的分类，都要清除现在的缓存,
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}


	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);		
	}

	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}



}

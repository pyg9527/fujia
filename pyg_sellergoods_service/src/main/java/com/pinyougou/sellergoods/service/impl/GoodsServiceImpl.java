package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;


	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0"); //新增商品默认为草稿状态
		goodsMapper.insert(tbGoods);
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(tbGoods.getId());
		goodsDescMapper.insert(goodsDesc);
		if("1".equals(tbGoods.getIsEnableSpec())){
            List<TbItem> itemList = goods.getItemList();
            //价格，库存，是否启用，是否默认已经传参了
            //设置如下：标题，图片，分类id，分类名，创建时间，修改时间，商品编号，商家编号，商家昵称，品牌名
            for (TbItem tbItem : itemList) {
                //其中标题是商品名称+spec中的所有内容
                String title = tbGoods.getGoodsName();
                Map<String,String> map = JSON.parseObject(tbItem.getSpec(), Map.class);//item里面的spec都是map对象，key，value {"机身内存":"16G","网络":"联通3G"}
                //遍历map，拿到对应的规格中的属性
                for (String set : map.keySet()) {
                    title+=" "+map.get(set);
                }
                tbItem.setTitle(title);
                //将图片数组中的第一个地址存入。图片的地址集合存在goods_desc中，
                //格式：[{"color":"白色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVnGbYuAO6AHAAjlKdWCzvg253.jpg"},{"color":"蓝色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVnKX4yAbCC0AAFa4hmtWek406.jpg"}]
                List<Map> lists = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
                if(lists.size()>1){
                    tbItem.setImage(lists.get(0).get("url").toString());
                }

                //插入三级分类id。可以从goods中直接取
                tbItem.setCategoryid(tbGoods.getCategory3Id());
                //创建时间，修改时间，商品编号
                tbItem.setCreateTime(new Date());
                tbItem.setUpdateTime(new Date());
                tbItem.setGoodsId(tbGoods.getId());

                //商家编号 在controller层走add方式，获得当前用户的登陆信息，赋给goods。
                tbItem.setSellerId(tbGoods.getSellerId());
                //商家编号.通过brandService查询
                tbItem.setBrand(brandMapper.selectByPrimaryKey(tbGoods.getBrandId()).getName());
                //设置分类名称
                tbItem.setCategory(itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName());
                //设置seller的name
                tbItem.setSeller(sellerMapper.selectByPrimaryKey(tbGoods.getSellerId()).getName());
                //注意前端用ng-ture-value设置是否默认和是否启用
                itemMapper.insert(tbItem);
            }
        }else{
            //没启用规格
            TbItem item = new TbItem();
            item.setTitle(tbGoods.getGoodsName()); // 商品名称直接设置到sku对象上


            //商品卖点
            item.setSellPoint(tbGoods.getCaption());
            //保存商品图片集合中的图片地址，取第一张
            List<Map> images = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
            if(images.size() > 0){
                //取第一个对象的图片地址
                item.setImage(images.get(0).get("url").toString());
            }

            //需要保存三级分类id
            item.setCategoryid(tbGoods.getCategory3Id());
            item.setCreateTime(new Date());
            item.setUpdateTime(new Date());
            item.setGoodsId(tbGoods.getId());

            //设置商家的id
            item.setSellerId(tbGoods.getSellerId()); //注意确认已经从springSecurity中获取到sellerid封装到goods中
            //设置分类名称
            item.setCategory( itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName());

            //设置品牌名称
            item.setBrand(brandMapper.selectByPrimaryKey(tbGoods.getBrandId()).getName());

            //设置seller的name
            item.setSeller( sellerMapper.selectByPrimaryKey(tbGoods.getSellerId()) .getName());


            //将spec，status，isDefault，price，num设置默认值（之前是页面传入）
            item.setPrice(tbGoods.getPrice());
            item.setStatus("1");
            item.setIsDefault("1");
            item.setNum(99999);

            itemMapper.insert(item);
        }


	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}



	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		TbGoods tbGoods = goods.getGoods();
		goodsMapper.updateByPrimaryKey(tbGoods);
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDescMapper.updateByPrimaryKey(goodsDesc);
		List<TbItem> itemList = goods.getItemList();
		for (TbItem tbItem : itemList) {
			itemMapper.updateByPrimaryKey(tbItem);
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods=new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		TbItemExample example=new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goods.setItemList(tbItems);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
//			goodsMapper.deleteByPrimaryKey(id);
			//改为逻辑删除
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}


	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		//只查询删除的
			criteria.andIsDeleteIsNull();
		if(goods!=null){
			//显示当前商家的商品
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
		}

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}


}

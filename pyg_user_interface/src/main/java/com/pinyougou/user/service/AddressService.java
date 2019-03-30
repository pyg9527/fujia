package com.pinyougou.user.service;

import java.util.List;

import com.pinyougou.pojo.TbAddress;

import entity.PageResult;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface AddressService {
    //获得地址集合
	List<TbAddress> findListByUserId(String userId);
}

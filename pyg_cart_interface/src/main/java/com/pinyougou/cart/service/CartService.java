package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List;

public interface CartService {

    /**
     *  将itemId和数量，传入该方法，再传入购物车列表，返回添加好商品的购物车列表
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addTbItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中根据key获取 购物车
     * @param key
     * @return
     */
    public List<Cart> getCartByUserId(String key);

    public void setCartListByUserId(String key,List<Cart> cartList);
    //合并购物车的方法
    public List<Cart> mergeCartList(List<Cart> list1,List<Cart> list2);
    //删除购物车的方法
    public  void deleCartListByUserId(String key);
}

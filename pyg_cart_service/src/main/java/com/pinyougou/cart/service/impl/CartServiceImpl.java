package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void deleCartListByUserId(String key) {
        redisTemplate.boundHashOps("cartList").delete(key);
    }

    /**
     * 合并购物车的方法
     *
     * @param list1
     * @param list2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> list1, List<Cart> list2) {
        for (Cart cart : list1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                list2 = addTbItemToCartList(list2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return list2;
    }

    /**
     * 只要redis中的东西是序列化的，可以直接存入reids。
     *
     * @param key
     * @return
     */
    @Override
    public List<Cart> getCartByUserId(String key) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(key);
        //存在当前没有过购物车，null值
        if (cartList == null) {
            cartList = new ArrayList<Cart>();
        }
        for (Cart cart : cartList) {
            System.out.println(cart.getSellerName());
        }
        return cartList;
    }

    /**
     * 添加购物车到redis中
     *
     * @param key
     * @param cartList
     */
    @Override
    public void setCartListByUserId(String key, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(key, cartList);

    }

    /**
     * 循环一遍购物车，有匹配的就输出这个购物车，没有返回null
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    public Cart findCartFromCartList(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据订单属性的itemID与要加入购物车的商品进行对比
     *
     * @param orderItems
     * @param itemId
     * @return
     */
    public TbOrderItem findOrderItemFromList(List<TbOrderItem> orderItems, Long itemId) {
        for (TbOrderItem orderItem : orderItems) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 讲重复代码提取出来，作用是item对象转为orderItem对象
     *
     * @param item
     * @param num
     * @return
     */
    public TbOrderItem setValue(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(num * item.getPrice().doubleValue())); //总金额 数量*单价
        orderItem.setNum(num); //数量
        orderItem.setTitle(item.getTitle());
        orderItem.setPicPath(item.getImage()); //唯一的一张图片
        orderItem.setSellerId(item.getSellerId()); //商家id
        orderItem.setItemId(item.getId());  //这个id很重要是明细对应的sku的id
        orderItem.setGoodsId(item.getGoodsId());  //商品id
        return orderItem;
    }

    /**
     * 商品加入购物车，
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addTbItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据itemId获得商品的属性
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        Cart cart = findCartFromCartList(cartList, item.getSellerId());
        //当购物车之前不存的话
        if (cart == null) {
            //下面是讲商品的属性转为订单的属性
            TbOrderItem orderItem = setValue(item, num);
            //讲订单属性存入集合封装到cart对象中
            ArrayList<TbOrderItem> orderItems = new ArrayList<>();
            orderItems.add(orderItem);
            //讲cart赋值
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            cart.setOrderItemList(orderItems);
            //将这个购物车对象传到
            cartList.add(cart);
        } else { //当购物车存在的话
            //判断当前添加的商品是否已经存在
            TbOrderItem orderItem = findOrderItemFromList(cart.getOrderItemList(), itemId);
            if (orderItem == null) { //说明商品并不存在
                orderItem = setValue(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {//商品已经存在，只需要增加num，总金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
                //可能存在商品数量减为0的情况，需要我们讲它移除。
                if (orderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //若orderItemList为空，
                if (cart.getOrderItemList().size() < 1) {
                    cartList.remove(cart);
                }
                //list.get取得是地址值
            }
        }
        return cartList;
    }

}

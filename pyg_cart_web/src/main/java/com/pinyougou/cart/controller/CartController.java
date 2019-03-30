package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.utils.CookieUtil;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")

public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    //获得cookie中的uuid
    public String getUuid() {
        String uuid = CookieUtil.getCookieValue(request, "uuid", "utf-8");
        //一开始不存在uuid时，进行赋值
        if (uuid == null || uuid.equals("")) {
            uuid = UUID.randomUUID().toString();
            //设置存活时间，讲uuid放入cookie中
            CookieUtil.setCookie(request, response, "uuid", uuid, 48 * 60 * 60, "utf-8");
        }
        return uuid;
    }

    /**
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addItemToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addItemToCartList(Long itemId, Integer num) {
        try {
            String uuid = getUuid();
            String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser".equals(loginName)) {//说明登陆了
                uuid = loginName;
            }
            List<Cart> cartList = cartService.getCartByUserId(uuid);
            List<Cart> carts = cartService.addTbItemToCartList(cartList, itemId, num);
            //存入redis
            cartService.setCartListByUserId(uuid, carts);
            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }
    }

    @RequestMapping("/findCartListFromRedis")
    public List<Cart> findCartListFromRedis() {
        String uuid = getUuid();
        //        得到未登录的购物车
        List<Cart> cartList = cartService.getCartByUserId(uuid);
        //如果没有配置，直走单点登陆的话，这边查不到，会报错
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        //        System.out.println("===="+loginName);
        if (!"anonymousUser".equals(loginName)) {//说明登陆了
            //            uuid=loginName;
            // 登录后合并购车,先获取登录后的购物车,先判断未登陆的购物车有没有
            List<Cart> cartLogin = cartService.getCartByUserId(loginName);
            if(cartList.size()>0){
                cartList= cartService.mergeCartList(cartList, cartLogin);
               //存入reids中
               cartService.setCartListByUserId(loginName,cartList);
               //清空未登录的购物车
                cartService.deleCartListByUserId(uuid);
            }else{
                cartList=cartLogin;
            }

        }
        return cartList;
    }
}

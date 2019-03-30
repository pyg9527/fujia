package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.utils.PhoneFormatCheckUtils;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference(timeout = 10000)
    private UserService userService;
    @RequestMapping("/getName")
    public Map getName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("name",name);
        return map;
    }
    @RequestMapping("/add")
    public Result add(@RequestBody TbUser tbUser,String code){
        boolean logo = userService.checkCode(tbUser.getPhone(), code);
        if(!logo){
            return new Result(false,"验证码不正确");
        }
        try {
            userService.add(tbUser);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    @RequestMapping("/sendSms")
    public Result sendSms(String phone) {
        boolean legal = PhoneFormatCheckUtils.isChinaPhoneLegal(phone);
        if(!legal){
            return new Result(false, "手机格式不正确");
        }
        try {
            userService.sendSms(phone);
            return new Result(true, "发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "发送失败");
        }
    }
}

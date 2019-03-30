package com.itbsoft.sms.test;

import com.pinyougou.utils.HttpClientUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilsTest {
    public static void main(String[] args) throws IOException {

        // 需要安全框架认证
        HttpClientUtil httpClient1 = new HttpClientUtil("http://localhost:9101/login");
        Map map= new HashMap();
        map.put("username","admin");
        map.put("password","123456");
        httpClient1.setParameter(map);
        httpClient1.post();
        //快速访问findAll
        HttpClientUtil httpClient=new HttpClientUtil("http://localhost:9101/brand/findAll.do");
        try {
            httpClient.get();
            String content = httpClient.getContent();
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

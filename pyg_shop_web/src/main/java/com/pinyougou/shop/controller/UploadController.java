package com.pinyougou.shop.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    private String url="http://192.168.25.133/";
    @RequestMapping("/upload")
    public Result upload(MultipartFile file1) {
        try {
            //获取文件的全名
            String filename = file1.getOriginalFilename();
            //获取文件的后缀名
            String substring = filename.substring(filename.lastIndexOf(".") + 1);//indexof得到的是当前的索引
            //创建 FastDFSClient的类
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            // 因为是从文件夹选择，所以不知道文件路径，选择字节。
            String fildId = fastDFSClient.uploadFile(file1.getBytes(), substring);
           return new Result(true,url+fildId);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"图片上传失败");
        }
    }
}

package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojo.TbSellerExample;
import com.pinyougou.sellergoods.service.SellerService;
import com.pinyougou.utils.DownloadUtil;
import entity.Result;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/poi")
public class PoiController {

   @Reference
   private SellerService sellerService;
   /* @Autowired
    private HttpServletResponse response;*/

    @RequestMapping("/export")
    public Result poiToExcle() {
        //查出符合条件的供应商列表(未审核)
        List<TbSeller> sellerList = sellerService.findSellerNoStatus();
        OutputStream os =null;
        try {
            //1.创建一个工作簿
            Workbook wb = new HSSFWorkbook();//作用于excel2003 版本
            //2.创建工作表
            Sheet sheet = wb.createSheet("未审核供应商");
            //3.创建行对象。行的索引是行号-1
            Row row = sheet.createRow(0);
            String[] headers={"商家ID", "公司名称","店铺名称","联系人姓名","公司电话"};
            for (int i=0;i<headers.length;i++) {
                row.createCell(i).setCellValue(headers[i]);
            }
            //4.创建单元格
            //Cell cell = row.createCell(0);
            if(sellerList!=null&&sellerList.size()>0){
                for(int i=1;i<=sellerList.size();i++){
                    row=sheet.createRow(i);
                    TbSeller seller = sellerList.get(i - 1);
                    // 设置单元格值
                    row.createCell(0).setCellValue(seller.getSellerId());
                    row.createCell(1).setCellValue(seller.getName());
                    row.createCell(2).setCellValue(seller.getNickName());
                    row.createCell(3).setCellValue(seller.getLinkmanName());
                    row.createCell(4).setCellValue(seller.getTelephone());
                }
            }
         /*   //6.设置单元格的样式
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 11);//字体大小
            font.setFontName("宋体");//字体名称
            cellStyle.setFont(font);//设置单元格样式的字体
            cell.setCellStyle(cellStyle);//将单元格样式作用于单元格*/
            //7.保存，关闭流
            String filename="商家";
            os = new FileOutputStream("D:\\01品优购\\poi\\"+filename+".xls");
            wb.write(os);
            return new Result(true,"导出成功");
            //8.下载（在项目中采用工具类的方式）
          /*  DownloadUtil downloadUtil = new DownloadUtil();
            downloadUtil.download(os,response,filename);*/

        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false,"导出失败");
        }finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                return new Result(false,"导出失败");
            }
        }
    }
}

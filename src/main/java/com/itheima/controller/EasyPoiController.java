package com.itheima.controller;

import com.deepoove.poi.XWPFTemplate;
import com.itheima.entity.AddressBook;
import com.itheima.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * easyPoi导出word和excel
 */
@RestController
@RequestMapping("/easyPoi")
@Api(tags = "word模板相关接口")
public class EasyPoiController {
    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/wordExport/{id}")
    @ApiOperation(value = "填充word模板接口")
    public void wordExport(HttpServletResponse response ,@PathVariable("id") Long id) throws IOException {
        //输入address_book表的id：1653747447360077825
        AddressBook byId = addressBookService.getById(id);

        //设置填充后的word名称
        String fileName = URLEncoder.encode("外卖地址表填充.docx", "UTF-8");

        //word模板的路径
        ClassPathResource resource = new ClassPathResource("word/外卖地址表.docx");

        //date类型转str
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        //localDateTime类型转str
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        String createTime = formatter.format( byId.getCreateTime());
        String updateTime = formatter.format( byId.getUpdateTime());

        //填充数据
        XWPFTemplate template = null;
        if (byId != null) {
            template = XWPFTemplate.compile(resource.getFile()).
                    render(new HashMap<String, Object>() {{
                        put("date", date);
                        put("userId", byId.getUserId());
                        put("consignee", byId.getConsignee());
                        put("sex", byId.getSex());
                        put("phone", byId.getPhone());
                        put("detail", byId.getDetail());
                        put("isDefault", byId.getIsDefault());
                        put("createUser", byId.getCreateUser());
                        put("updateUser", byId.getUpdateUser());
                        put("createTime", createTime);
                        put("updateTime", updateTime);
                    }});
        }

        //返回文件
        response.reset();
        response.setContentType("application/x-msdownload");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        ServletOutputStream servletOS = response.getOutputStream(); //输出流
        template.write(ostream);//数据写入到输出流
        servletOS.write(ostream.toByteArray());
        servletOS.flush();
        servletOS.close();
        ostream.close();
    }



}

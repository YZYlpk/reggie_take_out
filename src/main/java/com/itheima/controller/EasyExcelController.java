package com.itheima.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.aliyuncs.http.HttpResponse;
import com.itheima.common.R;
import com.itheima.dto.FillDataDto;
import com.itheima.entity.Category;
import com.itheima.entity.Employee;
import com.itheima.service.CategoryService;
import com.itheima.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * easyExcel导出excel
 */
@RestController
@RequestMapping("/easyExcel")
@Api(tags = "Excel模板相关接口")
public class EasyExcelController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/outputExcelLocal")
    @ApiOperation(value = "注解写Excel接口(直接保存到本地)")
    public void OutputExcel() {
        /** 保存到本地的写Excel接口 **/
        String filename = "D:\\workspace\\reggie_take_out\\src\\main\\resources\\excel\\菜品分类管理表.xlsx";
        //导入的数据
        List<Category> list = categoryService.list();

        // 创建ExcelWriter对象
        ExcelWriter excelWriter = EasyExcel.write(filename, Category.class).build();
        // 创建Sheet对象
        WriteSheet writeSheet = EasyExcel.writerSheet("用户信息").build();
        // 向Excel中写入数据
        excelWriter.write(list, writeSheet);
        // 关闭流
        excelWriter.finish();

    }

    @GetMapping("/outputExcel")
    @ApiOperation(value = "注解写Excel接口")
    public void OutputExcel(HttpServletResponse response){
        /** 通过网页响应下载excel **/
        try {
            // 设置下载信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("菜品分类管理表.xlsx", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName);

            //导入的数据
            List<Category> list = categoryService.list();
            // 调用方法，进行写操作，这里使用输出流
            EasyExcel.write(response.getOutputStream(), Category.class).sheet("菜品分类").doWrite(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @GetMapping("/fillExcel")
    @ApiOperation(value = "填充Excel模板接口")
    public void fillExcel(HttpServletResponse response) throws IOException {
        try {
            ServletOutputStream out = response.getOutputStream();
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("UTF-8");

            //文件名字
            String fileName = URLEncoder.encode("员工信息表填充.xlsx", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);

            //文件模板输入流
            InputStream inputStream = new ClassPathResource("excel/员工信息表.xlsx").getInputStream();

            //准备填充的数据
            List<Employee> employees = employeeService.list();

            ExcelWriter writer = EasyExcel.write(out).withTemplate(inputStream).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            //填充列表开启自动换行,自动换行表示每次写入一条list数据是都会重新生成一行空行,此选项默认是关闭的,需要提前设置为true
            FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();
            //填充数据
            writer.fill(employees, fillConfig, sheet);

            //获取日期
            FillDataDto fillDataDto = new FillDataDto();
            Date date = new Date();
            //只获取年月日
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String format = simpleDateFormat.format(date);
            fillDataDto.setDate(format);

            //统计数量
            int count = employees.size();
            fillDataDto.setCount(count);
            writer.fill(fillDataDto, sheet);

            //填充完成
            writer.finish();
            out.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}

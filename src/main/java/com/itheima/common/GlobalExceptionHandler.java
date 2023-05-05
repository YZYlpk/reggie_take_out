package com.itheima.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Service.class}) //annotations = {RestController.class} 表示只要加了这个注解的类，都会被统一用这个异常处理器处理
@ResponseBody //ResponseBody 最后封装成JSON数据返回
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        /**
         * 统一输出格式：某个字段（唯一索引）重复导致报错
         */
        if(ex.getMessage().contains("Duplicate entry")){//判断：如果报错信息包含 ”Duplicate entry“ 这个字段
            String[] spilt=ex.getMessage().split(" ");//参数为空格：根据空格划分为每个元素，然后存进数组
            //例如：Duplicate entry 'LiPeiKai' for key 'employee.idx_username' --> 我们要拿‘LiPeiKai’这个元素，索引为2
            String msg = spilt[2]+"已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 拿到自定义异常处理
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}

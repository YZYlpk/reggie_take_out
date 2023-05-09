package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 李培锴
 *
 */

@Slf4j
@SpringBootApplication
@ServletComponentScan //ServletComponentScan 可以扫描到 @WebFilter(过滤器) 这些注解
@MapperScan("com.itheima.mapper") //mapper接口位置，在这里加就不需要在每个mapper接口类上面加@mapper注解
@EnableTransactionManagement //开启事务
@EnableCaching //开启spring cache 缓存注解功能
public class ReggieApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ReggieApplication.class);
        log.info("项目启动成功...");
    }
}

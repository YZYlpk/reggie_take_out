package com.itheima;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;


/**
 * Unit test for simple App.
 */
@SpringBootTest
public class ReggieApplicationTest {
    @Test
    public void test1() {
        String fileName = "abc.jpg";
        String substring = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(substring);

    }

    /**
     * 使用jedis操作Redis
     */
    @Test
    public void testRedis(){
        //先启动redis服务
        //1.获取连接
        Jedis jedis=new Jedis("localhost",6379);

        //2.执行具体操作
        jedis.set("username","xiaoming");
        String username = jedis.get("username");
        System.out.println(username);

        jedis.hset("myhash","one","1");
        String hget = jedis.hget("myhash", "one");
        System.out.println(hget);

        //3.关闭连接
        jedis.close();
    }
}
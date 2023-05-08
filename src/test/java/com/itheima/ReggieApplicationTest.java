package com.itheima;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 测试
 */
@SpringBootTest
public class ReggieApplicationTest {
    //yml配置好redis配置后，注入封装类
    @Autowired
    private RedisTemplate redisTemplate;

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

    /**
     * redis命令操作string类型数据
     */
    @Test
    public void  testString(){
        redisTemplate.opsForValue().set("city","beijing");

        String city = (String) redisTemplate.opsForValue().get("city");
        System.out.println(city);

        //设置存活时间十秒
        redisTemplate.opsForValue().set("key1","value1",10l, TimeUnit.SECONDS);

        //判断是否存在key，否则创建
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("city1", "name1");
    }

    /**
     * redis命令操作hash
     */
    @Test
    public void  testHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();

        //存值
        hashOperations.put("001","name","xiaoming");
        hashOperations.put("001","age","20");

        //取值
        String name = (String) hashOperations.get("001", "name");
        System.out.println(name);

        //获得hash结构所有字段key
        Set keys = hashOperations.keys("001");
        for(Object key:keys){
            System.out.println(key);
        }

        //获得hash结构所有值value
        List values = hashOperations.values("001");
        for (Object value : values) {
            System.out.println(value);
        }
    }

    /**
     * redis命令操作list
     */
    @Test
    public void  testList() {
        ListOperations listOperations = redisTemplate.opsForList();

        //存值(队列)
        System.out.println("存值");
        listOperations.leftPush("mylist","a");
        listOperations.leftPushAll("mylist","b","c","d");

        //取值
        System.out.println("取值");
        List<String> mylist = listOperations.range("mylist", 0, -1);
        for (String o : mylist) {
            System.out.println(o);
        }

        //获取列表的长度
        System.out.println("出队列");
        Long size = listOperations.size("mylist");
        int lsize=size.intValue();//long转int
        for (int i = 0; i < lsize; i++) {
            //rightPop出队列（先进先出），leftRop出栈（先进后出）
            Object element = listOperations.rightPop("mylist");
            System.out.println(element);
        }
    }

    /**
     * redis命令操作Set
     */
    @Test
    public void  testSet() {
        SetOperations setOperations = redisTemplate.opsForSet();

        //存值
        setOperations.add("myset","a","b","c","a");

        //取值
        Set<String> myset = setOperations.members("myset");
        for (String o : myset) {
            System.out.println(o);
        }

        //删除
        setOperations.remove("myset","a","b");
    }

    /**
     * redis命令操作Zset
     */
    @Test
    public void  testZset() {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        //存值(按分数值排序)
        zSetOperations.add("myZset","a",10.0);
        zSetOperations.add("myZset","b",5.0);
        zSetOperations.add("myZset","c",11.0);
        zSetOperations.add("myZset","c",9.0);//两个相同元素，分数以最后一个为标准

        //取值
        Set<String> myset = zSetOperations.range("myZset",0,-1);
        for (String o : myset) {
            System.out.println(o);
        }

        //修改分数
        zSetOperations.incrementScore("myZset","b",20);

        //删除
        zSetOperations.remove("myZset","a","b");

    }

    /**
     * redis通用操作
     */
    @Test
    public void testCommon(){
        //获取redis中所有的key
        Set<String> keys = redisTemplate.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }

        //判断key是否存在
        Boolean myset = redisTemplate.hasKey("myset");
        System.out.println(myset);

        //删除指定key
        redisTemplate.delete("myset");

        //获取指定key对应value的数据类型
        DataType myZset = redisTemplate.type("myZset");
        System.out.println(myZset);


    }
}
package com.itheima;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;



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
}
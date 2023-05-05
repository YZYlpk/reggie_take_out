package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.OrderDetailService;
import com.itheima.service.OrdersService;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.expr.NewArray;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersConotroller {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 订单管理分页查询
     * @param page
     * @param pageSize
     * @param beginTime 起始时间
     * @param endTime   结束时间
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number ,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime){
        Page<Orders> pageInfo=new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(number!=null,Orders::getId,number);
        if (beginTime!=null){//开始时间存在，结束时间一定存在
            queryWrapper.between(Orders::getOrderTime,beginTime,endTime);
        }

        queryWrapper.orderByDesc(Orders::getOrderTime);

        ordersService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 移动端订单管理分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){

        Page<OrdersDto> pageInfoDto = ordersService.userPage(page, pageSize);

        return R.success(pageInfoDto);
    }

    /**
     * 修改订单运送状态
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        Long id = orders.getId();
        //Integer status = map.get("status");
        Orders byId = ordersService.getById(id);
        if(byId.getStatus()== 2||byId.getStatus()==3){
            byId.setStatus(byId.getStatus()+1);
        }
        ordersService.updateById(byId);

        return R.success("订单状态修改成功");
    }



    /**
     * 我们需要将订单内的菜品重新加入购物车，
     *所以在此之前我们需要将购物车清空（业务层实现方法）
     *
     */
    @PostMapping("/again")
    @Transactional
    public R<String> againSubmit(@RequestBody Map<String,Long> map){
        Long id = map.get("id");
        // 制作判断条件
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);

        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空
        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrentId();

        // 整体赋值
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {

            // 以下均为赋值操作
            ShoppingCart shoppingCart = new ShoppingCart();

            BeanUtils.copyProperties(item,shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将携带数据的购物车批量插入购物车表
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("添加购物车成功");
    }
}
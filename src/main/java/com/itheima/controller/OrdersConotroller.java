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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "订单相关接口")
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
    @ApiOperation(value = "用户下单接口")
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
    @ApiOperation(value = "后台订单管理分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "number",value = "订单号",required = false),
            @ApiImplicitParam(name = "beginTime",value = "开始时间",required = false),
            @ApiImplicitParam(name = "endTime",value = "结束时间",required = false)
    })
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
    @ApiOperation(value = "移动端订单管理分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true)
    })
    public R<Page> userPage(int page, int pageSize){

        Page<OrdersDto> pageInfoDto = ordersService.userPage(page, pageSize);

        return R.success(pageInfoDto);
    }

    /**
     * 修改订单运送状态
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改订单状态接口")
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
     * 再来一单
     * 我们需要将订单内的菜品重新加入购物车，
     *所以在此之前我们需要将购物车清空（业务层实现方法）
     */
    @PostMapping("/again")
    @Transactional
    @ApiOperation(value = "再来一单接口")
    public R<String> again(@RequestBody Map<String,Long> map){

        ordersService.again(map);

        return R.success("添加购物车成功");
    }
}

package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.BaseContext;
import com.itheima.common.CustomException;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.*;
import com.itheima.mapper.OrdersMapper;
import com.itheima.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前的购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        if(list==null || list.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//MP自带生成订单号
        String orderIds = String.valueOf(orderId);//订单号转为string型

        //原子操作，保证多线程的安全 用来计算总金额
        AtomicInteger amount=new AtomicInteger(0);

        //计算购物车总金额,并得到订单明细（一种菜品/套餐表示一条订单明细）
        List<OrderDetail> orderDetails=list.stream().map(e->{
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setOrderId(orderId);
            BeanUtils.copyProperties(e,orderDetail);
            //单份金额乘以分数，再转成int型
            amount.addAndGet(e.getAmount().multiply(new BigDecimal(e.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //向订单表插入数据，一条(使用builder)
        Orders finalOrder=Orders.builder().
                //主键（我们设置和订单号一样）
                id(orderId).
                //订单号
                number(orderIds).
                //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
                status(2).
                userId(userId).
                addressBookId(addressBookId).
                orderTime(LocalDateTime.now()).
                checkoutTime(LocalDateTime.now()).
                payMethod(orders.getPayMethod()).
                /** 总金额 **/
                amount(new BigDecimal(amount.get())).
                remark(orders.getRemark()).
                phone(user.getPhone()).
                userName(user.getName()).
                //收货人
                consignee(addressBook.getConsignee()).
                address( (addressBook.getProvinceName() ==null ? "" : addressBook.getProvinceName())
                        +(addressBook.getCityName() ==null ? "" : addressBook.getCityName())
                        +(addressBook.getDistrictName() ==null ? "" : addressBook.getDistrictName())
                        +(addressBook.getDetail() ==null ? "" : addressBook.getDetail())
                ).
                build();

        String s = addressBook.getDetail() == null ? "" : addressBook.getDetail();
        log.info("地址，{}",s);
        this.save(finalOrder);

        //向明细表插入数据，多条
        orderDetailService.saveBatch(orderDetails);

        //下单完成后，清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        Page<Orders> pageInfo=new Page<>(page,pageSize);
        Page<OrdersDto> pageInfoDto=new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行分页
        this.page(pageInfo,queryWrapper);
        //copy，除了records属性
        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map(e -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(e, ordersDto);
            //拿到订单明细的数据
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();

            //条件：一条订单id的所有订单数据
            queryWrapper1.eq(OrderDetail::getOrderId, e.getId());
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);
            ordersDto.setOrderDetails(list);

            //计算该订单的商品总数(计算每条订单明细的数量，然后相加)
            List<Integer> sum = new ArrayList<>();
            list.stream().map(s -> {//将每一条订单明细的数量分别存进sum集合中
                sum.add(s.getNumber());
                return s;
            }).collect(Collectors.toList());

            //将sum集合的所有商品数量相加，求得最终商品总数
            Integer sumNum = sum.stream().reduce(Integer::sum).orElse(0);

            //得到商品总数（两个王老吉表示两个商品数量）
            ordersDto.setSumNum(sumNum);

            return ordersDto;

        }).collect(Collectors.toList());

        pageInfoDto.setRecords(collect);

        return pageInfoDto;
    }
}

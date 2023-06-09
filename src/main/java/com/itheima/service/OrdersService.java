package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.itheima.dto.OrdersDto;

import com.itheima.entity.Orders;

import java.util.Map;

public interface OrdersService extends IService<Orders> {
    public void submit(Orders orders);

    public Page<OrdersDto> userPage(int page, int pageSize);

    void again(Map<String, Long> map);
}

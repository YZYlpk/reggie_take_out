package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import org.springframework.stereotype.Service;


public interface ShoppingCartService extends IService<ShoppingCart> {
    R<String> clean();

}

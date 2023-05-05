package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.entity.Dish;
import org.apache.ibatis.annotations.Param;

public interface DishMapper extends BaseMapper<Dish> {
    void updateStatus(@Param("status") int status, @Param("id") Long l);

}

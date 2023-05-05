package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.entity.SetmealDish;
import org.apache.ibatis.annotations.Param;

public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

    void removeWithDish(@Param("id") Long id);
}

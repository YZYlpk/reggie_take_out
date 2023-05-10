package com.itheima.dto;

import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("套餐dto")
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

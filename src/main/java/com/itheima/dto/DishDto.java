package com.itheima.dto;

import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("菜品dto")
public class DishDto extends Dish {
    //除了以下三个，dto还继承来了Dish实体类的所有属性
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}

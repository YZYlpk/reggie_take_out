package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品，同时保存菜品对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional//涉及到多张表,需要加入事务控制
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish（有对应的字段就保存进去，多余的直接忽略）
        this.save(dishDto);

        //菜品id
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //stream流，对flavors集合里面的某个元素赋值，最后返回修改后的flavors集合
        flavors.stream().map((e)->{
            e.setDishId(dishId);
            return e;
        }).collect(Collectors.toList());

        //保存菜品数据到菜品口味表(对集合处理：批量保存)
        dishFlavorService.saveBatch(flavors);

    }


    //分页
    @Override
    public Page<DishDto> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> dishPage=new Page<>(page,pageSize);
        //因为页面有一个属性是Dish里面没有的，所以得用DishDto
        Page<DishDto> dishDtoPage=new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();

        //添加过滤条件(参数一：如果为空，这句话相当于不执行；如果不为空，则加入后面的条件参数)
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //添加排序条件(根据更新时间来排序)
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询(两种写法)
        //dishService.page(dishPage,queryWrapper);
        this.page(dishPage,queryWrapper);

        //对象拷贝(除了records：用来存放查询出来的数据)
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        List<Dish> records = dishPage.getRecords();

        //list为dishDtoPage里面的records属性对应的值
        List<DishDto> list = records.stream().map((e->{//得到分类id对应的分类名称
            DishDto dishDto=new DishDto();
            //拷贝
            BeanUtils.copyProperties(e,dishDto);
            Long categoryId = e.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            String name1 = category.getName();
            dishDto.setCategoryName(name1);
            return dishDto;
        })).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return dishDtoPage;
    }

    //根据id查询菜品信息和对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    @Transactional//涉及到多张表,需要加入事务控制
    public void updateWithFalvor(DishDto dishDto) {
        //更新dish表基本信息(自动转型)
        this.updateById(dishDto);

        //清除当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //更新dish_falvor表口味信息-insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        //stream流，对flavors集合里面的某个元素赋值，最后返回修改后的flavors集合
        flavors.stream().map((e)->{
            e.setDishId(dishDto.getId());
            return e;
        }).collect(Collectors.toList());

        //保存菜品数据到菜品口味表(对集合处理：批量保存)
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void updateStatus(int status,String ids) {
        /** 目的：将string转成long[],比如："123,222” -> [123,222] **/

        //如果ids是有多个id，那么将它转成string数组
        String[] split =  ids.split(",");

        if (split.length!= 0){//判断是否为空

            //处理string 转成Long :将数组每个string元素转成long型
            List<Long> longs= Arrays.stream(split).map((e) ->
                    Long.parseLong(e.trim())
            ).collect(Collectors.toList());

            for(Long l:longs){
                try {
                    //在数据层处理(更新状态)
                    dishMapper.updateStatus(status,l);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }




    }
}

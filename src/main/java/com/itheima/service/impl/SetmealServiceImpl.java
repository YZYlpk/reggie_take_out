package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealDishMapper;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal表，insert
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(e->{//给setmealDto的SetmealDishes属性里面的套餐id赋值
            e.setSetmealId(setmealDto.getId());
            return e;
        }).collect(Collectors.toList());

        //保存套餐和菜品关联的消息，操作setmeal_dish表，saveBatch表示批量插入
        setmealDishService.saveBatch(setmealDishes);
    }


    public Page<SetmealDto> page(int page, int pageSize, String name){

        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> pageInfoDto=new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        //排序条件
        queryWrapper.orderByAsc(Setmeal::getCategoryId).orderByDesc(Setmeal::getUpdateTime);

        //执行分页
        this.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> collect = records.stream().map(e -> {
            SetmealDto setmealDtoList = new SetmealDto();
            BeanUtils.copyProperties(e, setmealDtoList);
            //查找套餐的分类id，根据id去对应的菜品表里面找到对应的菜品分类名称
            Category categoryServiceById = categoryService.getById(e.getCategoryId());
            if(categoryServiceById!=null){
                String setmealName = categoryServiceById.getName();
                setmealDtoList.setCategoryName(setmealName);
            }

            return setmealDtoList;
        }).collect(Collectors.toList());

        pageInfoDto.setRecords(collect);

        //返回需要展示的数据字段给当前页面
        return pageInfoDto;
    }


    @Override
    public void updateStatus(int status, List<Long> ids) {
        List<Setmeal> setmeals = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setStatus(status);
            setmeal.setId(id);
            setmeals.add(setmeal);
        }
        //更新有值的字段，null值的字段保持不变
        this.updateBatchById(setmeals);

    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除(只有停售的套餐才可以删除)
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //判断表中id是否有在ids中
        queryWrapper.in(Setmeal::getId, ids);
        //判断是否在启售
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("套餐正在售卖中，无法删除");
        }

        // 可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        // 删除关系表中的数据

        //方法一：mybatis数据层处理
        for (Long id:ids){
            setmealDishMapper.removeWithDish(id);
        }

//        //方法二：MP处理
//        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper();
//        //找到关系表中对应的套餐id
//        wrapper.in(SetmealDish::getSetmealId, ids);
//
//        setmealDishService.remove(wrapper);



    }

    @Override
    @Transactional//涉及多张表
    public SetmealDto getByIdWithFlavor(Long id) {
        SetmealDto setmealDto=new SetmealDto();

        //查询id在套餐基本信息表里面的信息
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询id在套餐菜品关联表里面的信息
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());

        //得到符合条件的集合
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }


    @Override
    public void updateSB(SetmealDto setmealDto) {
        //更新套餐基本信息表
        this.updateById(setmealDto);

        //清除当前套餐关联的菜品数据
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //更新套餐菜品关联表信息-insert操作
        List<SetmealDish> list = setmealDto.getSetmealDishes();

        //stream流，对list集合里面的某个元素赋值，最后返回修改后的flavors集合
        List<SetmealDish> collect = list.stream().map((e) -> {
            e.setSetmealId(setmealDto.getId());
            return e;
        }).collect(Collectors.toList());

        //保存菜品数据到菜品口味表(对集合处理：批量保存)
        setmealDishService.saveBatch(collect);

    }
}

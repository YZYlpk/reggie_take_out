package com.itheima.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 菜品管理分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        Page<DishDto> dishDtoPage = dishService.page(page, pageSize, name);

        return R.success(dishDtoPage);
    }


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public  R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFalvor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 批量修改起售停售状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, String ids){
        log.info("状态：{}，id：{}",status,ids);

        dishService.updateStatus(status,ids);

        return R.success("状态更新成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids){
        String[] split = ids.split(","); //将每个id分开

        //每个id还是字符串，转成Long
        List<Long> idList = Arrays.stream(split).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());

        dishService.removeByIds(idList);//执行批量删除（MP自带方法）

        log.info("删除的ids: {}",ids);
        return R.success("删除成功"); //返回成功
    }


//    /**
//     * 根据条件查询对应的菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    // url：http://localhost:8080/dish/list?categoryId=1413341197421846529
//    // 正常情况下参数应该是id，但我们可以用dish实体类，它有属性id。
//    // 这样我们就可以通过dish里面的status来筛选还在售卖的菜品
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询状态为1的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    /**
     * 根据菜品分类id查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    @Transactional
    // url：http://localhost:8080/dish/list?categoryId=1413341197421846529
    // 正常情况下参数应该是id，但我们可以用dish实体类，它有属性id。
    // 这样我们就可以通过dish里面的status来筛选还在售卖的菜品
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //因为只目前显示了菜品信息，还需要将每个菜品对应的口味信息加上
        List<DishDto> dtoList= list.stream().map(e -> {
            DishDto dishDto = new DishDto();
            //先将菜品信息复制过来
            BeanUtils.copyProperties(e,dishDto);

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(e.getId() != null, DishFlavor::getDishId, e.getId());

            //分别得到每个菜品对应的口味信息
            List<DishFlavor> list1 = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(list1);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dtoList);
    }
}

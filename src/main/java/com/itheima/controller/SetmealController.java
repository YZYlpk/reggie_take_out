package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<SetmealDto> pageInfo = setmealService.page(page, pageSize, name);
        return  R.success(pageInfo);
    }


    /**
     * 批量修改套餐起售和停售状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    // Request URL: http://localhost:8080/setmeal/status/0?ids=1415580119015145474,1653641213449551873
    // 方法：第二个参数可以设置long型数组，不是基本类型都得加requestparam注解
    // 其他方法在dishController里面
    public R<String> updataStatus(@PathVariable int status,@RequestParam List<Long> ids){

       setmealService.updateStatus(status,ids);

       return R.success("套餐状态修改成功");

    }


    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

    /**
     * 据id查询套餐信息和对应的关联菜品信息
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithFlavor(id);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public  R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateSB(setmealDto);

        return R.success("修改成功");
    }

    /**
     * 根据套餐分类id查询对应的所有套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        Long categoryId = setmeal.getCategoryId();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(Setmeal::getStatus,1);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}

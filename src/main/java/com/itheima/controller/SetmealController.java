package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId +'_1'") //删除该套餐对应的分类id的所有缓存
    @ApiOperation(value = "新增套餐接口")
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

//        //清理套餐对应的一类菜品分类id的缓存数据
//        String key="setmeal_"+setmealDto.getCategoryId()+"_1";
//        redisTemplate.delete(key);

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
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
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
    @CacheEvict(value = "setmealCache",allEntries = true) //删除该名字下所有缓存数据
    @ApiOperation(value = "批量修改套餐起售和停售状态接口")
    // Request URL: http://localhost:8080/setmeal/status/0?ids=1415580119015145474,1653641213449551873
    // 方法：第二个参数可以设置long型数组，不是基本类型都得加requestparam注解
    // 其他方法在dishController里面
    public R<String> updataStatus(@PathVariable int status,@RequestParam List<Long> ids){

       setmealService.updateStatus(status,ids);

//        //清理修改的菜品对应的菜品分类id的数据缓存
//        if(ids.size()==1){
//            //如果只修改了一个菜品，则清理对应的那个菜品分类id缓存即可；
//
//            Setmeal byId = setmealService.getById(ids.get(0));
//            String key="setmeal"+"_"+byId.getCategoryId()+"_1";
//            redisTemplate.delete(key);
//
//        } else if(ids.size()>1){
//            //如果修改多个菜品，则清理菜品全部的缓存
//            Set keys = redisTemplate.keys("setmeal_*");
//            redisTemplate.delete(keys);
//
//        }
       return R.success("套餐状态修改成功");

    }


    /**
     * 批量删除套餐
     * 该操作不需要清理缓存，因为删除的商品必须是停售状态，而
     * 修改停售状态就已经清理缓存了，并且后续的商品展示只会展示启售的商品，缓存也只会存正在启售的商品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "批量删除套餐接口")
    public R<String> delete(@RequestParam List<Long> ids){

        setmealService.removeWithDish(ids);

//        //清理修改的菜品对应的菜品分类id的数据缓存
//        if(ids.size()==1){
//            //如果只修改了一个菜品，则清理对应的那个菜品分类id缓存即可；
//
//            Setmeal byId = setmealService.getById(ids.get(0));
//            String key="setmeal"+"_"+byId.getCategoryId()+"_1";
//            redisTemplate.delete(key);
//
//        } else if(ids.size()>1){
//            //如果修改多个菜品，则清理菜品全部的缓存
//            Set keys = redisTemplate.keys("setmeal*");
//            redisTemplate.delete(keys);
//
//        }
        return R.success("套餐数据删除成功");
    }

    /**
     * 据id查询套餐信息和对应的关联菜品信息
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "后台套餐条件查询接口")
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
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId +'_1'") //删除该套餐对应的分类id的所有缓存
    @ApiOperation(value = "修改套餐接口")
    public  R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateSB(setmealDto);

//        //清理套餐对应的一类菜品分类id的缓存数据
//        String key="setmeal_"+setmealDto.getCategoryId()+"_1";
//        redisTemplate.delete(key);
        return R.success("修改成功");
    }

    /**
     * 根据套餐分类id查询对应的所有套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId +'_'+#setmeal.status")
    @ApiOperation(value = "移动端套餐条件查询接口")
    public R<List<Setmeal>> list(Setmeal setmeal){
        List<Setmeal> list=null;

//        //动态获取key
//        String key="setmeal_"+setmeal.getCategoryId()+"_"+setmeal.getStatus();//setmeal_1653318404253810690_1
//
//        //判断redis缓存是否有数据，有直接获取
//        list= (List<Setmeal>) redisTemplate.opsForValue().get(key);
//        if (list!=null){
//            return R.success(list);
//        }

        //没有则从数据库获取
        Long categoryId = setmeal.getCategoryId();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(Setmeal::getStatus,1);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        list = setmealService.list(queryWrapper);

//        //并缓存进redis
//        redisTemplate.opsForValue().set(key,list,60, TimeUnit.MINUTES);

        return R.success(list);
    }
}

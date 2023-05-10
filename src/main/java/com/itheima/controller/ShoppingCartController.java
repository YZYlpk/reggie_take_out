package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查看购物车接口")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车.....");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 添加到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加到购物车接口")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置购物车的使用者id(不然不清楚是谁的购物车)
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //查询当前菜品/套餐是否已经在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        if (shoppingCart.getDishId()!=null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //拿到符合条件的数据
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if(one!=null){
            //有的话number+1
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }else {
            //没有的话新加进去,数据库设置了number默认值为1，所以不需要赋值
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success(shoppingCart);
    }

    /**
     * 购物车减掉商品数量（修改）
     * @param map 菜品/套餐的id
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation(value = "购物车减少商品数量接口")
    public R<String> sub(@RequestBody Map<String,Long> map){
        //设置购物车的使用者id(不然不清楚是谁的购物车)
        Long id = BaseContext.getCurrentId();

        //判断map是否为空
        if(!map.isEmpty()){
            LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,id);
            //查询当前是菜品还是套餐
            if(map.get("dishId")!=null){
                Long dishId = map.get("dishId");
                queryWrapper.eq(ShoppingCart::getDishId,dishId);
            }else{
                Long setmealId = map.get("setmealId");
                queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            }

            //数据库拿到数据
            ShoppingCart one = shoppingCartService.getOne(queryWrapper);
            if (one!=null){//判断是否为空
                if (one.getNumber()==1){//只有一条，直接移除
                    shoppingCartService.removeById(one.getId());
                } else if (one.getNumber()>1) {//有多条，则-1
                    one.setNumber(one.getNumber()-1);
                    shoppingCartService.updateById(one);
                }
            }
            return R.success("修改成功");
        }

        return R.success("没有该数据");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车接口")
    public R<String> delete(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}

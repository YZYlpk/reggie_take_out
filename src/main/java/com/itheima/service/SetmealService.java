package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    public Page<SetmealDto> page(int page, int pageSize, String name);

    public void updateStatus(int status, List<Long> ids);

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    public SetmealDto getByIdWithFlavor(Long id);

    public void updateSB(SetmealDto setmealDto);
}

package com.itheima.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类管理实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("分类管理")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @ExcelIgnore //该字段不写入excel
    private Long id;


    //类型 1 菜品分类 2 套餐分类
    @ApiModelProperty("类型")
    @ExcelProperty(value = "1 菜品分类 2 套餐分类")
    private Integer type;


    //分类名称
    @ApiModelProperty("分类名称")
    @ExcelProperty(value = "分类名称")
    private String name;


    //顺序
    @ApiModelProperty("顺序")
    @ExcelIgnore //该字段不写入excel
    private Integer sort;


    //创建时间
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;


    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty(value = "更新时间")
    private LocalDateTime updateTime;


    //创建人
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建人")
    private Long createUser;


    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty(value = "修改人")
    private Long updateUser;

}

package com.itheima.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */

@Data
@ApiModel("员工")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("身份证号码")
    private String idNumber;//身份证号码

    @ApiModelProperty("状态")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * MP提供的公共字段自动填充的策略
     */
    //表示该字段在插入数据时会自动填充相应的值，而在更新数据时不会进行填充
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //插入和更新都填充字段
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}

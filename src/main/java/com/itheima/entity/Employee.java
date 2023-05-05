package com.itheima.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */

@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;//身份证号码

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

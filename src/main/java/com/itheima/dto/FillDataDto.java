package com.itheima.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 *填充excel模板
 */
@Data
@ApiModel("统计填入excel的数量和日期Dto")
public class FillDataDto {
    //日期
    private String date;
    //统计数量
    private int count;
}

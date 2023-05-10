package com.itheima.dto;

import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("订单dto")
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

    private int sumNum;//下单的商品数量
	
}

package com.leyou.order.service;

import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.UserOrderMapper;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.UserOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserOrderMapper userOrderMapper;
//    @Test
//    public void queryOrderByUid() {
//        orderService.queryOrderByUid(29L);
//    }
    @Test
    public void updateUserOrder(){
        List<OrderDetail> orderDetails = orderDetailMapper.selectAll();
        List<UserOrder> userOrderList =new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            UserOrder userOrder = new UserOrder();
            userOrder.setUid(29L);
            userOrder.setSkuid(orderDetail.getSkuId());
            userOrder.setOrderid(orderDetail.getOrderId());
            userOrderList.add(userOrder);
        }
        userOrderMapper.insertList(userOrderList);
    }
}
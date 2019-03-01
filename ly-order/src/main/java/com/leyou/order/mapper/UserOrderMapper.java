package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.UserOrder;
import org.apache.ibatis.annotations.Select;


import java.util.List;

public interface UserOrderMapper extends BaseMapper<UserOrder> {

    @Select("select  orderid from tb_order_user where uid = #{uid} and orderid in (select order_id from tb_order_status where status != 7 and status != 8) group by orderid")
    List<Long> queryUserRightOrderIds(Long uid);

    @Select("select  orderid from tb_order_user where uid = #{uid} and orderid in (select order_id from tb_order_status where status = 7 or status = 8) group by orderid")
    List<Long> queryUserErrorOrderIds(Long uid);

    @Select("select order_id from tb_order where user_id = #{uid} and order_id in (select order_id from tb_order_status where status = #{status})")
    List<Long> queryUserByUidAndStatus(Long uid, Integer status);
}

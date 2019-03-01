package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.mapper.UserOrderMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.UserOrder;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;


import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        //1.新增订单
        Order order = new Order();
        //1.1订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());

        //1.2用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        //1.3收货人信息
        //获取收货人信息
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());

        //1.4金额
        //把CartDTO转为一个map,key是sku的id,值是num
        Map<Long, Integer> numMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取所有sku的id
        Set<Long> ids = numMap.keySet();
        //根据id查询sku
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));

        //准备orderDetail的集合
        List<OrderDetail>  details = new ArrayList<>();

        //准备userOrder的集合
        List<UserOrder> userOrderList = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skus) {
            //计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());

            //封装orderdetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            details.add(orderDetail);

            //封装userorder
            UserOrder userOrder = new UserOrder();
            userOrder.setOrderid(orderId);
            userOrder.setSkuid(sku.getId());
            userOrder.setUid(sku.getUid());
            userOrderList.add(userOrder);

        }
        order.setTotalPay(totalPay);
        //实际金额: 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0 );

        //1.5 order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("[创建订单] 创建订单失败 ，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //2.新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size()){
            log.error("[创建订单] 创建订单失败 ，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //2.1新增厂商和订单
        count = userOrderMapper.insertList(userOrderList);
        if (count != userOrderList.size()){
            log.error("[创建订单] 创建订单失败,厂家信息未同步 ，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //3.新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1){
            log.error("[创建订单] 创建订单失败 ，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //4.减库存
        List<CartDTO> cartsDTOs = orderDTO.getCarts();
        goodsClient.decreaseStock(cartsDTOs);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);

        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null){
            //不存在
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);

        return order;
    }

    public PageResult<Order> queryOrderByUid(Long uId, Integer page, Integer rows) {
        //分页
        PageHelper.startPage(page,rows);

        //根据用户id查询订单
        Order order = new Order();
        order.setUserId(uId);
        List<Order> orderList = orderMapper.select(order);
        orderListAddStateDetail(orderList);
        //解析分页结果
        PageInfo<Order> info = new PageInfo<>(orderList);
        return new PageResult<>(info.getTotal(), orderList);
    }


    //查询正常订单
    public List<Order> queryOrderRight(){
        Example example = new Example(OrderStatus.class);
        example.createCriteria().andNotEqualTo("status",7);
        List<Order> orderList = queryOrderListByExample(example);
        return orderList;
    }

    //查询异常订单
    public List<Order> queryOrderError(){
        Example example = new Example(OrderStatus.class);
        example.createCriteria().orEqualTo("status",7).orEqualTo("status",8);
        List<Order> orderList = queryOrderListByExample(example);
        return orderList;
    }

    private List<Order> queryOrderListByExample(Example example) {
        List<OrderStatus> orderStatusList = orderStatusMapper.selectByExample(example);
        List<Long> orderIds = orderStatusList.stream().map(OrderStatus::getOrderId).collect(Collectors.toList());
        List<Order> orderList = orderMapper.selectByIdList(orderIds);
        if (CollectionUtils.isEmpty(orderList)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        orderListAddStateDetailFor(orderList, orderIds, orderStatusList);
        return orderList;
    }

    private void orderListAddStateDetail(List<Order> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        List<Long> orderIds = orderList.stream().map(Order::getOrderId).collect(Collectors.toList());
        List<OrderStatus> orderStatuseList = orderStatusMapper.selectByIdList(orderIds);
        orderListAddStateDetailFor(orderList, orderIds, orderStatuseList);
    }

    private void orderListAddStateDetailFor(List<Order> orderList, List<Long> orderIds, List<OrderStatus> orderStatuseList) {
        int flag = 0;

        for (Order orderOne : orderList) {
            int totalNum = 0;
            orderOne.setOrderStatus(orderStatuseList.get(flag));
            //查询订单详情
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderIds.get(flag));
            List<OrderDetail> details = orderDetailMapper.select(detail);
            for (OrderDetail orderDetail : details) {
                totalNum += orderDetail.getNum();
            }
            orderOne.setOrderDetails(details);
            orderOne.setTotalNum(totalNum);
            flag++;
        }
    }

    @Transactional
    public void updateOrderState(Long orderId, int state) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        orderStatus.setStatus(state);
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }

    public String createPayUrl(Long orderid) {
        //查询订单
        Order order = queryOrderById(orderid);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()){
            //订单状态异常 不为1
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = /*order.getActualPay()*/ 1L;
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        return payHelper.createOrder(orderid,actualPay,desc);

    }

    public void handleNotify(Map<String, String> result) {
        //1 数据校验
        payHelper.isSuccess(result);

        //2 校验签名
        payHelper.isValidSign(result);

        //3 校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr)){
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //3.1 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //3.2 获取订单中的金额
        Long orderid = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderid);
        if (totalFee != /*order.getActualPay() */ 1) {
            //金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //4 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderid);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("[订单回调]，订单支付成功！ ，订单编号:{}",orderid);
    }

    public PayState queryOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if (status != OrderStatusEnum.UN_PAY.value()){
            // 如果已经支付，实际上就就已经支付好了
            return PayState.SUCCESS;
        }

        //如果未支付，不一定是未支付，必须去微信查询支付状态
        return payHelper.queryPayState(orderId);
    }



    public PageResult<Order> queryOrderbyPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        List<Order> orderList = queryOrderRight();

        //解析分页结果
        PageInfo<Order> info =new PageInfo<>(orderList);
        return new PageResult<>(info.getTotal(),orderList);

    }

    public PageResult<Order>  queryOrderListErrorbyPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        List<Order> orderList = queryOrderError();

        //解析分页结果
        PageInfo<Order> info =new PageInfo<>(orderList);
        return new PageResult<>(info.getTotal(),orderList);
    }

    //**************************************厂家查询正常订单****************************************
    public PageResult<Order> queryUserOrderRightbyPage(Long uid, Integer page, Integer rows, String key) {
        //分页
        PageHelper.startPage(page, rows);

        //查询uid下的所有的正常订单编号
        List<Long> RightIds = userOrderMapper.queryUserRightOrderIds(uid);
        return getOrderPageResultbyOrderidList(RightIds);
    }
    //**************************************厂家查询异常订单订单****************************************
    public PageResult<Order> queryUserOrderErrorbyPage(Long uid, Integer page, Integer rows, String key) {
        //分页
        PageHelper.startPage(page, rows);

        //查询uid下的所有的正常订单编号
        List<Long> Errorids = userOrderMapper.queryUserErrorOrderIds(uid);
        return getOrderPageResultbyOrderidList(Errorids);
    }

    private PageResult<Order> getOrderPageResultbyOrderidList(List<Long> errorids) {
        if (CollectionUtils.isEmpty(errorids)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查询他们的状态
        List<OrderStatus> orderStatusList = orderStatusMapper.selectByIdList(errorids);
        if (CollectionUtils.isEmpty(orderStatusList)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查询订单
        List<Order> orderList = orderMapper.selectByIdList(errorids);
        //添加订单细节以及状态码
        if (CollectionUtils.isEmpty(orderList)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        orderListAddStateDetailFor(orderList, errorids, orderStatusList);

        //解析分页结果
        PageInfo<Order> info = new PageInfo<>(orderList);
        return new PageResult<>(info.getTotal(), orderList);
    }

    public PageResult<Order> queryOrderByUidAndState(Long uid, Integer page, Integer rows, Integer status) {
        //分页
        PageHelper.startPage(page, rows);
        List<Long> ids = userOrderMapper.queryUserByUidAndStatus(uid, status);
        List<Order> orderList = orderMapper.selectByIdList(ids);
        orderListAddStateDetail(orderList);
        //解析分页结果
        PageInfo<Order> info = new PageInfo<>(orderList);
        return new PageResult<>(info.getTotal(), orderList);
    }
}

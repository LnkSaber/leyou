package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.leyou.order.config.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.wxpay.sdk.WXPayConstants.*;
@Slf4j
@Component
public class PayHelper {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    public String createOrder(Long orderId , Long totalPay , String desc){
        try {
            HashMap<String, String> data = new HashMap<>();
            //商品描述
            data.put("body",desc);
            //订单号
            data.put("out_trade_no",orderId.toString());
            //金额，单位是分
            data.put("total_fee",totalPay.toString());
            //调用微信支付的终端IP
            data.put("spbill_create_ip","127.0.0.1");
            //回调地址
            data.put("notify_url",config.getNotifyUrl());
            //交易类型为扫描支付
            data.put("trade_type","NATIVE");

            //利用wxPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            //判断通信和业务标识
            isSuccess(result);

            //下单成功，获取支付链接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error("[微信下单] 创建预交易订单异常失败",e);
            return null;
        }

    }

    public void isSuccess(Map<String, String> result) {
        //判断通信标识
        String returnCode = result.get("return_code");
        if (FAIL.equals(returnCode)){
            //通信失败
            log.error("[微信下单] 微信下单通信失败，失败原因:{}",result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        //判断业务标识
        String resultCode = result.get("result_code");
        if (FAIL.equals(resultCode)){
            //业务失败
            log.error("[微信下单] 微信下单业务失败，错误码:{}，错误原因:{}",result.get("err_code"),result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    public void isValidSign(Map<String, String> data) {
        try {
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), SignType.MD5);

            //和传过来的签名进行比较
            String sign = data.get("sign");
            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }
    }

    public PayState queryPayState(Long orderId) {
        try {
            //组织请求参数  //其他4个参数已经默认填上了
            Map<String, String> data = new HashMap<>();
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //查询状态
            Map<String, String> result = wxPay.orderQuery(data);

            //校验状态
            isSuccess(result);

            //校验签名
            isValidSign(result);

            //校验金额
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if (StringUtils.isEmpty(totalFeeStr)){
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            //3.1 获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            //3.2 获取订单中的金额
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (totalFee != /*order.getActualPay() */ 1) {
                //金额不符
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            /***
             * SUCCESS—支付成功
             * REFUND—转入退款
             * NOTPAY—未支付
             * CLOSED—已关闭
             * REVOKED—已撤销(刷卡支付)
             * USERPAYING--用户支付中
             * PAYERROR--支付失败(其他原因，如银行返回失败)
             */
            String tradeState = result.get("trade_state");
            if (SUCCESS.equals(tradeState)){
                //支付成功
                // 修改订单状态
                OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAYED.value());
                status.setOrderId(orderId);
                status.setPaymentTime(new Date());
                int count = orderStatusMapper.updateByPrimaryKeySelective(status);
                if (count != 1){
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }
                //返回成功
                return PayState.SUCCESS;
            }

            if ("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)){
                return PayState.NOT_PAY;
            }

            return PayState.FAIL;
        } catch (Exception e) {
            return  PayState.NOT_PAY;
        }
    }
}

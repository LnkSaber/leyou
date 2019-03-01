package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Api("微信支付回调通知")
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;
    /**
     * 微信支付成功回调
     * @param result
     * @return
     */
    @PostMapping(value = "pay" , produces = "application/xml")
    @ApiOperation(value = "微信异步回调", notes = "微信异步回调(外网->内网)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "成功处理微信回调，向微信发送校验成功信息"),
            @ApiResponse(code = 500, message = "服务器异常")
    })
    public Map<String,String> successNotify (@RequestBody Map<String,String> result){
        //处理回调
        orderService.handleNotify(result);
        log.info("[支付回调] 接收微信支付回调,结果:{}", result);
        //返回成功
        Map<String, String> msg = new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }

}

package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Api("授权接口")
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;


    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @ApiOperation(value = "登录授权，接收用户和密码,校验，并将生成的token保存到浏览器中的cookie之中")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", required = true, value = "账户的用户名"),
            @ApiImplicitParam(name = "password", required = true, value = "账户的密码")
    })
    @ApiResponse(code = 204, message = "授权登录成功，且无返回值")
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response){
        //登录
        String token=  authService.login(username,password);
        //写入cookie
        CookieUtils.newBuilder(response).httpOnly().request(request)
                .build(prop.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * @param token
     * @return
     */
    @ApiOperation("校验是否授权过,从本地的cookie中取出token，传送到后台校验")
    @ApiImplicitParam(name = "token", required = true, value = "从cookie中获取的token(加密后的载荷)，后台教研")
    @ApiResponses({
            @ApiResponse(code = 200, message = "校验成功"),
            @ApiResponse(code = 403, message = "未授权")
    })
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue ("LY_TOKEN") String token,
              HttpServletRequest request, HttpServletResponse response){
        try {
            //解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //刷新token,重新生成token
            String newtoken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            //写入cookie
            CookieUtils.newBuilder(response).httpOnly().request(request)
                    .build(prop.getCookieName(),newtoken);
            //已登录，返回用户信息
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}

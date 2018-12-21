package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     *
     * @param token
     * @return
     */
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

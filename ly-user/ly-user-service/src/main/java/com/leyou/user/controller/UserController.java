package com.leyou.user.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.user.pojo.User;
import com.leyou.user.pojo.UserRole;
import com.leyou.user.service.*;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.tree.VoidDescriptor;

import javax.naming.Name;
import javax.validation.Valid;
import java.util.stream.Collectors;

@Api("用户接口")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据
     * @param data
     * @param type                                                                                                                                                                                                                          b
     * @return
     */
    @ApiOperation(value = "数据校验接口", notes = "用于前端检验用户名和电话号码是否在后台被注册过")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "data", required = true, value = "用来校验的数据(注册用户名/手机号)"),
            @ApiImplicitParam(name = "type", required = true, value = "数据的类型（1 用户名 2 手机号）")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "该用户名/手机号可用做注册申请"),
            @ApiResponse(code = 400, message = "无效的用户数据类型"),
            @ApiResponse(code = 500, message = "服务器异常")
    })
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,
                                             @PathVariable("type") Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 发送短信
     * @param phone
     * @return
     */
    @ApiOperation(value = "短信接口", notes = "用于短信发送验证码(本地生成6位随机数)")
    @ApiImplicitParam(name = "phone", required = true, value = "发送验证短信所需手机号")
    @ApiResponses({
            @ApiResponse(code = 204, message = "发送成功，无内容返回"),
            @ApiResponse(code = 500, message = "服务器异常")
    })
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册
     * @param user
     * @param result
     * @param code
     * @return
     */
    @ApiOperation(value = "注册接口", notes = "用于注册账号(前后端均有数据校验，后[hibernate validation])")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", required = true, value = "前端接收过来的用户注册数据(用户名,密码,电话)"),
            @ApiImplicitParam(name = "code", required = true, value = "注册短信所发送的验证码,用户手机接收")
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "成功创建且无返回值"),
            @ApiResponse(code = 500, message = "服务器异常")
    })
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code){
        if (result.hasFieldErrors()) {
            throw new RuntimeException(result.getFieldErrors().stream()
                    .map(e->e.getDefaultMessage()).collect(Collectors.joining("|")));
        }
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @ApiOperation(value = "校验用户接口", notes = "接收用户名和密码,用于查询该用户是否存在")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用于校验的用户名"),
            @ApiImplicitParam(name = "password", value = "用于校验的密码")
    })
    @ApiResponses({
            @ApiResponse(code = 400, message = "用户名或者密码错误"),
            @ApiResponse(code = 200, message = "校验成功")
    })
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }

    @GetMapping("userList")
    public ResponseEntity<PageResult<User>> queryUserListPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key){
        return ResponseEntity.ok(userService.queryUserListPage(page, rows, sortBy, desc,key));
    }
    @PutMapping("role")
    public ResponseEntity<Void> UpdateUserRole(@RequestBody UserRole userRole){
        userService.UpdateUserRole(userRole);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

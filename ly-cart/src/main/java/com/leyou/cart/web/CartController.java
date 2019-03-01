package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api("购物车接口")
@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 新增购物车
     * @param cart
     * @return
     */
    @ApiOperation(value = "接收前端传来的购物车数据,创建购物车记录")
    @ApiImplicitParam(name = "cart", required = true, value = "购物车的json对象")
    @ApiResponse(code = 201, message = "购物车车成功创建,且无返回值" )
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车
     * @return
     */
    @ApiOperation(value = "查询购物车的列表(前提登陆用户后)")
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     * 修改购物车的商品数量
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改购物车的商品数量(前提登陆用户后)")
    @ApiResponse(code = 204, message = "修改成功，且无返回值")
    public ResponseEntity<Void> updateCartNum(
            @RequestParam("id") Long skuId,
            @RequestParam("num") Integer num){
        cartService.updateCartNum(skuId,num);
     return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车数据
     * @param skuId
     * @return
     */
    @ApiOperation(value = "删除购物车的单个商品(前提登陆用户后)")
    @ApiImplicitParam(name = "{skuId}", required = true, value = "商品集子集(skuId)")
    @ApiResponse(code = 204, message = "删除选定商品成功,且无返回值")
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

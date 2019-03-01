package com.leyou.page.web;

import com.leyou.page.service.PageService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Api("页面静态化接口")
@Controller
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    @ApiOperation(value = "根据html模板,静态化页面", notes = "Thymeleaf静态化")
    @ApiImplicitParam(name = "{id}", required = true, value = "商品的spuId,用于后台查询模型数据,返回到 XXX.html视图层")
    public String toItemPage(@PathVariable("id") Long spuId , Model model){
        //查询模型数据
        Map<String,Object> attributes=pageService.loadModel(spuId);

        //准备模型数据
        model.addAllAttributes(attributes);
        //返回视图
        return "item";
    }
}

//package com.leyou.item.controller;
//
//import com.leyou.common.vo.PageResult;
//import com.leyou.item.pojo.Brand;
//import com.leyou.item.service.BrandService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author: HuYi.Zhang
// * @create: 2018-08-16 12:13
// **/
//@RestController
//@RequestMapping("brand")
//public class BrandController {
//
//    @Autowired
//    private BrandService brandService;
//
//    @GetMapping("/page")
//    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
//            @RequestParam(value = "page", defaultValue = "1")Integer page,
//            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
//            @RequestParam(value = "sortBy", required = false)String sortBy,
//            @RequestParam(value = "desc", defaultValue = "false")Boolean desc,
//            @RequestParam(value = "key", required = false)String key
//    ){
//        return ResponseEntity.ok(brandService.queryBrandByPage(page, rows, sortBy, desc, key));
//    }
//}

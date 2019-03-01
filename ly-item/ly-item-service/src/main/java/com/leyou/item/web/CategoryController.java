package com.leyou.item.web;

import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api("商品种类接口")
@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;
    /**
     *根據父節點id查詢商品分類
     * @param pid
     * @return
     */
    @ApiOperation(value = "查询商品分类", notes = "根据父节点id查询商品分类")
    @ApiImplicitParam(name = "pid", required = true, value = "商品种类(三级分类)的父id")
    @ApiResponse(code = 200, message = "查询成功，返回相应的种类列表")
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid") Long pid){
//        return ResponseEntity.status(HttpStatus.OK).body(null);
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

    /**
     * 根据id查询商品分类
     * @param ids
     * @return
     */
    @ApiOperation(value = "查询商品分类", notes = "根据id集合查询相应的商品种类")
    @ApiImplicitParam(name = "ids", required = true, value = "商品分类id集合")
    @ApiResponse(code = 200, message = "查询成功返回相应的商品分类列表")
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByuId(@PathVariable("bid") long bid){
        return ResponseEntity.ok(categoryService.queryCategoryByuId(bid));
    }


}

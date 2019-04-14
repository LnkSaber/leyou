package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import io.swagger.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("品牌服务接口")
@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    @ApiOperation(value = "分页查询品牌", notes = "分页查询品牌,返回分页后的品牌列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", required = false, value = "分页查询所需要的页数(当前页),默认值为1"),
            @ApiImplicitParam(name = "rows", required = false, value = "一页共有几条数据，默认值为5"),
            @ApiImplicitParam(name = "sortBy", required = false, value = "分类排序，默认为false"),
            @ApiImplicitParam(name = "desc", required = false, value = "排序方式desc 降序，asc 升序， 默认为升序"),
            @ApiImplicitParam(name = "key", required = false, value = "关键字搜索，默认为false")
    })
    @ApiResponse(code = 404, message = "品牌不存在")
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
//        PageResult<Brand> result=brandService.queryBrandByPage(page,rows,sortBy,desc,key);
        return ResponseEntity.ok(brandService.queryBrandByPage(page,rows,sortBy,desc,key));
    }

    //TODO 厂家实现新增品牌添加新的关系表是否要删除该方法
    /**
     * 新增品牌
     * @param brand
     * @param cids
     * @return
     */
    @ApiOperation(value = "新增品牌", notes = "根据商品种类(cid1,cid2,ci3)来创建商品品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brand", required = true, value = "品牌(名字，图片，首字母)"),
            @ApiImplicitParam(name = "cids", required = true, value = "三级分类cid(cid1,cid2,ci3)")
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "创建品牌成功，且无返回值"),
            @ApiResponse(code = 500, message = "服务器异常，新增品牌失败")
    })
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("cids") List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{cid}")
    public ResponseEntity<Void> updateBrand(@RequestBody Brand brand,@PathVariable("cid") List<Long> cids){
        brandService.updateBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //TODO 是否删除
    @DeleteMapping("/{bId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bId") Long bId){
        brandService.deleteBrand(bId);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据商品种类(cid)查询品牌
     * @param cid
     * @return
     */
    @ApiOperation(value = "查询品牌", notes = "根据商品种类(cid)，查询品牌")
    @ApiImplicitParam(name = "{cid}", required = true, value = "商品种类cid", type = "Long")
    @ApiResponse(code = 200, message = "查询成功，返回品牌列表")
    @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    /**
     * 根据品牌id查询品牌
     * @param id
     * @return
     */
    //TODO  厂家已完成分页查询，管理员是否需要分页查询
    @ApiOperation(value = "查询品牌", notes = "根据商品品牌(leyou),查询品牌")
    @ApiImplicitParam(name = "id", required = true, value = "品牌bid", type = "Long")
    @ApiResponse(code = 200, message = "查询成功,返回品牌信息")
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id){
        return ResponseEntity.ok(brandService.queryById(id));
    }

    /**
     * 获取品牌列表
     * @param ids
     * @return
     */
    @ApiOperation(value = "品牌列表", notes = "根据bid集合，获取品牌列表")
    @ApiImplicitParam(name = "ids", required = true, value = "品牌id的集合(bid1,bid2....)")
    @ApiResponse(code = 200, message = "查询成功，返回品牌列表")
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }



    //*****************************厂家**********************************//
    @GetMapping("user/page")
    public ResponseEntity<PageResult<Brand>> queryUserBrandByPage(
            @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "page" ,defaultValue = "1") Integer page,
            @RequestParam(value = "rows" ,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy" ,required = false) String sortBy,
            @RequestParam(value = "desc" ,defaultValue = "false") Boolean desc,
            @RequestParam(value = "key" , required = false) String key
    ){
//        PageResult<Brand> result=brandService.queryBrandByPage(page,rows,sortBy,desc,key);
        return ResponseEntity.ok(brandService.queryUserBrandByPage(uid,page,rows,sortBy,desc,key));
    }

    @PostMapping("user")
    public ResponseEntity<Void> saveUserBrand(Brand brand,@RequestParam("cids") List<Long> cids, @RequestParam("uid") Long uid){
        brandService.saveUserBrand(brand,cids,uid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/user/{bId}")
    public ResponseEntity<Void> deleteUserBrand(@PathVariable("bId") Long bId){
        brandService.deleteUserBrand(bId);
        return ResponseEntity.ok().build();
    }
}

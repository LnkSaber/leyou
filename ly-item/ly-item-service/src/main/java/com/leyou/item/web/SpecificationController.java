package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api("规格参数接口")
@RestController
@RequestMapping("spec")
public class SpecificationController  {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id查询规格组
     * @param cid
     * @return
     */
    @ApiOperation(value = "查询规格组", notes = "根据分类id查询规格组")
    @ApiImplicitParam(name ="cid", required = true, value = "商品种类cid")
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    /**
     * 新增规格组信息
     * @param specGroup
     * @return
     */
    @ApiOperation(value = "新增规格组")
    @ApiImplicitParam(name = "specGroup", required = true, value = "规格组")
    @ApiResponse(code = 201, message = "创建完成，且无返回值")
    @PostMapping("group")
    public ResponseEntity<Void> addSpecGroup(@RequestBody SpecGroup specGroup){
        specificationService.addSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /***
     * 更新商品规格组
     * @param specGroup
     * @return
     */
    @ApiOperation(value = "新增规格组")
    @ApiImplicitParam(name = "specGroup", required = true, value = "规格组")
    @PutMapping("group")
    public ResponseEntity<Void> UpdateSpecGroup(@RequestBody SpecGroup specGroup){
        specificationService.UpdateSpecGroup(specGroup);
        return ResponseEntity.ok().build();
    }


    /**
     * 删除商品规格组，以及与其对应的规格参数
     * @param gid
     * @return
     */
    @ApiOperation(value = "删除规格组")
    @ApiImplicitParam(name = "{gid}", required = true, value = "规格组gid")
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> DeleteSpecGroupByGid(@PathVariable("gid") Long gid){
        System.out.println(gid);
        specificationService.DeleteSpecGroupByGid(gid);
        return ResponseEntity.ok().build();
    }


    /***
     * 新增规格参数信息
     */
    @ApiOperation(value = "新增规格参数")
    @ApiImplicitParam(name = "specParam", required = true, value = "规格参数")
    @PostMapping("param")
    public ResponseEntity<Void> addSpecParam(@RequestBody SpecParam specParam){
        specificationService.addSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新规格参数信息
     * @param specParam
     * @return
     */
    @ApiOperation(value = "更新规格参数")
    @ApiImplicitParam(name = "specParam", required = true, value = "规格参数")
    @PutMapping("param")
    public ResponseEntity<Void> UpdateSpecParam(@RequestBody SpecParam specParam){
        specificationService.UpdateSpecParam(specParam);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "删除规格参数")
    @ApiImplicitParam(name = "id", required = true, value = "规格参数id")
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> DeleteSpecParamById(@PathVariable Long id){
        System.out.println(id);
        specificationService.DeleteSpecParamById(id);
        return ResponseEntity.ok().build();
    }


    /**
     * 查询参数的集合
     * @param gid   组id
     * @param cid   分类id
     * @param searching 是否搜索
     * @return
     */
    @ApiOperation(value = "查询规格参数", notes = "根据规格组gid, 商品种类cid, 搜索条件，查询规格参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gid",  value = "规格组gid"),
            @ApiImplicitParam(name = "cid",  value = "规格参数cid"),
            @ApiImplicitParam(name = "searching", value = "搜索条件")
    })
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByList(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        return ResponseEntity.ok(specificationService.queryParamList(gid,cid,searching));
    }


    /**
     *根据分类查询规格组及组内参数
     * @param cid
     * @return
     */
    @ApiOperation("根据分类查询规格组及组内参数")
    @ApiImplicitParam(name = "cid", required = true, value = "规格组id")
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListByCid(cid));
    }
}

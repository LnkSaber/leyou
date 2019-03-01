package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api("搜索接口")
@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @ApiOperation(value = "从ElasticSearch的桶查询商品信息")
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        System.out.println(request);
        return ResponseEntity.ok(searchService.search(request));
    }
}

package com.leyou.upload.web;

import com.leyou.upload.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@Api("上传接口")
@RestController
@RequestMapping("upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 上传图片
     * @param file
     * @return
     */
    @ApiOperation(value = "MultipartFile格式的文件,且仅允许图片文件上传(不可超过50M)")
    @ApiImplicitParam(name = "file", required = true, value = "不超过50M的图片文件")
    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){
//        String url=uploadService.uploadImage(file);
        return ResponseEntity.ok(uploadService.uploadImage(file));
    }
}

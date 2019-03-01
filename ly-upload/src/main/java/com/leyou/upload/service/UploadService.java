package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private UploadProperties prop;

    @Autowired
    private FastFileStorageClient storageClient;

//    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/gif","image/png","image/bmp","image/jpg");
    public String uploadImage(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            if(!prop.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image==null){
                throw  new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

//            //准备目标路径
//            File dest =new File("C:\\Users\\pc\\Desktop\\leyou\\upload",file.getOriginalFilename());
//            //保存图片到本地
//            file.transferTo(dest);

            //上传到FastDfs
//            String extension=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1); 效率差
            String extension= StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            log.info("[文件上传成功]：地址{}",prop.getBaseUrl()+storePath.getFullPath());
            //返回路径"http://image.leyou.com/" +
            return prop.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            //上传失败
            log.error("[文件上传] 上传文件失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}

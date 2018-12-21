package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String secret; //密钥
    private String pubKeyPath;//公钥路径
    private String priKeyPath;//私钥路径
    private Integer expire; //token过期时间
    private String cookieName; //cookie名称
    private PublicKey publicKey; // 公钥
    private PrivateKey privateKey; // 私钥

    //对象实例化之后，获取公钥和私钥
    @PostConstruct
    public void init()  {
        try {
            //公钥私钥如果不存在，先生成
            File priPath = new File(priKeyPath);
            File pubPath = new File(pubKeyPath);
            if (!pubPath.exists()||!priPath.exists()) {
                //生成公钥和私钥
                RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
            }
            //获取公私钥
            this.privateKey=RsaUtils.getPrivateKey(priKeyPath);
            this.publicKey=RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！",e);
            throw new RuntimeException();
        }

    }
}

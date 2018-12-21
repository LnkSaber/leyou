package com.leyou.auth.utils;

import com.leyou.auth.pojo.UserInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "C:\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "C:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

//    @Test
//    public void testRsa() throws Exception {
//        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
//    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        System.out.println(privateKey);
        System.out.println(publicKey);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
//        byte[] privateKey = Files.readAllBytes(new File(priKeyPath).toPath());
        String token = JwtUtils.generateToken(new UserInfo(20L, "林Saber"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoi5p6XU2FiZXIiLCJleHAiOjE1NDQ1Nzg0NDR9.LrqURg6pZ3dDogYi3g_m1lRDOEDnQqMe1H0hhABm2DHX7r3myqvfFRNtBPdsNAQl7HrZWPzvaHtHPt1eDv6UuzHgIGiMrrNHhuNubBp9zkcJwCrPWbR2Ulf2t9RJaMFC0ipoK0actiRlDnapNcsGWw-D9gcKSnJLUs71XwJNTvE";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
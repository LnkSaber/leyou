package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.UserSkuMappper;
import com.leyou.item.mapper.UserSpuMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.UserSku;
import com.leyou.item.pojo.UserSpu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private UserSpuMapper userspuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private UserSkuMappper userSkuMappper;
    @Test
    public void decreaseStock() {
        List<CartDTO> list = Arrays.asList(new CartDTO(2600242L, 2), new CartDTO(2600248L, 2));
        goodsService.decreaseStock(list);
    }
    @Test
    public void updateSpuUser(){
        List<Spu> spus = spuMapper.selectAll();
        List<Long> ids = spus.stream().map(Spu::getId).collect(Collectors.toList());
        for (Long id : ids) {
            UserSpu userSpu = new UserSpu();
            userSpu.setUid(29L);
            userSpu.setSpuid(id);
            userspuMapper.insert(userSpu);
        }
    }

    @Test
    public void UpdateSkuUser(){
        List<Long> SkuIds = skuMapper.selectAll().stream().map(Sku::getId).collect(Collectors.toList());
        List<UserSku> list = new ArrayList<>();
        for (Long skuId : SkuIds) {
            UserSku userSku = new UserSku();
            userSku.setUid(29L);
            userSku.setSkuid(skuId);
            list.add(userSku);
        }
        userSkuMappper.insertList(list);
    }
}
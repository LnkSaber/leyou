package com.leyou.page.service;

import org.hibernate.validator.constraints.EAN;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createHtml() {
//        for (int i=2; i<=182 ;i++){
//            Long num =Long.valueOf(i);
//            pageService.createHtml(num);
//        }
        List<Long> ids =new ArrayList<>();
        ids.add(191L);
        ids.add(192L);
        ids.add(197L);
        ids.add(199L);
        ids.add(200L);
        ids.add(201L);
        ids.add(202L);
        ids.add(203L);
        ids.add(205L);
        ids.add(208L);
        for (Long id : ids) {
            pageService.createHtml(id);
        }

    }
}
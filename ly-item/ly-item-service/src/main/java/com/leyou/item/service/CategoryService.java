package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryBrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.CategoryBand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    public List<Category> queryCategoryListByPid(Long pid) {
        //查詢條件，mapper會把對象中的非空屬性作爲查詢條件
        Category t=new Category();
        t.setParentId(pid);
        List<Category> list=categoryMapper.select(t);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;

    }

    public List<Category> queryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }

    public List<Category> queryCategoryByuId(long bid) {
        List<CategoryBand> categoryBandList = categoryBrandMapper.queryCategoryBandBybid(bid);
        List<Long> cids = categoryBandList.stream().map(CategoryBand::getCategory_id).collect(Collectors.toList());
         return categoryMapper.selectByIdList(cids);
    }
}

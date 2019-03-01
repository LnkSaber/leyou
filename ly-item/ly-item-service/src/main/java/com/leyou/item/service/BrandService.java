package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.UserBrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.UserBrand;
import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private UserBrandMapper userBrandMapper;


    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            //过滤条件
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
            //这里id ASC之间要有空格隔开，要不然分页助手会识别不了
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(),list);
    }

    //TODO 厂家完成新增品牌，原方法是否删除
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);

        if(count !=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(count !=1){
                throw new LyException(ExceptionEnum.CATEGORY_BRAND_SAVE_ERROR);
            }
        }
    }

    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw new LyException((ExceptionEnum.BRAND_NOT_FOUND));
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        if (brand.getId() == null){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        brandMapper.updateByPrimaryKeySelective(brand);
        brandMapper.deleteCategoryBrandBybid(brand.getId());
        for (Long cid : cids) {
            int count = 0;
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(count !=1){
                throw new LyException(ExceptionEnum.CATEGORY_BRAND_SAVE_ERROR);
            }
        }
    }

    //TODO 是否删除
    @Transactional
    public void deleteBrand(Long bId) {
        Brand brand = brandMapper.selectByPrimaryKey(bId);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        int count = brandMapper.deleteCategoryBrandBybid(brand.getId());
        if (count == 0){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        count = brandMapper.deleteByPrimaryKey(brand.getId());
        if (count == 0){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }



    //*******************厂家***************************//
    public PageResult<Brand> queryUserBrandByPage(Long uid, Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        UserBrand userBrand = new UserBrand();
        userBrand.setUid(uid);
        List<UserBrand> userBrands = userBrandMapper.select(userBrand);
        List<Long> ids = userBrands.stream().map(UserBrand::getBid).collect(Collectors.toList());
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        PageHelper.startPage(page,rows);
        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(brandList);
        info.setEndRow(rows);

        return new PageResult<>(info.getTotal(),brandList);
    }

    @Transactional
    public void saveUserBrand(Brand brand, List<Long> cids, Long uid) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);

        if(count !=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(count !=1){
                throw new LyException(ExceptionEnum.CATEGORY_BRAND_SAVE_ERROR);
            }
        }
        System.out.println(uid);
        //新增用户关系表
        UserBrand userBrand = new UserBrand();
        userBrand.setUid(uid);
        userBrand.setBid(brand.getId());
        userBrandMapper.insert(userBrand);
    }


    @Transactional
    public void deleteUserBrand(Long bId) {
        Brand brand = brandMapper.selectByPrimaryKey(bId);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        int count = brandMapper.deleteCategoryBrandBybid(brand.getId());
        if (count == 0){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        count = brandMapper.deleteByPrimaryKey(brand.getId());
        if (count == 0){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        count = userBrandMapper.deleteByPrimaryKey(bId);
        if (count == 0){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }

}

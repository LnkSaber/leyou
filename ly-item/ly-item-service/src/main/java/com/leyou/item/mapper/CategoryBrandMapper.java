package com.leyou.item.mapper;


import com.leyou.item.pojo.CategoryBand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface CategoryBrandMapper extends BaseMapper<CategoryBand> {
    @Select("select * from tb_category_brand where brand_id = #{bid}")
    List<CategoryBand> queryCategoryBandBybid(@Param("bid") Long bid);
}

package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_category_brand")
public class CategoryBand {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long category_id;
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long brand_id;
}

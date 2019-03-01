package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_brand_user")
public class UserBrand {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long bid;
    private Long uid;
}

package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_sku_user")
public class UserSku {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long skuid;
    private Long uid;
}

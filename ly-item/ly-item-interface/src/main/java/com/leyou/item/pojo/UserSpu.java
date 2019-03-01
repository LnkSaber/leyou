package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_spu_user")
public class UserSpu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long spuid;
    private Long uid;
}

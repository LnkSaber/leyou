package com.leyou.bid.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_bid")
public class Bid {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long bidid; //投标的id
    private String title; //标题
    private String solution; //解决方案
    private Long tenderid; //投标的id
    private Integer state;      //投标的状态 0招标中，1招标成功 2异常招标 3.已处理异常订单 4厂家撤销投标 5投标结束，已被淘汰 6 代销商已撤销
    private Long uid;       //投标的厂家id
    private Long price;     //一套的价格
}

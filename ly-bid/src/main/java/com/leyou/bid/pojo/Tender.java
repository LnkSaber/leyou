package com.leyou.bid.pojo;

import lombok.Data;
import lombok.ToString;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Data
@Table(name = "tb_tender")
@ToString
public class Tender {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long tenderid; //招标id
    private String title;   //招标标题
    private String description;     //招标描述
    private Long price;     //方案价格1套
    private Integer state;      //招标的状态 0招标中，1招标成功 2异常招标 3.已处理异常订单 4.代销商撤销招标 5 招标结束
    private Integer num;    //数量
    private Long totalpay;  //总价
    private Long finalbidid;    //最终方案的id
    private Long uid;   //用户id
    @Transient
    private List<Bid> bids; //改招标下的所有投标方案

}

package com.leyou.user.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_user_role")
public class UserRole {
    @Id
    private Long id; //用户id
    private Long role; //权限 0 管理员 1 厂家 2 代销商

}

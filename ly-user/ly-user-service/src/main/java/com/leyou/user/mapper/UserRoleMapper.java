package com.leyou.user.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.user.pojo.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Update;

public interface UserRoleMapper extends BaseMapper<UserRole> {
}

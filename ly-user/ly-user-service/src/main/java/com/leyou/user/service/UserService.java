package com.leyou.user.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.LyUserApplication;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.mapper.UserRoleMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.pojo.UserRole;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Id;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Long INIT_ROLE = 2L;
    private static final String KEY_PREFIX="user:verify:phone:";

    public Boolean checkData(String data, Integer type) {
        User user =new User();
        //判断数据类型
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user)==0;
    }

    public void sendCode(String phone) {
        //生成key
        String key=KEY_PREFIX+phone;
        //生成验证码
        String code= NumberUtils.generateCode(6);
        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);

    }

    public void register(User user, String code) {
        //从redis中取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //校验验证码
        if (!StringUtils.equals(code,cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //生成盐
        String salt=CodecUtils.generateSalt();
        user.setSalt(salt);
        // 对密码加密
        user.setPassword( CodecUtils.md5Hex(user.getPassword(),salt));
        //写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
        //加入权限表
        User one = userMapper.selectOne(user);
        UserRole userRole = new UserRole(one.getId(),INIT_ROLE);
        userRoleMapper.insert(userRole);

    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        //查询用户
        User recode = new User();
        recode.setUsername(username);
        User user = userMapper.selectOne(recode);
        //校验
        if(user==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //校验密码
        if (!StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password,user.getSalt()))) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        UserRole userRole = userRoleMapper.selectByPrimaryKey(user.getId());
        user.setRole(userRole.getRole());
        //用户名和密码正确
        return user;
    }


    public PageResult<User> queryUserListPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example =new Example(User.class);
        if (StringUtils.isNotBlank(key)) {
            //过滤条件
            example.createCriteria().orLike("name", "%"+key+"%");
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
            //这里id ASC之间要有空格隔开，要不然分页助手会识别不了
        }
        //查询
        List<User> userList = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userList)) {
            throw new LyException(ExceptionEnum.USER_NOT_FOUND);
        }
        List<Long> uIds = userList.stream().map(User::getId).collect(Collectors.toList());
        List<UserRole> roleList = userRoleMapper.selectByIdList(uIds);
        int flag = 0 ;
        for (User user : userList) {
            user.setRole(roleList.get(flag).getRole());
            flag ++;
        }
        //解析分页结果
        PageInfo<User> info = new PageInfo<>(userList);
        return new PageResult<>(info.getTotal(), userList);
    }

    @Transactional
    public void UpdateUserRole(UserRole userRole) {
        int count = userRoleMapper.updateByPrimaryKeySelective(userRole);
        if (count != 1){
            throw new LyException(ExceptionEnum.USER_UPDATE_ROLE_ERROR);
        }
    }
}

package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.common.RedisKey;
import com.turing.entity.User;
import com.turing.entity.vo.RegisterVo;
import com.turing.entity.dto.UserDto;
import com.turing.exception.RequestParamValidationException;
import com.turing.mapper.UserMapper;
import com.turing.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Override
    public User getByOpenId(String openid) {
        log.info("openid:" + openid);
        User user = (User) redisTemplate.opsForHash().get(RedisKey.USER_HASH_KEY, RedisKey.USER_FILED_KEY + openid);
        if(user != null) {
            log.info("Redis缓存查询用户结果：" + user);
            return user;
        }
        user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", openid));
        log.info("MySQL数据库查询用户结果：" + user);
        if(user != null) {
            redisTemplate.opsForHash().put(RedisKey.USER_HASH_KEY, RedisKey.USER_FILED_KEY+openid, user);
            log.info("数据库查询到该用户,已经缓存至Redis!");
        }
        return user;
    }

    @Override
    public UserDto register(RegisterVo registerVo) {
        User user = new User();
        user.setOpenid(registerVo.getOpenid());
        user.setClassname(registerVo.getClassname());
        user.setName(registerVo.getName());
        user.setAdmin(false);
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        redisTemplate.opsForHash().put(RedisKey.USER_HASH_KEY, RedisKey.USER_FILED_KEY + user.getOpenid(), user);
        UserDto userDto = new UserDto();
        userDto.transform(user);
        return userDto;
    }

    @Override
    public User update(User user) {
        userMapper.updateById(user);
        redisTemplate.opsForHash().put(RedisKey.USER_HASH_KEY, RedisKey.USER_FILED_KEY+user.getOpenid(), user);
        return user;
    }


}

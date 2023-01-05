package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaolyuh.annotation.CachePut;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.support.CacheMode;
import com.turing.common.RedisKey;
import com.turing.entity.User;
import com.turing.entity.vo.RegisterVo;
import com.turing.mapper.UserMapper;
import com.turing.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedisTemplate redisTemplate;

    private static final int MAX_LENGTH = 28;

    @Override
    @Cacheable(cacheNames = RedisKey.USER_KEY, key = "#openid", depict = "用户缓存", cacheMode = CacheMode.SECOND,
            secondaryCache = @SecondaryCache(expireTime = 60 * 3, preloadTime = 20, timeUnit = TimeUnit.MINUTES, forceRefresh = true, magnification = 200))
    public User getByOpenId(String openid) {
        if(openid.length() > MAX_LENGTH && openid.startsWith("\"") && openid.endsWith("\"")) {
            openid = openid.replaceAll("\"", "");
        }
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User :: getOpenid, openid));
        return user;
    }

    @Override
    @CachePut(cacheNames = RedisKey.USER_KEY, key = "#registerVo.openid", depict = "用户缓存", cacheMode = CacheMode.SECOND,
            secondaryCache = @SecondaryCache(expireTime = 60 * 3, preloadTime = 20, timeUnit = TimeUnit.MINUTES, forceRefresh = true, magnification = 200))
    public User register(RegisterVo registerVo) {
        User user = new User();
        user.setOpenid(registerVo.getOpenid());
        user.setClassname(registerVo.getClassname());
        user.setName(registerVo.getName());
        user.setAdmin(false);
        user.setRegisterTime(LocalDateTime.now());
        baseMapper.insert(user);
        return user;
    }

    @Override
    @CachePut(cacheNames = RedisKey.USER_KEY, key = "#user.openid", depict = "用户缓存", cacheMode = CacheMode.SECOND,
            secondaryCache = @SecondaryCache(expireTime = 60 * 3, preloadTime = 20, timeUnit = TimeUnit.MINUTES, forceRefresh = true, magnification = 200))
    public User update(User user) {
        baseMapper.updateById(user);
        log.info("更新用户信息: {}", user);
        return user;
    }
}

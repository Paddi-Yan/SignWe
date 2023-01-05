package com.turing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaolyuh.annotation.*;
import com.github.xiaolyuh.support.CacheMode;
import com.turing.common.RedisKey;
import com.turing.entity.Door;
import com.turing.mapper.DoorMapper;
import com.turing.service.DoorService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
public class DoorServiceImpl extends ServiceImpl<DoorMapper, Door> implements DoorService {

    @Resource
    private DoorMapper doorMapper;


    @Resource
    private RedisTemplate redisTemplate;

    private static final String ID = "TuringTeam";

    @Override
    @Cacheable(cacheNames = RedisKey.DOOR_KEY, key = "#id", depict = "开门状态缓存信息", cacheMode = CacheMode.ALL,
            firstCache = @FirstCache(initialCapacity = 1, maximumSize = 1, expireTime = 15, timeUnit = TimeUnit.HOURS),
            secondaryCache = @SecondaryCache(expireTime = 15, timeUnit = TimeUnit.HOURS, forceRefresh = true))
    public Door getDoorStatus(String id) {
        Door door = doorMapper.selectById(ID);
        return door;
    }

    @Override
    @CachePut(cacheNames = RedisKey.DOOR_KEY, key = "#door.id", depict = "开门状态缓存信息", cacheMode = CacheMode.ALL,
            firstCache = @FirstCache(initialCapacity = 1, maximumSize = 1, expireTime = 15, timeUnit = TimeUnit.HOURS),
            secondaryCache = @SecondaryCache(expireTime = 15, timeUnit = TimeUnit.HOURS, forceRefresh = true))
    public Door openDoor(Door door, String username) {
        door.setOpen(true);
        door.setOpenInfo(username);
        door.setOpenTime(LocalDateTime.now());
        doorMapper.updateById(door);
        return door;
    }

    @Override
    @CacheEvict(cacheNames = RedisKey.DOOR_KEY, key = "#door.id")
    public Door closeDoor(Door door, String username) {
        door.setCloseInfo(username);
        door.setOpen(false);
        doorMapper.updateById(door);
        return door;
    }
}

package com.turing.service.impl;

import com.turing.common.RedisKey;
import com.turing.entity.Door;
import com.turing.mapper.ChairsMapper;
import com.turing.mapper.DoorMapper;
import com.turing.service.DoorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class DoorServiceImpl extends ServiceImpl<DoorMapper, Door> implements DoorService {

    @Resource
    private DoorMapper doorMapper;


    @Resource
    private RedisTemplate redisTemplate;

    private static final String ID = "TuringTeam";

    @Override
    public Door getDoorStatus() {
        Door door = (Door) redisTemplate.opsForValue().get(RedisKey.DOOR_KEY);
        if(door != null && door.getOpen() != null) {
            return door;
        }
        door = doorMapper.selectById(ID);
        redisTemplate.opsForValue().set(RedisKey.DOOR_KEY, door);
        return door;
    }

    @Override
    public Door openDoor(Door door, String username) {
        door.setOpen(true);
        door.setOpenInfo(username);
        door.setOpenTime(LocalDateTime.now());
        doorMapper.updateById(door);
        redisTemplate.opsForValue().set(RedisKey.DOOR_KEY, door);
        return door;
    }

    @Override
    public Door closeDoor(Door door, String username) {
        door.setCloseInfo(username);
        door.setOpen(false);
        doorMapper.updateById(door);
        redisTemplate.opsForValue().set(RedisKey.DOOR_KEY, door);
        return door;
    }


}

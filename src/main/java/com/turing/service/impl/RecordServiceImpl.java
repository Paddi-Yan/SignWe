package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.common.RedisKey;
import com.turing.entity.Record;
import com.turing.entity.User;
import com.turing.mapper.RecordMapper;
import com.turing.service.RecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.turing.service.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 学习记录历史 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Override
    public List<Record> getRecordByUser(User user) {
        List<Record> recordList = redisTemplate.opsForList()
                                  .range(RedisKey.RECORD_KEY + user.getOpenid(), 0, -1);
        if(recordList != null && !recordList.isEmpty()) {
            return recordList;
        }
        recordList = recordMapper.getByUser(user);
        if(recordList != null && !recordList.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(RedisKey.RECORD_KEY+user.getOpenid(), recordList);
        }
        return recordList;
    }

    @Override
    public List<Record> getYesterdayRecord() {
        List<Record> recordList = new ArrayList<>();
        List<Record> finalRecordList = recordList;
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                Set<String> keys = redisOperations.keys(RedisKey.RECORD_KEY + "*");
                for(String recordKey : keys) {
                    String openid = recordKey.replace(RedisKey.RECORD_KEY, "");
                    User user = userService.getByOpenId(openid);
                    List<Record> records = redisOperations.opsForList().range(recordKey, 0, user.getTodayCount() - 1);
                    finalRecordList.addAll(records);
                }
                return redisOperations.exec();
            }
        });
        if(!finalRecordList.isEmpty()) {
            return finalRecordList;
        }
        LocalDateTime yesterdayTime = LocalDateTime.now().minusDays(1);
        recordList = recordMapper.selectList(new QueryWrapper<Record>().ge("final_stop_time", yesterdayTime));
        return recordList;
    }

    @Override
    public void deleteLogical() {
        //删除Redis中的缓存
        redisTemplate.delete(redisTemplate.keys(RedisKey.RECORD_KEY+"*"));
        //逻辑删除Mysql中的记录
        recordMapper.delete(null);
    }
}

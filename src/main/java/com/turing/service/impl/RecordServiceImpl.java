package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.turing.common.RedisKey;
import com.turing.common.ScrollResult;
import com.turing.entity.Chairs;
import com.turing.entity.Record;
import com.turing.entity.User;
import com.turing.exception.AuthenticationException;
import com.turing.exception.RequestParamValidationException;
import com.turing.mapper.RecordMapper;
import com.turing.service.RecordService;
import com.turing.service.UserService;
import com.turing.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习记录历史 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    private final RecordMapper recordMapper;
    private final RedisTemplate redisTemplate;
    private final UserService userService;

    private final ExecutorService cacheThreadPool;

    @Override
    public List<Record> getRecordByUser(String id) {
        User user = userService.getById(id);
        if(user == null) {
            throw new RequestParamValidationException(ImmutableMap.of("userId", id));
        }
        if(user.getClassname() == null || user.getName() == null) {
            throw new AuthenticationException();
        }
        List<Record> recordList = redisTemplate.opsForList()
                                               .range(RedisKey.RECORD_KEY + user.getOpenid(), 0, -1);
        if(recordList != null && !recordList.isEmpty()) {
            return recordList;
        }
        recordList = recordMapper.getByUser(user);
        if(recordList != null && !recordList.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(RedisKey.RECORD_KEY + user.getOpenid(), recordList);
        }
        return recordList;
    }

    private List<Record> getYesterdaySignedRecord(String userId) {
        long max = System.currentTimeMillis();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        long min = TimeUtils.getTimeInMillis(yesterday);
        Set<Record> records = redisTemplate.opsForZSet().range(RedisKey.RECORD_KEY + userId, min, max);
        if(CollectionUtil.isNotEmpty(records)) {
            return new ArrayList<>(records);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Record> getYesterdaySignedRecordList() {
        List<Record> recordList = new ArrayList<>();
        LocalDateTime yesterdayTime = LocalDateTime.now().minusDays(1);
        String todaySignMemberKey = RedisKey.DAY_STATISTICS_KEY + yesterdayTime.format(DateTimeFormatter.ofPattern(":yyyy/MM/DD"));
        log.info("今日签到用户集合Key：{}", todaySignMemberKey);
        Set<String> todaySignMemberIds = redisTemplate.opsForSet().members(todaySignMemberKey);
        if(todaySignMemberIds == null || todaySignMemberIds.isEmpty()) {
            return Collections.emptyList();
        }
        for(String userId : todaySignMemberIds) {
            List<Record> yesterdaySignedRecord = getYesterdaySignedRecord(userId);
            if(CollectionUtil.isNotEmpty(yesterdaySignedRecord)) {
                recordList.addAll(yesterdaySignedRecord);
            }
            User user = userService.getById(userId);
            user.setTodayCount(0);
            user.setTodayTime(0);
            userService.update(user);
        }
        if(!recordList.isEmpty()) {
            return recordList;
        }
        recordList = recordMapper.selectList(new LambdaQueryWrapper<Record>().ge(Record :: getFinalStopTime, yesterdayTime));
        if(recordList == null || recordList.isEmpty()) {
            return Collections.emptyList();
        }
        log.error("学习记录缓存与数据库出现不一致,即将进行缓存重建");
        cacheThreadPool.execute(() -> {
            //执行这里的缓存重建的条件为：[缓存]中[查询不到]所有今日[有签到记录]的人的学习记录 && 数据库能查到记录 => 缓存和数据库出现不一致
            List<Record> rebuildRecords = recordMapper.selectList(new LambdaQueryWrapper<Record>().eq(Record :: getDeleted, 0));
            Map<String, List<Record>> userToRecord = rebuildRecords.stream()
                                                                   .collect(Collectors.groupingBy(Record :: getUserId));
            for(Map.Entry<String, List<Record>> entry : userToRecord.entrySet()) {
                HashSet<ZSetOperations.TypedTuple> typedTuples = new HashSet<>();
                for(Record record : entry.getValue()) {
                    typedTuples.add(new DefaultTypedTuple(record, (double) TimeUtils.getTimeInMillis(record.getFinalStopTime())));
                }
                redisTemplate.opsForZSet().add(RedisKey.RECORD_KEY + entry.getKey(), typedTuples);
            }
        });
        return recordList;
    }

    @Override
    public void deleteLogical() {
        //删除Redis中的缓存
        redisTemplate.delete(redisTemplate.keys(RedisKey.RECORD_KEY + "*"));
        //逻辑删除Mysql中的记录
        recordMapper.delete(null);
    }

    @Transactional
    @Override
    public void insertRecord(Chairs chair,
                             User user,
                             LocalDateTime signInTime,
                             LocalDateTime signOutTime,
                             Integer studyTime) {
        Record record = new Record();
        record.setChair(chair.getId());
        record.setDistance(user.getFinalDistance());
        record.setOpenid(user.getOpenid());
        record.setName(user.getName());
        record.setClassname(user.getClassname());
        record.setFinalStartTime(signInTime);
        record.setFinalStopTime(signOutTime);
        record.setStudyTime(studyTime);
        record.setUserId(user.getId());
        recordMapper.insert(record);
        log.info("添加学习记录:{}", record);
        redisTemplate.opsForZSet().add(RedisKey.RECORD_KEY + user.getId(), record, System.currentTimeMillis());
    }

    @Override
    public ScrollResult<Record> getByScrollWithUserId(String userId, Long max, Long offset) {
        User user = userService.getById(userId);
        if(user == null) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "用户不存在"));
        }
        max = (max <= 0 ? System.currentTimeMillis() : max);
        Set<ZSetOperations.TypedTuple> typedTuples = redisTemplate.opsForZSet()
                                                                  .reverseRangeByScoreWithScores(RedisKey.RECORD_KEY + userId, 0, max, offset, 5);
        ScrollResult<Record> scrollResult = new ScrollResult<>();
        if(CollectionUtil.isEmpty(typedTuples)) {
            scrollResult.setData(Collections.emptyList());
            scrollResult.setHasData(false);
            return scrollResult;
        }
        long minTime = 0;
        List<Record> data = new ArrayList<>(typedTuples.size());
        for(ZSetOperations.TypedTuple typedTuple : typedTuples) {
            minTime = typedTuple.getScore().longValue();
            data.add((Record) typedTuple.getValue());
        }
        scrollResult.setData(data);
        scrollResult.setHasData(true);
        scrollResult.setMinTime(minTime);
        scrollResult.setOffset(1L);
        return scrollResult;
    }
}

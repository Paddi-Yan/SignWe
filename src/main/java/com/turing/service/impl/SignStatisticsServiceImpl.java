package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.ImmutableMap;
import com.turing.common.RedisKey;
import com.turing.entity.StatisticsInfo;
import com.turing.entity.User;
import com.turing.exception.RequestParamValidationException;
import com.turing.mapper.UserMapper;
import com.turing.service.SignStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月06日 21:44:19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignStatisticsServiceImpl implements SignStatisticsService {

    private final RedisTemplate redisTemplate;

    private final UserMapper userMapper;

    @Override
    public void count(String userId) {
        LocalDateTime now = LocalDateTime.now();
        //今日签到用户集合
        String key = RedisKey.DAY_STATISTICS_KEY + now.format(DateTimeFormatter.ofPattern("yyyy/MM/DD"));
        String monthKeySuffix = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        if(!checkSign(userId, key)) {
            redisTemplate.opsForSet().add(key, userId);
            redisTemplate.opsForValue()
                         .setBit(RedisKey.MONTH_STATISTICS_KEY + monthKeySuffix + ":" + userId, now.getDayOfMonth() - 1, true);
        }
    }

    private boolean checkSign(String userId, String key) {
        return redisTemplate.opsForSet().isMember(key, userId);
    }

    @Override
    public StatisticsInfo getSignStatistics(String userId) {
        User user = userMapper.selectById(userId);
        if(user == null) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "用户不存在"));
        }
        LocalDateTime now = LocalDateTime.now();
        String monthKeySuffix = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String key = RedisKey.MONTH_STATISTICS_KEY + monthKeySuffix + ":" + userId;
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = redisTemplate.opsForValue()
                                         .bitField(key, BitFieldSubCommands.create()
                                                                           .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                                                                           .valueAt(0));
        StatisticsInfo statisticsInfo = new StatisticsInfo();
        statisticsInfo.setUserId(userId);
        if(CollectionUtil.isEmpty(result)) {
            statisticsInfo.setKeepSignInDays(0);
            return statisticsInfo;
        }
        Long num = result.get(0);
        if(num == null || num == 0) {
            statisticsInfo.setKeepSignInDays(0);
            return statisticsInfo;
        }
        int keepSignInDays = getSignInDays(num);
        if(keepSignInDays != 0) {
            statisticsInfo.setKeepSignInDays(keepSignInDays);
            return statisticsInfo;
        } else {
            num >>>= 1;
        }
        int yesterdayKeepSignInDays = getSignInDays(num);
        statisticsInfo.setKeepSignInDays(yesterdayKeepSignInDays);
        return statisticsInfo;
    }

    private Integer getSignInDays(Long num) {
        int keepSignInDays = 0;
        while(true) {
            if((num & 1) == 0) {
                break;
            }
            keepSignInDays++;
            num >>>= 1;
        }
        return keepSignInDays;
    }
}

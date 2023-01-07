package com.turing.service.impl;

import com.turing.common.RedisKey;
import com.turing.service.SignStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Override
    public void count(String userId) {
        LocalDateTime now = LocalDateTime.now();
        //今日签到用户集合
        String key = RedisKey.DAY_STATISTICS_KEY + now.format(DateTimeFormatter.ofPattern("yyyy/MM/DD"));
        String monthKeySuffix = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        if(!redisTemplate.opsForSet().isMember(key, userId)) {
            redisTemplate.opsForSet().add(key, userId);
            redisTemplate.opsForValue()
                         .setBit(RedisKey.MONTH_STATISTICS_KEY + monthKeySuffix, now.getDayOfMonth() - 1, true);
        }
    }
}

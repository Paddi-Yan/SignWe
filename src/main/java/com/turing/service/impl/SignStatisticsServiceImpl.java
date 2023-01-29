package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
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
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月06日 21:44:19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignStatisticsServiceImpl implements SignStatisticsService {

    private final RedisTemplate<String, Serializable> redisTemplate;

    private final UserMapper userMapper;

    private final ThreadPoolTaskExecutor commonThreadPool;

    @Override
    public void count(String userId) {
        LocalDateTime now = LocalDateTime.now();
        //今日签到用户集合
        String key = RedisKey.DAY_STATISTICS_KEY + now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String monthKeySuffix = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        if(!checkSign(userId, key)) {
            redisTemplate.opsForSet().add(key, userId);
            redisTemplate.opsForValue()
                         .setBit(RedisKey.MONTH_STATISTICS_KEY + monthKeySuffix + ":" + userId, now.getDayOfMonth() - 1, true);
        }
    }

    private boolean checkSign(String userId, String key) {
        return BooleanUtil.isTrue(redisTemplate.opsForSet().isMember(key, userId));
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
        StatisticsInfo statisticsInfo = new StatisticsInfo();
        statisticsInfo.setUserId(userId);
        if(BooleanUtil.isFalse(redisTemplate.hasKey(key))) {
            statisticsInfo.setTotalSignInDays(0);
            statisticsInfo.setKeepSignInDays(0);
            return statisticsInfo;
        }
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = redisTemplate.opsForValue()
                                         .bitField(key, BitFieldSubCommands.create()
                                                                           .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                                                                           .valueAt(0));
        if(CollectionUtil.isEmpty(result) || result.get(0) == null || result.get(0) == 0) {
            statisticsInfo.setKeepSignInDays(0);
            statisticsInfo.setKeepSignInDays(0);
            return statisticsInfo;
        }
        Long signBit = result.get(0);
        //获取连续签到天数
        CompletableFuture<Integer> querySignInDaysFuture = CompletableFuture.supplyAsync(() -> getSignInDays(signBit), commonThreadPool);
        //获取本月签到天数
        CompletableFuture<Integer> queryTotalSignInDaysFuture = CompletableFuture.supplyAsync(() -> getTotalSignDays(key), commonThreadPool);
        //获取签到日历
        CompletableFuture<Map<String, Boolean>> queryCalendarFuture = CompletableFuture.supplyAsync(() -> getSignCalendar(signBit), commonThreadPool);
        CompletableFuture.allOf(querySignInDaysFuture, queryTotalSignInDaysFuture, queryCalendarFuture).join();
        statisticsInfo.setKeepSignInDays(querySignInDaysFuture.join());
        statisticsInfo.setTotalSignInDays(queryTotalSignInDaysFuture.join());
        statisticsInfo.setSignCalendar(queryCalendarFuture.join());
        return statisticsInfo;
    }

    private int getTotalSignDays(Long signBit) {
        int days = 0;
        while(signBit != 0) {
            signBit = signBit & (signBit - 1);
            days++;
        }
        return days;
    }

    private Map<String, Boolean> getSignCalendar(Long signBit) {
        LocalDate date = LocalDate.now();
        Map<String, Boolean> signMap = new LinkedHashMap<>(date.getDayOfMonth());
        Long num = signBit;
        for(int i = date.getDayOfMonth(); i > 0; i--) {
            LocalDate d = date.withDayOfMonth(i);
            signMap.put(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), num >> 1 << 1 != num);
            num >>= 1;
        }
        
        return signMap;
    }

    private int getTotalSignDays(String key) {
        Long result = redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes()));
        return result == null ? 0 : result.intValue();
    }

    @Override
    public void deleteYesterdayStatistics() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        String key = RedisKey.DAY_STATISTICS_KEY + yesterday.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        redisTemplate.delete(key);
    }

    @Override
    public void deleteLastMonthStatistics() {
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        String key = RedisKey.MONTH_STATISTICS_KEY + lastMonth.format(DateTimeFormatter.ofPattern("yyyy/MM")) + ":";
        Set<String> keys = redisTemplate.keys(key + "*");
        if(CollectionUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private Integer getSignInDays(Long signBit) {
        Long num = signBit;
        int keepSignInDays = 0;
        while((num & 1) != 0) {
            keepSignInDays++;
            num >>>= 1;
        }
        if(keepSignInDays != 0) {
            return keepSignInDays;
        }
        num >>>= 1;
        while((num & 1) != 0) {
            keepSignInDays++;
            num >>>= 1;
        }
        return keepSignInDays;
    }
}

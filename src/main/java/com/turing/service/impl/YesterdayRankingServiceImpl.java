package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaolyuh.annotation.CacheEvict;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.support.CacheMode;
import com.turing.common.RedisKey;
import com.turing.entity.Record;
import com.turing.entity.YesterdayRanking;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RecordService;
import com.turing.service.YesterdayRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class YesterdayRankingServiceImpl extends ServiceImpl<YesterdayRankingMapper, YesterdayRanking> implements YesterdayRankingService {

    @Resource
    private RecordService recordService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private YesterdayRankingMapper yesterdayRankingMapper;

    @Override
    public void generateYesterdayRanking() {
        //生成昨日排行榜之前需要对之前的数据进行清除
        deleteOldYesterdayRanking(RedisKey.TURING_TEAM);
        //获取昨天的所有学习记录
        List<Record> recordList = recordService.getYesterdaySignedRecordList();
        if(recordList != null && !recordList.isEmpty()) {
            log.info("开始结算[{}]学习排行榜", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd")));
            Map<String, Integer> yesterdayRanking = recordList.stream()
                                                              .collect(Collectors.toMap(Record :: getName, Record :: getStudyTime, (value1, value2) -> value1 + value2));
            HashSet<ZSetOperations.TypedTuple<String>> typedTuples = new HashSet<>(yesterdayRanking.size());
            for(Map.Entry<String, Integer> entry : yesterdayRanking.entrySet()) {
                log.info("今日学习记录=>[{}: {}minutes]", entry.getKey(), entry.getValue());
                yesterdayRankingMapper.insert(new YesterdayRanking(entry.getValue(), entry.getKey()));
                typedTuples.add(new DefaultTypedTuple(entry.getKey(), entry.getValue().doubleValue()));
            }
            redisTemplate.opsForZSet().add(RedisKey.YESTERDAY_RANKING_KEY, typedTuples);
            redisTemplate.expire(RedisKey.YESTERDAY_RANKING_KEY, 24, TimeUnit.HOURS);
        }
    }

    @CacheEvict(cacheNames = RedisKey.YESTERDAY_RANKING_KEY, key = "#id", cacheMode = CacheMode.FIRST)
    private void deleteOldYesterdayRanking(String id) {
        yesterdayRankingMapper.deleteAll();
    }

    @Override
    @Cacheable(cacheNames = RedisKey.YESTERDAY_RANKING_KEY, key = "#id", depict = "昨日学习排行榜缓存", cacheMode = CacheMode.FIRST,
            firstCache = @FirstCache(initialCapacity = 1, maximumSize = 1, expireTime = 15, timeUnit = TimeUnit.HOURS))
    public List<YesterdayRanking> getRanking(String id) {
        Set<ZSetOperations.TypedTuple> typedTuples = redisTemplate.opsForZSet()
                                                                  .reverseRangeWithScores(RedisKey.YESTERDAY_RANKING_KEY, 0, 4);
        if(CollectionUtil.isEmpty(typedTuples)) {
            return Collections.emptyList();
        }
        List<YesterdayRanking> result = typedTuples
                .stream().map(typedTuple
                        -> new YesterdayRanking(typedTuple.getScore().intValue(),
                        String.valueOf(typedTuple.getValue())))
                .collect(Collectors.toList());
        if(CollectionUtil.isEmpty(result)) {
            result = baseMapper.getRanking();
        }
        return result;
    }
}

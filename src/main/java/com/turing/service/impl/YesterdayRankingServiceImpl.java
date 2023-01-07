package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaolyuh.annotation.Cacheable;
import com.github.xiaolyuh.annotation.FirstCache;
import com.github.xiaolyuh.annotation.SecondaryCache;
import com.github.xiaolyuh.support.CacheMode;
import com.turing.common.RedisKey;
import com.turing.entity.Record;
import com.turing.entity.YesterdayRanking;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RecordService;
import com.turing.service.YesterdayRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        deleteOldYesterdayRanking();
        //获取昨天的所有学习记录
        List<Record> recordList = recordService.getYesterdaySignedRecordList();
        if(recordList != null && !recordList.isEmpty()) {
            log.info("开始结算[{}]学习排行榜", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd")));
            Map<String, Integer> yesterdayRanking = recordList.stream()
                                                              .collect(Collectors.toMap(Record :: getName, Record :: getStudyTime, (value1, value2) -> value1 + value2));
            for(Map.Entry<String, Integer> entry : yesterdayRanking.entrySet()) {
                log.info("今日学习记录=>[{}: {}minutes]", entry.getKey(), entry.getValue());
                yesterdayRankingMapper.insert(new YesterdayRanking(entry.getValue(), entry.getKey()));
                redisTemplate.opsForZSet()
                             .incrementScore(RedisKey.YESTERDAY_RANKING_KEY, entry.getKey(), entry.getValue());
            }
        }
    }

    private void deleteOldYesterdayRanking() {
        redisTemplate.delete(RedisKey.YESTERDAY_RANKING_KEY);
        yesterdayRankingMapper.deleteAll();
    }

    @Override
    @Cacheable(cacheNames = RedisKey.YESTERDAY_RANKING_KEY, key = "#id", depict = "昨日学习排行榜缓存", cacheMode = CacheMode.ALL,
            firstCache = @FirstCache(initialCapacity = 1, maximumSize = 1, expireTime = 15, timeUnit = TimeUnit.HOURS),
            secondaryCache = @SecondaryCache(expireTime = 15, timeUnit = TimeUnit.HOURS, forceRefresh = true))
    public List<YesterdayRanking> getRanking(String id) {
        Set<ZSetOperations.TypedTuple> typedTuples = redisTemplate.opsForZSet()
                                                                  .reverseRangeWithScores(RedisKey.YESTERDAY_RANKING_KEY, 0, 4);
        if(CollectionUtil.isEmpty(typedTuples)) {
            return Collections.emptyList();
        }
        List<YesterdayRanking> result = typedTuples.stream()
                                                   .map(typedTuple -> new YesterdayRanking(typedTuple.getScore()
                                                                                                     .intValue(), String.valueOf(typedTuple.getValue())))
                                                   .collect(Collectors.toList());
        if(CollectionUtil.isEmpty(result)) {
            result = baseMapper.selectList(null);
        }
        return result;
    }
}

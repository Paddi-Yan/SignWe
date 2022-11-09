package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.common.RedisKey;
import com.turing.entity.Record;
import com.turing.entity.YesterdayRanking;
import com.turing.mapper.RecordMapper;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RecordService;
import com.turing.service.YesterdayRankingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
        redisTemplate.delete(RedisKey.YESTERDAY_RANKING_ZSET_KEY);
        yesterdayRankingMapper.deleteAll();
        //获取昨天的所有学习记录
        List<Record> recordList = recordService.getYesterdayRecord();
        HashMap<String, Integer> yesterdayRanking = new HashMap<>();
        log.info("生成昨日学习排行榜中....");
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                for(Record record : recordList) {
                    redisOperations.opsForZSet().incrementScore(RedisKey.YESTERDAY_RANKING_ZSET_KEY, record.getName(), record.getStudyTime());
                    yesterdayRanking.put(record.getName(), yesterdayRanking.getOrDefault(record.getName(), 0) + record.getStudyTime());
                }
                return redisOperations.exec();
            }
        });
        for(String name : yesterdayRanking.keySet()) {
            YesterdayRanking entity = new YesterdayRanking();
            entity.setUsername(name);
            entity.setStudyTime(yesterdayRanking.get(name));
            yesterdayRankingMapper.insert(entity);
            log.info("姓名: " + name + "| 学习时长: "+ yesterdayRanking.get(name) + "minutes" );
        }
        log.info("昨日学习排行榜生成成功!");
    }

    @Override
    public LinkedList<YesterdayRanking> getRanking() {
        Set<String> nameList = redisTemplate.opsForZSet().range(RedisKey.YESTERDAY_RANKING_ZSET_KEY, 0, -1);
        LinkedList<YesterdayRanking> result = new LinkedList<>();
        for(String name : nameList) {
            int studyTime = redisTemplate.opsForZSet().score(RedisKey.YESTERDAY_RANKING_ZSET_KEY, name).intValue();
            YesterdayRanking yesterdayRanking = new YesterdayRanking(studyTime, name);
            result.addFirst(yesterdayRanking);
        }
        if(result.isEmpty()) {
            List<YesterdayRanking> rankings = yesterdayRankingMapper.selectList(new QueryWrapper<YesterdayRanking>().orderByDesc("study_time"));
            result.addAll(rankings);
            redisTemplate.execute(new SessionCallback() {
                @Override
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisOperations.multi();
                    for(YesterdayRanking ranking : rankings) {
                        redisOperations.opsForZSet().add(RedisKey.YESTERDAY_RANKING_ZSET_KEY, ranking.getUsername(), ranking.getStudyTime());
                    }
                    return redisOperations.exec();
                }
            });
        }
        return result;
    }
}

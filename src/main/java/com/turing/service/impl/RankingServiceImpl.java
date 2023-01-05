package com.turing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.turing.common.RedisKey;
import com.turing.entity.Ranking;
import com.turing.mapper.RankingMapper;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
public class RankingServiceImpl extends ServiceImpl<RankingMapper, Ranking> implements RankingService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RankingMapper rankingMapper;

    @Resource
    private YesterdayRankingMapper yesterdayRankingMapper;

    @Override
    public List<Ranking> getRanking() {
        Set<String> userIdsOfRanking = redisTemplate.opsForZSet().reverseRange(RedisKey.RANKING_ZSET_KEY, 0, -1);
        if(userIdsOfRanking == null || userIdsOfRanking.isEmpty()) {
            return Collections.emptyList();
        }
        List<Ranking> result = userIdsOfRanking.stream()
                                               .map(userId -> CompletableFuture.supplyAsync(() -> (Ranking) redisTemplate.opsForHash()
                                                                                                                         .get(RedisKey.RANKING_HASH_KEY, userId)))
                                               .collect(Collectors.toList())
                                               .stream()
                                               .map(future -> future.join())
                                               .collect(Collectors.toList());
        /*for(String userId : userIdsOfRanking) {
            Ranking entity = (Ranking) redisTemplate.opsForHash().get(RedisKey.RANKING_HASH_KEY, userId);
            result.add(entity);
        }*/
        if(!result.isEmpty()) {
            return result;
        }
        result = rankingMapper.getRanking();
        if(result == null || result.isEmpty()) {
            return null;
        }
        List<Ranking> finalResult = result;
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                for(Ranking entity : finalResult) {
                    String fieldKey = RedisKey.RANKING_FIELD_KEY + entity.getId();
                    redisOperations.opsForHash().put(RedisKey.RANKING_HASH_KEY, fieldKey, entity);
                    redisOperations.opsForZSet().add(RedisKey.RANKING_ZSET_KEY, fieldKey, entity.getTotalTime());
                }
                return redisOperations.exec();
            }
        });
        return result;
    }

    /**
     * 清除排名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetRanking() {
        //Ranking-删除总排名和昨日排名
        ArrayList<String> deleteKeys = new ArrayList<>();
        deleteKeys.add(RedisKey.RANKING_HASH_KEY);
        deleteKeys.add(RedisKey.RANKING_ZSET_KEY);
        deleteKeys.add(RedisKey.YESTERDAY_RANKING_KEY);
        redisTemplate.delete(deleteKeys);
        rankingMapper.deleteAll();
        yesterdayRankingMapper.deleteAll();
    }
}

package com.turing.service.impl;

import com.turing.common.RedisKey;
import com.turing.entity.Ranking;
import com.turing.mapper.RankingMapper;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RankingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.krb5.internal.KRBCred;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class RankingServiceImpl extends ServiceImpl<RankingMapper, Ranking> implements RankingService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RankingMapper rankingMapper;

    @Resource
    private YesterdayRankingMapper yesterdayRankingMapper;

    @Override
    public List<Ranking> getRanking() {
        List<Ranking> ranking = new ArrayList<>();
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                Set<String> userIdsOfRanking = redisOperations.opsForZSet().range(RedisKey.RANKING_ZSET_KEY, 0, -1);
                for(String userId : userIdsOfRanking) {
                    Ranking entity = (Ranking) redisOperations.opsForHash().get(RedisKey.RANKING_HASH_KEY, RedisKey.RANKING_FIELD_KEY+userId);
                    ranking.add(entity);
                }
                return redisOperations.exec();
            }
        });
        if(!ranking.isEmpty()) {
            return ranking;
        }
        List<Ranking> result = rankingMapper.getRanking();
        if(result == null || result.isEmpty()) {
            return null;
        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                for(Ranking entity : result) {
                    String fieldKey = RedisKey.RANKING_FIELD_KEY + entity.getId();
                    redisOperations.opsForHash().put(RedisKey.RANKING_HASH_KEY, fieldKey, entity);
                    redisOperations.opsForZSet().add(RedisKey.RANKING_ZSET_KEY, fieldKey, entity.getTotalTime());
                }
                return redisOperations.exec();
            }
        });
        return ranking;
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
        deleteKeys.add(RedisKey.YESTERDAY_RANKING_ZSET_KEY);
        redisTemplate.delete(deleteKeys);
        rankingMapper.deleteAll();
        yesterdayRankingMapper.deleteAll();
    }
}

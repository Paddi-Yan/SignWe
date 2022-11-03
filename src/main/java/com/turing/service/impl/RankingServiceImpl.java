package com.turing.service.impl;

import com.turing.common.RedisKey;
import com.turing.entity.Ranking;
import com.turing.mapper.RankingMapper;
import com.turing.service.RankingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
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

    @Override
    public List<Ranking> getRanking() {
        List<Ranking> ranking = new ArrayList<>();
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                int size = redisOperations.opsForZSet().size(RedisKey.RANKING_ZSET_KEY).intValue();
                Set<String> userIdsOfRanking = redisOperations.opsForZSet().range(RedisKey.RANKING_ZSET_KEY, 0, size - 1);
                for(String userId : userIdsOfRanking) {
                    Ranking entity = (Ranking) redisOperations.opsForHash().get(RedisKey.RANKING_HASH_KEY, userId);
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
}

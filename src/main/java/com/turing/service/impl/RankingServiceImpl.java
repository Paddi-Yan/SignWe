package com.turing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.turing.common.RedisKey;
import com.turing.entity.Ranking;
import com.turing.entity.User;
import com.turing.mapper.RankingMapper;
import com.turing.mapper.YesterdayRankingMapper;
import com.turing.service.RankingService;
import com.turing.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
@RequiredArgsConstructor
public class RankingServiceImpl extends ServiceImpl<RankingMapper, Ranking> implements RankingService {

    private final RedisTemplate redisTemplate;
    private final RankingMapper rankingMapper;
    private final YesterdayRankingMapper yesterdayRankingMapper;
    private final UserService userService;
    private final ExecutorService cacheThreadPool;

    @Override
    public List<Ranking> getRanking() {
        Set<ZSetOperations.TypedTuple> typedTuples = redisTemplate.opsForZSet()
                                                                  .reverseRangeWithScores(RedisKey.TOTAL_RANKING_KEY, 0, -1);
        List<Ranking> result = null;
        if(CollectionUtil.isNotEmpty(typedTuples)) {
            result = typedTuples.stream().map(typedTuple -> {
                String openid = typedTuple.getValue().toString();
                int studyTime = typedTuple.getScore().intValue();
                User user = userService.getByOpenId(openid);
                Ranking ranking = new Ranking();
                ranking.setName(user.getName());
                ranking.setFinalChair(user.getFinalChair());
                ranking.setCount(user.getTodayCount());
                ranking.setTotalTime(studyTime);
                ranking.setStatus(user.getStatus());
                ranking.setOpenid(user.getOpenid());
                ranking.setId(user.getId());
                return ranking;
            }).collect(Collectors.toList());
            return result;
        }
        result = rankingMapper.getRanking();
        if(CollectionUtil.isNotEmpty(result)) {
            List<Ranking> finalResult = result;
            cacheThreadPool.execute(() -> {
                HashSet<ZSetOperations.TypedTuple<String>> newTypedTuples = new HashSet<>(finalResult.size());
                finalResult.stream().forEach(ranking -> {
                    DefaultTypedTuple<String> typedTuple = new DefaultTypedTuple<>(ranking.getOpenid(), Double.valueOf(ranking.getTotalTime()));
                    newTypedTuples.add(typedTuple);
                });
                redisTemplate.opsForZSet().add(RedisKey.TOTAL_RANKING_KEY, newTypedTuples);
            });
        }
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
        deleteKeys.add(RedisKey.TOTAL_RANKING_KEY);
        deleteKeys.add(RedisKey.YESTERDAY_RANKING_KEY);
        redisTemplate.delete(deleteKeys);
        rankingMapper.deleteAll();
        yesterdayRankingMapper.deleteAll();
    }

    @Override
    public void updateRanking(String id, String openid, Integer studyTime) {
        Ranking ranking = getById(id);
        ranking.setTotalTime(ranking.getTotalTime() + studyTime);
        updateById(ranking);
        redisTemplate.opsForZSet().incrementScore(RedisKey.TOTAL_RANKING_KEY, openid, studyTime);
    }
}

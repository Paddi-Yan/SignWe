package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.turing.common.RedisKey;
import com.turing.entity.*;
import com.turing.entity.dto.SignDto;
import com.turing.entity.dto.SignOutDto;
import com.turing.mapper.*;
import com.turing.service.ChairsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
public class ChairsServiceImpl extends ServiceImpl<ChairsMapper, Chairs> implements ChairsService {

    @Resource
    private ChairsMapper chairsMapper;

    @Resource
    private RankingMapper rankingMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RecordMapper recordMapper;

    @Override
    public List<Chairs> getChairsList() {
        int size = redisTemplate.opsForList().size(RedisKey.CHAIRS_LIST_KEY).intValue();
        List<Chairs> chairsList = redisTemplate.opsForList().range(RedisKey.CHAIRS_LIST_KEY, 0, size-1);
        if(chairsList != null && !chairsList.isEmpty()) {
            return chairsList;
        }
        chairsList = chairsMapper.selectList(null);
        redisTemplate.opsForList().rightPushAll(RedisKey.CHAIRS_LIST_KEY, chairsList);
        return chairsList;
    }


    @Override
    public Chairs getById(Integer id) {

        Chairs chair = (Chairs) redisTemplate.opsForList().index(RedisKey.CHAIRS_LIST_KEY, id-1);
        if(chair == null) {
            chair = chairsMapper.selectById(id);
            if(chair != null) {
                redisTemplate.opsForList().set(RedisKey.CHAIRS_LIST_KEY, id-1, chair);
            }
        }
        return chair;
    }

    /**
     * !!!!!!!需要加锁!!!!!!!
     */
    //@SneakyThrows 不知道怎么用这个注解的屑
    @Override
    public Chairs signIn(Chairs chair, SignDto signDto, User user) throws Exception {
        //使用乐观锁
        //修改座位信息
        chair.setOpenId(signDto.getOpenid());
        chair.setIsEmpty(false);
        chair.setLastUsedName(user.getName());
        int signInResult = chairsMapper.updateById(chair);
        if(signInResult == 0) {
            throw new Exception("签到占座失败,可能已经被其他人先坐下,请稍后重试或换一个位置签到!");
        }
        redisTemplate.opsForList().set(RedisKey.CHAIRS_LIST_KEY, chair.getId()-1, chair);
        //修改用户签到信息
        user.setFinalChair(chair.getId());
        user.setFinalDistance(signDto.getDistance());
        user.setFinalStartTime(LocalDateTime.now());
        user.setStatus(User.SIGN_IN);
        userMapper.updateById(user);
        //修改排行榜上的学习状态
        Ranking userOfRanking = (Ranking) redisTemplate.opsForHash()
                                                       .get(RedisKey.RANKING_HASH_KEY, RedisKey.RANKING_FIELD_KEY + user.getId());
        if(userOfRanking == null) {
            userOfRanking = rankingMapper.selectOne(new QueryWrapper<Ranking>().eq("id", user.getId()));
        }
        if(userOfRanking != null) {
            userOfRanking.setStatus(User.SIGN_IN);
            redisTemplate.opsForHash()
                         .put(RedisKey.RANKING_HASH_KEY, RedisKey.RANKING_FIELD_KEY + user.getId(), userOfRanking);
            rankingMapper.updateById(userOfRanking);
        }
        return chair;
    }

    @Override
    public Chairs signOut(SignOutDto signOutDto, Boolean autoSignOut) throws Exception {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", signOutDto.getOpenid()));
        Chairs chair = chairsMapper.selectById(signOutDto.getChairId());
        //校验签到状态
        checkSignInStatus(signOutDto, user, chair);
        //更新座位信息
        updateChairStatus(user, chair);
        //更新用户信息
        user.setStatus(User.SIGN_OUT);
        LocalDateTime signInTime = user.getFinalStartTime();
        LocalDateTime signOutTime = LocalDateTime.now();
        Integer studyTime = Math.toIntExact(signInTime.until(signOutTime, ChronoUnit.MINUTES));
        //如果是自动签到那么将该用户的当天学习时长设置为0  ----->   偷个懒^_^
        if(autoSignOut == false) {
            user.setTodayTime(user.getTodayTime() + studyTime);
        } else {
            user.setTodayTime(0);
        }
        user.setTotalTime(user.getTotalTime() + studyTime);
        userMapper.updateById(user);
        //添加学习记录
        Record record = new Record();
        record.setChair(chair.getId());
        record.setDistance(user.getFinalDistance());
        record.setName(user.getName());
        record.setClassname(user.getClassname());
        record.setFinalStartTime(signInTime);
        record.setFinalStopTime(signOutTime);
        record.setStudyTime(studyTime);
        int resultOfInsRecord = recordMapper.insert(record);
        if(resultOfInsRecord == 1) {
            log.info("记录学习记录:" + record);
        }
        redisTemplate.opsForList().rightPush(RedisKey.RECORD_KEY + record.getClassname() + record.getName(), record);
        //更新总排行榜
        Ranking ranking = (Ranking) redisTemplate.opsForHash()
                                                 .get(RedisKey.RANKING_HASH_KEY, RedisKey.RANKING_FIELD_KEY + user.getId());
        if(ranking == null) {
            ranking = rankingMapper.selectById(user.getId());
        }
        //1.之前没有学习记录,排行榜没有该用户
        if(ranking == null) {
            ranking = new Ranking(user.getName(), chair.getId(), 1, user.getTotalTime(), User.SIGN_OUT, user.getId());
            rankingMapper.insert(ranking);
            final Ranking finalRanking = ranking;
            redisTemplate.execute(new SessionCallback() {
                @Override
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisOperations.multi();
                    redisOperations.opsForHash().put(RedisKey.RANKING_HASH_KEY,
                            RedisKey.RANKING_FIELD_KEY + user.getId(),
                            finalRanking);
                    redisOperations.opsForZSet().add(RedisKey.RANKING_ZSET_KEY,
                            RedisKey.RANKING_FIELD_KEY + user.getId(),
                            finalRanking.getTotalTime());
                    return redisOperations.exec();
                }
            });
        } else {
            //2.有学习记录,排行榜有该用户
            ranking.setFinalChair(chair.getId());
            ranking.setCount(ranking.getCount() + 1);
            ranking.setTotalTime(user.getTotalTime());
            ranking.setStatus(User.SIGN_OUT);
            rankingMapper.updateById(ranking);
            Ranking finalRankingTwo = ranking;
            redisTemplate.execute(new SessionCallback() {
                @Override
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisOperations.multi();
                    redisOperations.opsForHash().put(RedisKey.RANKING_HASH_KEY,
                            RedisKey.RANKING_FIELD_KEY + user.getId(),
                            finalRankingTwo);
                    redisOperations.opsForZSet().incrementScore(RedisKey.RANKING_ZSET_KEY,
                            RedisKey.RANKING_FIELD_KEY + user.getId(),
                            studyTime);
                    return redisOperations.exec();
                }
            });
        }
        return chair;
    }

    private void updateChairStatus(User user, Chairs chair) {
        chair.setIsEmpty(true);
        redisTemplate.opsForList().set(RedisKey.CHAIRS_LIST_KEY, chair.getId()-1, chair);
        int resultOfSignOut = chairsMapper.updateById(chair);
        if(resultOfSignOut == 1) {
            log.info("用户[" + user.getClassname() + user.getName() + "]签退成功!");
            log.info("当前座位信息:" + chair);
        } else {
            log.error("用户" + user.getClassname() + user.getName() + "签退失败!");
            throw new RuntimeException("签退失败,请重试或联系管理员!");
        }
    }

    private static void checkSignInStatus(SignOutDto signOutDto, User user, Chairs chair) throws Exception {
        if(user == null || chair == null) {
            throw new Exception("携带参数有误");
        }
        if(!User.SIGN_IN.equals(user.getStatus())) {
            throw new Exception("当前状态不可签退");
        }
        if(chair.getIsEmpty() || !signOutDto.getOpenid().equals(chair.getOpenId())) {
            throw new Exception("该位置未坐下,无法进行签退操作或当前签退者与签到者信息不符,无法签退");
        }
    }

    /**
     * 强制签退并且不记录时长
     *
     * @param signOutDto
     * @return
     */
    @Override
    public Chairs signOutForce(SignOutDto signOutDto) throws Exception {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", signOutDto.getOpenid()));
        Chairs chair = chairsMapper.selectById(signOutDto.getChairId());
        //校验签到状态
        checkSignInStatus(signOutDto, user, chair);
        //更新座位信息
        updateChairStatus(user, chair);
        //更新用户信息
        user.setStatus(User.SIGN_OUT);
        userMapper.updateById(user);
        return chair;
    }

}
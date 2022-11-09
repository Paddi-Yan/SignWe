package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.turing.common.HttpStatusCode;
import com.turing.common.RedisKey;
import com.turing.common.Result;
import com.turing.entity.*;
import com.turing.entity.vo.SignVo;
import com.turing.entity.vo.SignOutVo;
import com.turing.exception.InternalServerException;
import com.turing.exception.RequestParamValidationException;
import com.turing.exception.ResourceNotFoundException;
import com.turing.mapper.*;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
import com.turing.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
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
    private DoorService doorService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private ChairsService chairsService;

    @Override
    public List<Chairs> getChairsList() {
        List<Chairs> chairsList = redisTemplate.opsForHash().values(RedisKey.CHAIRS_HASH_KEY);
        if(chairsList != null && !chairsList.isEmpty()) {
            chairsList.sort(Comparator.comparingInt(Chairs::getId));
            return chairsList;
        }

        chairsList = chairsMapper.selectList(null);
        HashMap<String, Chairs> map = new HashMap<>();
        for(Chairs chairs : chairsList) {
            map.put(RedisKey.CHAIRS_FIELD_KEY+chairs.getId(), chairs);
        }
        redisTemplate.opsForHash().putAll(RedisKey.CHAIRS_HASH_KEY, map);
        return chairsList;
    }


    @Override
    public Chairs getById(Integer id) {

        Chairs chair = (Chairs) redisTemplate.opsForHash().get(RedisKey.CHAIRS_HASH_KEY, RedisKey.CHAIRS_FIELD_KEY+id);
        if(chair == null) {
            chair = chairsMapper.selectById(id);
            if(chair != null) {
                redisTemplate.opsForHash().put(RedisKey.CHAIRS_HASH_KEY, RedisKey.CHAIRS_FIELD_KEY+id, chair);
            }
        }
        return chair;
    }

    /**
     * !!!!!!!需要加锁!!!!!!!
     */
    //@SneakyThrows 不知道怎么用这个注解的屑
    @Override
    public Chairs signIn(SignVo signVo) throws Exception {
        Door door = doorService.getDoorStatus();
        if(!door.getOpen()) {
            throw new RequestParamValidationException(ImmutableMap.of("doorStatus", door.getOpen()));
        }
        User user = userService.getByOpenId(signVo.getOpenid());
        if(user == null) {
            throw new ResourceNotFoundException(ImmutableMap.of("openid",signVo.getOpenid()));
        }
        if(User.SIGN_IN.equals(user.getStatus())) {
            throw new RequestParamValidationException(ImmutableMap.of("userStatus", user.getStatus()));
        }
        Chairs chair = chairsService.getById(signVo.getChairId());
        if(chair == null) {
            throw new RequestParamValidationException(ImmutableMap.of("chairId",signVo.getChairId()));
        }
        if(!chair.getIsEmpty()) {
            throw new RequestParamValidationException(ImmutableMap.of("chairStatus",chair.getIsEmpty()));
        }

        //使用乐观锁
        //修改座位信息
        chair.setOpenId(signVo.getOpenid());
        chair.setIsEmpty(false);
        chair.setLastUsedName(user.getName());
        int signInResult = chairsMapper.updateById(chair);
        if(signInResult == 0) {
            throw new RequestParamValidationException(ImmutableMap.of("case","签到占座失败,可能已经被其他人先坐下,请稍后重试或换一个位置签到!"));
        }
        redisTemplate.opsForHash().put(RedisKey.CHAIRS_HASH_KEY, RedisKey.CHAIRS_FIELD_KEY+chair.getId(), chair);
        //修改用户签到信息
        user.setFinalChair(chair.getId());
        user.setFinalDistance(signVo.getDistance());
        user.setFinalStartTime(LocalDateTime.now());
        user.setStatus(User.SIGN_IN);
        userService.update(user);
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
    public Chairs signOut(SignOutVo signOutVo) throws Exception {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", signOutVo.getOpenid()));
        Chairs chair = chairsMapper.selectById(signOutVo.getChairId());
        //校验签到状态
        checkSignInStatus(signOutVo, user, chair);
        //更新座位信息
        updateChairStatus(user, chair);
        //更新用户信息
        user.setStatus(User.SIGN_OUT);
        LocalDateTime signInTime = user.getFinalStartTime();
        LocalDateTime signOutTime = LocalDateTime.now();
        Integer studyTime = Math.toIntExact(signInTime.until(signOutTime, ChronoUnit.MINUTES));
        user.setTodayTime(user.getTodayTime() + studyTime);
        user.setTodayCount(user.getTodayCount() + 1);
        user.setTotalTime(user.getTotalTime() + studyTime);
        userService.update(user);
        //添加学习记录
        Record record = new Record();
        record.setChair(chair.getId());
        record.setDistance(user.getFinalDistance());
        record.setOpenid(user.getOpenid());
        record.setName(user.getName());
        record.setClassname(user.getClassname());
        record.setFinalStartTime(signInTime);
        record.setFinalStopTime(signOutTime);
        record.setStudyTime(studyTime);
        record.setDeleted(0);
        int resultOfInsRecord = recordMapper.insert(record);
        if(resultOfInsRecord == 1) {
            log.info("记录学习记录:" + record);
        }
        redisTemplate.opsForList().leftPush(RedisKey.RECORD_KEY +user.getOpenid(), record);
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
        log.info("签退前座位信息:" + chair);
        int resultOfSignOut = chairsMapper.updateById(chair);

        if(resultOfSignOut == 1) {
            log.info("用户[" + user.getClassname() + user.getName() + "]签退成功!");
            log.info("签退后座位信息:" + chair);
            redisTemplate.opsForHash().put(RedisKey.CHAIRS_HASH_KEY, RedisKey.CHAIRS_FIELD_KEY+chair.getId(), chair);
        } else {
            log.error("用户" + user.getClassname() + user.getName() + "签退失败!");
            throw new InternalServerException();
        }
    }

    private static void checkSignInStatus(SignOutVo signOutVo, User user, Chairs chair) throws Exception {
        if(user == null || chair == null) {
            throw new RequestParamValidationException(ImmutableMap.of("openid:",user.getOpenid(),"chairId",chair.getId()));
        }
        if(!User.SIGN_IN.equals(user.getStatus())) {
            throw new RequestParamValidationException(ImmutableMap.of("userStatus", user.getStatus()));
        }
        if(chair.getIsEmpty() || !signOutVo.getOpenid().equals(chair.getOpenId())) {
            throw new RequestParamValidationException(ImmutableMap.of("chairStatus", chair.getIsEmpty(),
                    "chairUserId",chair.getId(),
                    "userUseChairId",user.getFinalChair()));
        }
    }

    /**
     * 强制签退并且不记录时长
     *
     * @param signOutVo
     * @return
     */
    @Override
    public Chairs signOutForce(SignOutVo signOutVo) throws Exception {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", signOutVo.getOpenid()));
        Chairs chair = chairsMapper.selectById(signOutVo.getChairId());
        //校验签到状态
        checkSignInStatus(signOutVo, user, chair);
        //更新座位信息
        updateChairStatus(user, chair);
        //更新用户信息
        user.setStatus(User.SIGN_OUT);
        userService.update(user);
        return chair;
    }

}
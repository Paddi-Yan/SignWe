package com.turing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.turing.anotation.RedisLock;
import com.turing.common.RedisKey;
import com.turing.entity.*;
import com.turing.entity.vo.SignOutVo;
import com.turing.entity.vo.SignVo;
import com.turing.exception.InternalServerException;
import com.turing.exception.RequestParamValidationException;
import com.turing.exception.ResourceNotFoundException;
import com.turing.mapper.ChairsMapper;
import com.turing.mapper.RankingMapper;
import com.turing.mapper.RecordMapper;
import com.turing.mapper.UserMapper;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
import com.turing.service.UserService;
import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.turing.common.RedisKey.TURING_TEAM;

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
public class ChairsServiceImpl extends ServiceImpl<ChairsMapper, Chairs> implements ChairsService {

    private final ChairsMapper chairsMapper;
    private final RankingMapper rankingMapper;
    private final UserMapper userMapper;
    private final DoorService doorService;
    private final RedisTemplate redisTemplate;
    private final UserService userService;
    private final RecordMapper recordMapper;

    private final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newCachedThreadPool(ThreadFactoryBuilder.create()
                                                                                                             .setNameFormat("CacheRebuildTask-%s")
                                                                                                             .get());

    private static final ExecutorService INFO_UPDATE_EXECUTOR = new ThreadPoolExecutor(3, 10, 10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(1024),
            ThreadFactoryBuilder.create().setNameFormat("InfoUpdateTask-%s").get());

    private static final ExecutorService QUERY_INFO_EXECUTOR = Executors.newCachedThreadPool(ThreadFactoryBuilder.create()
                                                                                                                 .setNameFormat("InfoQueryTask-%s")
                                                                                                                 .get());

    @Override
    public List<Chairs> getChairsList() {
        Set<String> keys = redisTemplate.keys(RedisKey.CHAIRS_HASH_KEY + "*");
        List<Chairs> chairsList = new ArrayList<>(keys.size());
        CompletableFuture.allOf(keys.stream().map(key -> CompletableFuture.runAsync(() -> {
            Map entries = redisTemplate.opsForHash().entries(key);
            Chairs chairs = BeanUtil.mapToBean(entries, Chairs.class, false);
            chairsList.add(chairs);
        }, QUERY_INFO_EXECUTOR)).collect(Collectors.toList()).toArray(new CompletableFuture[0])).join();
        if(chairsList != null && !chairsList.isEmpty()) {
            chairsList.sort(Comparator.comparingInt(Chairs :: getId));
            return chairsList;
        }
        List<Chairs> chairs = chairsMapper.selectList(null);
        if(chairs == null || chairs.isEmpty()) {
            return Collections.emptyList();
        }
        CACHE_REBUILD_EXECUTOR.execute(() -> chairs.forEach(chair -> {
            log.info("重建缓存:{}", chair);
            Map<String, Object> entry = BeanUtil.beanToMap(chair, new HashMap<>(), false, false);
            redisTemplate.opsForHash().putAll(RedisKey.CHAIRS_HASH_KEY + chair.getId(), entry);
        }));
        return chairs;
    }


    @Override
    public Chairs getById(Integer id) {
        Map entries = redisTemplate.opsForHash().entries(RedisKey.CHAIRS_HASH_KEY + id);
        if(MapUtil.isEmpty(entries)) {
            Chairs chair = baseMapper.selectById(id);
            if(chair == null) {
                return null;
            }
            CACHE_REBUILD_EXECUTOR.execute(() -> {
                List<Chairs> chairs = getBaseMapper().selectList(null);
                if(chairs == null || chairs.isEmpty()) {
                    return;
                }
                chairs.forEach(chairInfo -> {
                    log.info("重建缓存:{}", chairInfo);
                    Map<String, Object> entry = BeanUtil.beanToMap(chairInfo, new HashMap<>(), false, false);
                    redisTemplate.opsForHash().putAll(RedisKey.CHAIRS_HASH_KEY + chairInfo.getId(), entry);
                });
            });
            return chair;
        }
        Chairs chair = BeanUtil.mapToBean(entries, Chairs.class, false);
        return chair;
    }

    @Override
    @RedisLock(lockName = "signIn", key = "#signVo.openid")
    @Transactional(rollbackFor = Exception.class)
    public Chairs signIn(SignVo signVo) throws Exception {
        CompletableFuture<Door> validFuture1 = CompletableFuture.supplyAsync(() -> {
            Door door = doorService.getDoorStatus(TURING_TEAM);
            log.info("查询到开门状态信息: {}", door);
            return door;
        }, QUERY_INFO_EXECUTOR);
        CompletableFuture<User> validFuture2 = CompletableFuture.supplyAsync(() -> {
            User user = userService.getByOpenId(signVo.getOpenid());
            log.info("查询到用户信息: {}", user);
            return user;
        }, QUERY_INFO_EXECUTOR);
        CompletableFuture<Chairs> validFuture3 = CompletableFuture.supplyAsync(() -> {
            Chairs chair = getById(signVo.getChairId());
            log.info("查询到座位信息: {}", chair);
            return chair;
        }, QUERY_INFO_EXECUTOR);
        CompletableFuture<ImmutableMap<String, Object>> validAllFuture
                = CompletableFuture.allOf(validFuture1, validFuture2, validFuture3)
                                   .thenApply(v -> ImmutableMap.of(
                                           "door", validFuture1.join(),
                                           "user", validFuture2.join(),
                                           "chair", validFuture3.join()));
        ImmutableMap<String, Object> map = validAllFuture.join();
        Door door = (Door) map.get("door");
        User user = (User) map.get("user");
        Chairs chair = (Chairs) map.get("chair");
        if(!door.getOpen()) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "请开门后再进行签到"));
        }
        if(user == null) {
            throw new ResourceNotFoundException(ImmutableMap.of("cause", "用户不存在", "openid", signVo.getOpenid()));
        }
        if(User.SIGN_IN.equals(user.getStatus())) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "已处于签到状态,无法再次签到", "userStatus", user.getStatus()));
        }
        if(chair == null) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "无法匹配到对应签到位置", "chairId", signVo.getChairId()));
        }
        if(!chair.getIsEmpty()) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "当前位置已经被占用", "chairStatus", chair.getIsEmpty()));
        }
        //使用乐观锁
        //修改座位信息
        chair.setOpenId(signVo.getOpenid());
        chair.setIsEmpty(false);
        chair.setLastUsedName(user.getName());
        int signInResult = chairsMapper.updateById(chair);
        if(signInResult == 0) {
            throw new RequestParamValidationException(ImmutableMap.of("case", "签到占座失败,可能已经被其他人先坐下,请稍后重试或换一个位置签到!"));
        }
        INFO_UPDATE_EXECUTOR.submit(() -> {
            redisTemplate.opsForHash()
                         .putAll(RedisKey.CHAIRS_HASH_KEY + chair.getId(), BeanUtil.beanToMap(chair, false, false));
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
        });

        log.info("签到信息: {}", chair);
        return chair;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Chairs signOut(SignOutVo signOutVo) throws Exception {
        CompletableFuture<User> queryUserFuture = CompletableFuture.supplyAsync(() -> {
            User result = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User :: getOpenid, signOutVo.getOpenid()));
            log.info("查询用户结果：{}", result);
            return result;
        }, QUERY_INFO_EXECUTOR);
        //User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", signOutVo.getOpenid()));
        CompletableFuture<Chairs> queryChairFuture = CompletableFuture.supplyAsync(() -> {
            Chairs result = getById(signOutVo.getChairId());
            log.info("查询到桌子结果：{}", result);
            return result;
        }, QUERY_INFO_EXECUTOR);
        //Chairs chair = chairsMapper.selectById(signOutVo.getChairId());
        //校验签到状态
        User user = queryUserFuture.join();
        Chairs chair = queryChairFuture.join();
        checkSignInStatus(signOutVo, user, chair);
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
        log.info("更新用户信息:{}", user);
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
        recordMapper.insert(record);
        log.info("添加学习记录:{}", record);
        INFO_UPDATE_EXECUTOR.execute(() -> {
            redisTemplate.opsForList().leftPush(RedisKey.RECORD_KEY + user.getOpenid(), record);
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
        });
        log.info("签退完毕:{}", chair);
        return chair;
    }

    private int updateChairStatus(User user, Chairs chair) {
        chair.setIsEmpty(true);
        ChairsServiceImpl.log.info("签退前座位信息:" + chair);
        int resultOfSignOut = chairsMapper.updateById(chair);

        if(resultOfSignOut == 1) {
            ChairsServiceImpl.log.info("用户[" + user.getClassname() + user.getName() + "]签退成功!");
            ChairsServiceImpl.log.info("签退后座位信息:" + chair);
            redisTemplate.opsForHash()
                         .putAll(RedisKey.CHAIRS_HASH_KEY + chair.getId(), ImmutableMap.of("isEmpty", true, "version", chair.getVersion()));
            return resultOfSignOut;
        } else {
            ChairsServiceImpl.log.error("用户" + user.getClassname() + user.getName() + "签退失败!");
            throw new InternalServerException();
        }
    }

    private static void checkSignInStatus(SignOutVo signOutVo, User user, Chairs chair) throws Exception {
        if(user == null || chair == null) {
            throw new RequestParamValidationException(ImmutableMap.of("openid:", user.getOpenid(), "chairId", chair.getId()));
        }
        if(!User.SIGN_IN.equals(user.getStatus())) {
            throw new RequestParamValidationException(ImmutableMap.of("userStatus", user.getStatus()));
        }
        if(chair.getIsEmpty() || !signOutVo.getOpenid().equals(chair.getOpenId())) {
            throw new RequestParamValidationException(ImmutableMap.of("chairStatus", chair.getIsEmpty(),
                    "chairUserId", chair.getId(),
                    "userUseChairId", user.getFinalChair()));
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
        ChairsServiceImpl.checkSignInStatus(signOutVo, user, chair);
        //更新座位信息
        updateChairStatus(user, chair);
        //更新用户信息
        user.setStatus(User.SIGN_OUT);
        userService.update(user);
        return chair;
    }

}
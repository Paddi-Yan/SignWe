package com.turing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.turing.anotation.RedisLock;
import com.turing.common.RedisKey;
import com.turing.entity.Chairs;
import com.turing.entity.Door;
import com.turing.entity.User;
import com.turing.entity.vo.SignOutVo;
import com.turing.entity.vo.SignVo;
import com.turing.exception.InternalServerException;
import com.turing.exception.RequestParamValidationException;
import com.turing.mapper.ChairsMapper;
import com.turing.mapper.UserMapper;
import com.turing.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
    private final RankingService rankingService;
    private final UserMapper userMapper;
    private final DoorService doorService;
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;
    private final RecordService recordService;
    private final ExecutorService cacheThreadPool;
    private final ThreadPoolTaskExecutor commonThreadPool;

    @Override
    public List<Chairs> getChairsList() {
        Set<String> keys = redisTemplate.keys(RedisKey.CHAIRS_HASH_KEY + "*");
        if(CollectionUtil.isNotEmpty(keys)) {
            List<Chairs> chairsList = new ArrayList<>(keys.size());
            CompletableFuture.allOf(keys.stream().map(key -> CompletableFuture.runAsync(() -> {
                Map entries = redisTemplate.opsForHash().entries(key);
                Chairs chairs = BeanUtil.mapToBean(entries, Chairs.class, false);
                chairsList.add(chairs);
            }, cacheThreadPool)).toArray(CompletableFuture[] :: new)).join();
            if(CollectionUtil.isNotEmpty(chairsList)) {
                chairsList.sort(Comparator.comparingInt(Chairs :: getId));
                return chairsList;
            }
        }
        List<Chairs> chairs = chairsMapper.selectList(null);
        if(CollectionUtil.isEmpty(chairs)) {
            return Collections.emptyList();
        }
        cacheThreadPool.execute(() -> chairs.forEach(chair -> {
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
            cacheThreadPool.execute(() -> {
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
        return BeanUtil.mapToBean(entries, Chairs.class, false);
    }

    @Override
    @RedisLock(lockName = "signIn", key = "#signVo.openid")
    public Chairs signIn(SignVo signVo) {
        CompletableFuture<Door> validFuture1 = CompletableFuture.supplyAsync(() -> {
            Door door = doorService.getDoorStatus(TURING_TEAM);
            log.info("查询到开门状态信息: {}", door);
            return door;
        }, cacheThreadPool);
        CompletableFuture<User> validFuture2 = CompletableFuture.supplyAsync(() -> {
            User user = userService.getByOpenId(signVo.getOpenid());
            log.info("查询到用户信息: {}", user);
            return user;
        }, cacheThreadPool);
        CompletableFuture<Chairs> validFuture3 = CompletableFuture.supplyAsync(() -> {
            Chairs chair = getById(signVo.getChairId());
            log.info("查询到座位信息: {}", chair);
            return chair;
        }, cacheThreadPool);
        CompletableFuture<Map<String, Object>> validFuture
                = CompletableFuture.allOf(validFuture1, validFuture2, validFuture3)
                                   .thenApply((v) -> {
                                       Door door = validFuture1.join();
                                       User user = validFuture2.join();
                                       Chairs chair = validFuture3.join();
                                       if(!door.getOpen()) {
                                           return ImmutableMap.of("cause", "请开门后再进行签到");
                                       }
                                       if(user == null) {
                                           return ImmutableMap.of("cause", "用户不存在", "openid", signVo.getOpenid());
                                       }
                                       if(User.SIGN_IN.equals(user.getStatus())) {
                                           return ImmutableMap.of("cause", "已处于签到状态,无法再次签到", "userStatus", user.getStatus());
                                       }
                                       if(chair == null) {
                                           return ImmutableMap.of("cause", "无法匹配到对应签到位置", "chairId", signVo.getChairId());
                                       }
                                       if(!chair.getIsEmpty()) {
                                           return ImmutableMap.of("cause", "当前位置已经被占用", "chairStatus", chair.getIsEmpty());
                                       }
                                       return ImmutableMap.of("chair", chair, "user", user);
                                   });
        Map<String, Object> validResult = validFuture.join();
        if(Objects.nonNull(validResult.get("cause"))) {
            throw new RequestParamValidationException(validResult);
        }
        User user = (User) validResult.get("user");
        Chairs chair = (Chairs) validResult.get("chair");
        //CAS修改座位信息
        chair.setOpenId(signVo.getOpenid());
        chair.setIsEmpty(false);
        chair.setLastUsedName(user.getName());
        Integer signInResult = transactionTemplate.execute(transactionStatus -> {
            int count = 0;
            try {
                count = chairsMapper.updateById(chair);
            } catch(Exception e) {
                log.error("用户{}签到失败: {}", user, chair);
                transactionStatus.setRollbackOnly();
            }
            return count;
        });
        if(signInResult == null || signInResult == 0) {
            throw new RequestParamValidationException(ImmutableMap.of("case", "签到占座失败,可能已经被其他人先坐下,请稍后重试或换一个位置签到!"));
        }
        commonThreadPool.submit(() -> {
            //修改位置缓存信息
            redisTemplate.opsForHash()
                         .putAll(RedisKey.CHAIRS_HASH_KEY + chair.getId(), BeanUtil.beanToMap(chair, false, false));
            //修改用户签到信息
            user.setFinalChair(chair.getId());
            user.setFinalDistance(signVo.getDistance());
            user.setFinalStartTime(LocalDateTime.now());
            user.setStatus(User.SIGN_IN);
            userService.update(user);
        });
        log.info("签到信息: {}", chair);
        return chair;
    }

    @Override
    @RedisLock(lockName = "signOut", key = "#signOutVo.openid")
    public Chairs signOut(SignOutVo signOutVo) throws Exception {
        CompletableFuture<User> queryUserFuture = CompletableFuture.supplyAsync(() -> {
            User result = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User :: getOpenid, signOutVo.getOpenid()));
            log.info("查询用户结果：{}", result);
            return result;
        }, cacheThreadPool);
        CompletableFuture<Chairs> queryChairFuture = CompletableFuture.supplyAsync(() -> {
            Chairs result = getById(signOutVo.getChairId());
            log.info("查询到桌子结果：{}", result);
            return result;
        }, cacheThreadPool);
        //校验签到状态
        User user = queryUserFuture.join();
        Chairs chair = queryChairFuture.join();
        checkSignInStatus(signOutVo, user, chair);

        LocalDateTime signInTime = user.getFinalStartTime();
        LocalDateTime signOutTime = LocalDateTime.now();
        Integer studyTime = Math.toIntExact(signInTime.until(signOutTime, ChronoUnit.MINUTES));

        transactionTemplate.execute((status) -> {
            try {
                //更新座位信息
                updateChairStatus(user, chair);
                //用户签退
                userService.signOut(user, studyTime);
                //添加学习记录
                recordService.insertRecord(chair, user, signInTime, signOutTime, studyTime);
                //更新总排行榜
                rankingService.updateRanking(user.getId(), user.getOpenid(), studyTime);
                log.info("签退完毕:{}", chair);
            } catch(Exception e) {
                log.error("用户[{}]签退失败:{}, 异常信息: {}", user, chair, e.getMessage());
                status.setRollbackOnly();
                throw new InternalServerException(ImmutableMap.of("cause", "签退失败"));
            }
            return Boolean.TRUE;
        });
        return chair;
    }

    private int updateChairStatus(User user, Chairs chair) {
        chair.setIsEmpty(true);
        log.info("签退前座位信息:" + chair);
        int resultOfSignOut = chairsMapper.updateById(chair);

        if(resultOfSignOut == 1) {
            log.info("用户[" + user.getClassname() + user.getName() + "]签退成功!");
            log.info("签退后座位信息:" + chair);
            redisTemplate.opsForHash()
                         .putAll(RedisKey.CHAIRS_HASH_KEY + chair.getId(), ImmutableMap.of("isEmpty", true, "version", chair.getVersion()));
            return resultOfSignOut;
        } else {
            log.error("用户" + user.getClassname() + user.getName() + "签退失败!");
            throw new InternalServerException(ImmutableMap.of("cause", "签退失败"));
        }
    }

    private static void checkSignInStatus(SignOutVo signOutVo, User user, Chairs chair) throws Exception {
        if(user == null || chair == null) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "用户信息或位置信息为空"));
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
        CompletableFuture<User> queryUserFuture = CompletableFuture.supplyAsync(() -> {
            User result = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User :: getOpenid, signOutVo.getOpenid()));
            log.info("查询用户结果：{}", result);
            return result;
        }, cacheThreadPool);
        CompletableFuture<Chairs> queryChairFuture = CompletableFuture.supplyAsync(() -> {
            Chairs result = getById(signOutVo.getChairId());
            log.info("查询到桌子结果：{}", result);
            return result;
        }, cacheThreadPool);
        User user = queryUserFuture.join();
        Chairs chair = queryChairFuture.join();
        //校验签到状态
        checkSignInStatus(signOutVo, user, chair);
        transactionTemplate.execute(transactionStatus -> {
            try {
                //更新座位信息
                updateChairStatus(user, chair);
                //更新用户信息
                user.setStatus(User.SIGN_OUT);
                userService.update(user);
            } catch(Exception e) {
                log.error("用户[{}]强制签退失败: {}", user, chair);
                transactionStatus.setRollbackOnly();
            }
            return Boolean.TRUE;
        });
        return chair;
    }

}
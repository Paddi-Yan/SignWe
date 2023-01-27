package com.turing.aspect;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.ImmutableMap;
import com.turing.anotation.RedisLock;
import com.turing.exception.RequestParamValidationException;
import com.turing.utils.SpringELUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

import static com.turing.common.RedisKey.REDISSON_LOCK_PREFIX;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月04日 16:56:32
 */
@Aspect
@Component
@Slf4j
public class RedisLockAspect {
    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint point, RedisLock redisLock) throws Throwable {
        String spel = redisLock.key();
        String lockName = redisLock.lockName();
        String lockKey = getRedisKey(point, lockName, spel);
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLock = lock.tryLock();
        if(!isLock) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "请勿频繁重复操作"));
        }
        log.info("加锁: {}", lockKey);
        Object result = null;
        try {
            result = point.proceed();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private String getRedisKey(ProceedingJoinPoint joinPoint, String lockName, String spel) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        Object target = joinPoint.getTarget();
        Object[] arguments = joinPoint.getArgs();
        return REDISSON_LOCK_PREFIX + lockName + StrUtil.COLON + SpringELUtil.parse(target, spel, targetMethod, arguments);
    }
}

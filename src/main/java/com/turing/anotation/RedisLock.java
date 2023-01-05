package com.turing.anotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月04日 16:53:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {
    String lockName() default "";

    String key() default "";

    int expire() default 5000;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}

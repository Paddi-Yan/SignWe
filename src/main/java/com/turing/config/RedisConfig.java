package com.turing.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年01月19日 22:09:41
 */
@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        //设置Redis的key序列化方式string
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置Redis的value序列化方式json
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        //支持事务
        redisTemplate.setEnableTransactionSupport(true);
        //初始化参数和初始化工作
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedissonClient redissonClient() throws IOException {
        log.info("当前系统配置文件级别为：{}", profile);
        Config config = Config.fromYAML(this.getClass()
                                            .getClassLoader()
                                            .getResource("redisson/redisson-" + profile + ".yml"));
        return Redisson.create(config);
    }
}

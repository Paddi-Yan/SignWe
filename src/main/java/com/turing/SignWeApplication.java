package com.turing;

import com.github.xiaolyuh.cache.config.EnableLayeringCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月29日 16:58:15
 */
@SpringBootApplication
@EnableLayeringCache
public class SignWeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignWeApplication.class, args);
    }

}

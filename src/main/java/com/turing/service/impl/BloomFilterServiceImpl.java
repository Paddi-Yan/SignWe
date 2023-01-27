package com.turing.service.impl;

import com.turing.service.BloomFilterService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: SignWe
 * @CreatedTime: 2023年01月27日 20:59:48
 */
@Service
@RequiredArgsConstructor
public class BloomFilterServiceImpl<T> implements BloomFilterService<T> {
    private final RedissonClient redissonClient;

    @Override
    public RBloomFilter<T> create(String filterName, long expectedInsertions, double falseProbability) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }

    @Override
    public RBloomFilter<T> get(String filterName) {
        return redissonClient.getBloomFilter(filterName);
    }

}

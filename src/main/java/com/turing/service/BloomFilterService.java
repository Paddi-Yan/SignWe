package com.turing.service;

import org.redisson.api.RBloomFilter;

/**
 * @Author: Paddi-Yan
 * @Project: SignWe
 * @CreatedTime: 2023年01月27日 20:58:40
 */
public interface BloomFilterService<T> {
    RBloomFilter<T> create(String filterName, long expectedInsertions, double falseProbability);

    RBloomFilter<T> get(String filterName);

}

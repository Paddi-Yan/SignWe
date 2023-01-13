package com.turing.service;

import com.turing.entity.StatisticsInfo;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月06日 21:40:57
 */
public interface SignStatisticsService {

    void count(String userId);

    StatisticsInfo getSignStatistics(String userId);
}

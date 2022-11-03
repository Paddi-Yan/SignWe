package com.turing.service;

import com.turing.entity.YesterdayRanking;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface YesterdayRankingService extends IService<YesterdayRanking> {

    void generateYesterdayRanking();

    HashMap<String, Integer> getRanking();
}

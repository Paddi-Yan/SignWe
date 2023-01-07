package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.Ranking;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface RankingService extends IService<Ranking> {

    List<Ranking> getRanking();

    void resetRanking();

    void updateRanking(String id, String openid, Integer studyTime);
}

package com.turing.service;

import com.turing.entity.Ranking;
import com.baomidou.mybatisplus.extension.service.IService;

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
}

package com.turing.mapper;

import com.turing.entity.Ranking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Mapper
public interface RankingMapper extends BaseMapper<Ranking> {

    List<Ranking> getRanking();
}

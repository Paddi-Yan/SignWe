package com.turing.mapper;

import com.turing.entity.YesterdayRanking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Mapper
public interface YesterdayRankingMapper extends BaseMapper<YesterdayRanking> {

    /**
     * 清空表数据
     */
    @Update("truncate table sys_yesterday_ranking")
    void deleteAll();
}

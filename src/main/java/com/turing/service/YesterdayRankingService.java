package com.turing.service;

import com.turing.entity.YesterdayRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface YesterdayRecordService extends IService<YesterdayRecord> {

    void generateYesterdayRanking();
}

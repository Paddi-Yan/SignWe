package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.entity.Record;
import com.turing.entity.YesterdayRecord;
import com.turing.mapper.RecordMapper;
import com.turing.mapper.YesterdayRecordMapper;
import com.turing.service.RankingService;
import com.turing.service.RecordService;
import com.turing.service.YesterdayRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class YesterdayRecordServiceImpl extends ServiceImpl<YesterdayRecordMapper, YesterdayRecord> implements YesterdayRecordService {

    @Resource
    private RecordService recordService;

    @Resource
    private RecordMapper recordMapper;

    @Override
    public void generateYesterdayRanking() {
        LocalDateTime yesterdayTime = LocalDateTime.now().minusDays(1);
        recordMapper.selectList(new QueryWrapper<Record>().ge(""))
    }
}

package com.turing.service.impl;

import com.turing.entity.Record;
import com.turing.mapper.RecordMapper;
import com.turing.service.RecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 学习记录历史 服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

}

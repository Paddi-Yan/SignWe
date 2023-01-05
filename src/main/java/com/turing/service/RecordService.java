package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.Record;

import java.util.List;

/**
 * <p>
 * 学习记录历史 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface RecordService extends IService<Record> {

    Boolean insertRecord(Record record, String userId);

    List<Record> getRecordByUser(String id);

    List<Record> getYesterdayRecord();

    void deleteLogical();
}

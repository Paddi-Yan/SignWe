package com.turing.service;

import com.turing.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.User;

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

    List<Record> getRecordByUser(User user);

    List<Record> getYesterdayRecord();

}

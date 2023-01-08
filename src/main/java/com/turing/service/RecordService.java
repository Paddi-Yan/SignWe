package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.common.ScrollResult;
import com.turing.entity.Chairs;
import com.turing.entity.Record;
import com.turing.entity.User;

import java.time.LocalDateTime;
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


    List<Record> getRecordByUser(String id);

    List<Record> getYesterdaySignedRecordList();

    void deleteLogical();

    void insertRecord(Chairs chair, User user, LocalDateTime signInTime, LocalDateTime signOutTime, Integer studyTime);

    ScrollResult<Record> getByScrollWithUserId(String userId, Long max, Long offset);
}

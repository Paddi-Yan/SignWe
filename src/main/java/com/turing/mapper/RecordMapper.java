package com.turing.mapper;

import com.turing.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.turing.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 学习记录历史 Mapper 接口
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {


    List<Record> getByUser(User user);
}

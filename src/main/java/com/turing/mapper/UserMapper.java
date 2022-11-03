package com.turing.mapper;

import com.turing.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}

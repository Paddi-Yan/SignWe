package com.turing.service;

import com.turing.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.dto.RegisterDto;
import com.turing.entity.vo.UserVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface UserService extends IService<User> {

    User getByOpenId(String openid);

    UserVo register(RegisterDto registerDto);

}

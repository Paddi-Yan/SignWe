package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.User;
import com.turing.entity.vo.RegisterVo;

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

    User register(RegisterVo registerVo);

    User update(User user);

    void signOut(User user, Integer studyTime);
}

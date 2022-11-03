package com.turing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.entity.User;
import com.turing.entity.dto.RegisterDto;
import com.turing.entity.vo.UserVo;
import com.turing.mapper.UserMapper;
import com.turing.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getByOpenId(String openid) {
        log.info("openid:" + openid);
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", openid));
        log.info("查询用户结果：" + user);
        return user;
    }

    @Override
    public UserVo register(RegisterDto registerDto) {
        User user = new User();
        user.setOpenid(registerDto.getOpenid());
        user.setClassname(registerDto.getClassname());
        user.setName(registerDto.getName());
        user.setAdmin(false);
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        UserVo userVo = new UserVo();
        userVo.transform(user);
        return userVo;
    }
}

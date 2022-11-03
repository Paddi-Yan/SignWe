package com.turing.service.impl;

import com.turing.entity.User;
import com.turing.mapper.UserMapper;
import com.turing.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}

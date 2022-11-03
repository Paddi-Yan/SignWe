package com.turing.service;

import com.turing.entity.Chairs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.User;
import com.turing.entity.dto.SignDto;
import com.turing.entity.dto.SignOutDto;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
public interface ChairsService extends IService<Chairs> {


    List<Chairs> getChairsList();

    Chairs getById(Integer id);

    Chairs signIn(Chairs chair, SignDto signDto, User user) throws Exception;

    Chairs signOut(SignOutDto signOutDto) throws Exception;

    Chairs signOutForce(SignOutDto signOutDto) throws Exception;
}

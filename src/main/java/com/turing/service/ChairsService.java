package com.turing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.turing.entity.Chairs;
import com.turing.entity.vo.SignOutVo;
import com.turing.entity.vo.SignVo;

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

    Chairs signIn(SignVo signVo) throws Exception;

    Chairs signOut(SignOutVo signOutVo) throws Exception;

    Chairs signOutForce(SignOutVo signOutVo) throws Exception;
}

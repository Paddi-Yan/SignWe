package com.turing.controller;


import cn.hutool.core.collection.CollectionUtil;
import com.turing.common.Result;
import com.turing.entity.Ranking;
import com.turing.service.RankingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@RestController
@RequestMapping("/ranking")
public class RankingController {

    @Resource
    private RankingService service;

    @ApiOperation("获取当期学习排行榜")
    @ResponseBody
    @GetMapping("/get")
    public Result getRanking() {
        List<Ranking> result = service.getRanking();
        return Result.success(CollectionUtil.isEmpty(result) ? "暂无人上榜" : result);
    }
}


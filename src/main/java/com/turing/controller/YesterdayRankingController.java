package com.turing.controller;


import com.turing.common.RedisKey;
import com.turing.common.Result;
import com.turing.service.YesterdayRankingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@RestController
@RequestMapping("/yesterdayRecord")
public class YesterdayRankingController {

    @Resource
    private YesterdayRankingService service;

    @ApiOperation("获取昨日学习排行榜")
    @GetMapping("/get")
    @ResponseBody
    public Result getYesterdayRecord() {
        return Result.success(service.getRanking(RedisKey.TURING_TEAM));
    }
}


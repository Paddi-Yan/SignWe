package com.turing.controller;


import com.turing.common.Result;
import com.turing.service.RecordService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 学习记录历史 前端控制器
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@RestController
@RequestMapping("/record")
public class RecordController {

    @Resource
    private RecordService recordService;

    @ResponseBody
    @GetMapping("/getByUserId/{id}")
    @ApiOperation("获取本赛季打卡记录")
    public Result getByUserId(@PathVariable String id) {
        return Result.success(recordService.getRecordByUser(id));
    }

    @ResponseBody
    @GetMapping("/getByScroll")
    @ApiOperation(value = "滚动分页获取本赛季打卡记录", notes = "第一次获取max和offset均为默认值,滚动翻页需要带上第一次返回值中的max和offset")
    public Result getByScroll(@RequestParam(value = "max", defaultValue = "0") Long max,
                              @RequestParam String userId,
                              @RequestParam(value = "offset", defaultValue = "0") Long offset) {
        return Result.success(recordService.getByScrollWithUserId(userId, max, offset));
    }
}


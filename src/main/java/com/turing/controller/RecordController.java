package com.turing.controller;


import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.User;
import com.turing.service.RecordService;
import com.turing.service.UserService;
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

    @Resource
    private UserService userService;

    @ResponseBody
    @GetMapping("/getByUserId/{id}")
    @ApiOperation("获取本赛季打卡记录")
    public Result getByUserId(@PathVariable String id) {
        return Result.success(recordService.getRecordByUser(id));
    }
}


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
        User user = userService.getById(id);
        if(user == null) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR,"不存在该用户!");
        }
        if(user.getClassname() == null || user.getName() == null) {
            return Result.fail(HttpStatusCode.FORBIDDEN,"用户信息不完整,请先登记信息!");
        }
        return Result.success(recordService.getRecordByUser(user));
    }
}


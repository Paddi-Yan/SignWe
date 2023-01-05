package com.turing.controller;


import com.turing.common.RedisKey;
import com.turing.common.Result;
import com.turing.service.NoticeService;
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
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @ResponseBody
    @GetMapping("/getNotice")
    @ApiOperation("获取公告内容")
    public Result getNotice() {
        return Result.success(noticeService.getNotice(RedisKey.TURING_TEAM));
    }

}


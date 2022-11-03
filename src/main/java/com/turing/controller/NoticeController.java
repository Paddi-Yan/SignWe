package com.turing.controller;


import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Notice;
import com.turing.entity.User;
import com.turing.entity.vo.UserVo;
import com.turing.service.NoticeService;
import com.turing.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private UserService userService;

    @ResponseBody
    @GetMapping("/getNotice")
    @ApiOperation("获取公告内容")
    public Result getNotice() {
        return Result.success(noticeService.getNotice());
    }

}


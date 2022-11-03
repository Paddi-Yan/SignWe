package com.turing.controller.admin;

import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Notice;
import com.turing.entity.User;
import com.turing.entity.dto.SignOutDto;
import com.turing.service.ChairsService;
import com.turing.service.NoticeService;
import com.turing.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月02日 20:54:13
 */
@RestController()
@RequestMapping("/admin")
public class AdminController {


    @Resource
    private UserService userService;

    @Resource
    private ChairsService chairsService;

    @Resource
    private NoticeService noticeService;

    @ResponseBody
    @PostMapping("/signOutForce")
    @ApiOperation(value = "强制签退,不记录时长")
    public Result signOutForce(@RequestBody SignOutDto signOutDto, @RequestParam String adminOpenId) {
        //校验管理员身份
        User admin = userService.getByOpenId(adminOpenId);
        if(admin.getAdmin().booleanValue() == false) {
            return Result.fail(HttpStatusCode.FORBIDDEN, "非管理员没有权限进行强制签退的操作!");
        }
        try {
            return Result.success(chairsService.signOutForce(signOutDto));
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("/updateNotice")
    @ApiOperation("修改公告内容,需要验证是否管理员")
    public Result updateNotice(@RequestParam String openid, @RequestBody Notice notice) {
        User user = userService.getByOpenId(openid);
        if(user == null || !user.getAdmin()) {
            return Result.fail(HttpStatusCode.FORBIDDEN, "用户不存在或该用户无权限进行更改公告操作");
        }
        return Result.success(noticeService.updateNotice(notice));
    }



}

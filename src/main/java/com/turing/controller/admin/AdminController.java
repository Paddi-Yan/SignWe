package com.turing.controller.admin;

import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Notice;
import com.turing.entity.User;
import com.turing.entity.dto.SignOutDto;
import com.turing.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.security.sasl.AuthenticationException;

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

    @Resource
    private RankingService rankingService;

    @Resource
    private RecordService recordService;

    private void checkAdmin(String openId) throws Exception{
        User user = userService.getByOpenId(openId);
        if(user == null || user.getAdmin().booleanValue() == false) {
            throw new AuthenticationException("非管理员没有权限进行此操作!");
        }
    }

    @ResponseBody
    @PostMapping("/signOutForce")
    @ApiOperation(value = "强制签退,不记录时长")
    public Result signOutForce(@RequestBody SignOutDto signOutDto, @RequestParam String adminOpenId) {
        //校验管理员身份
        try {
            checkAdmin(adminOpenId);
            return Result.success(chairsService.signOutForce(signOutDto));
        } catch(AuthenticationException e) {
            return Result.fail(HttpStatusCode.FORBIDDEN, e.getMessage());
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("/updateNotice")
    @ApiOperation("修改公告内容,需要验证是否管理员")
    public Result updateNotice(@RequestParam String openid, @RequestBody Notice notice) {
        try {
            checkAdmin(openid);
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.FORBIDDEN, e.getMessage());
        }
        return Result.success(noticeService.updateNotice(notice));
    }


    @ResponseBody
    @PostMapping("/resetRanking")
    @ApiOperation("重置排名")
    public Result resetRanking(@RequestParam String openid) {
        try {
            checkAdmin(openid);
            rankingService.resetRanking();
        } catch(AuthenticationException e) {
            return Result.fail(HttpStatusCode.FORBIDDEN, e.getMessage());
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.ERROR, e.getMessage());
        }
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/resetRankingAndRecord")
    @ApiOperation("重置排名以及逻辑删除学习记录")
    public Result resetRankingAndRecord(@RequestParam String openid) {
        try {
            checkAdmin(openid);
            rankingService.resetRanking();
            recordService.deleteLogical();
        } catch(AuthenticationException e) {
            return Result.fail(HttpStatusCode.FORBIDDEN, e.getMessage());
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.ERROR, e.getMessage());
        }
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/rm-rf")
    @ApiOperation("删除所有数据(包括用户数据,学习记录,学习排名以及所有座位信息)")
    public Result rmrf() {
        return Result.success("6");
    }

}

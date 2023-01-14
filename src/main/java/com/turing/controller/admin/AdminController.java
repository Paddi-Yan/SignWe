package com.turing.controller.admin;

import com.google.common.collect.ImmutableMap;
import com.turing.common.RedisKey;
import com.turing.common.Result;
import com.turing.entity.Notice;
import com.turing.entity.User;
import com.turing.entity.vo.SignOutVo;
import com.turing.exception.AuthenticationException;
import com.turing.exception.RequestParamValidationException;
import com.turing.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月02日 20:54:13
 */
@RestController()
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final UserService userService;

    private final ChairsService chairsService;

    private final NoticeService noticeService;

    private final RankingService rankingService;

    private final RecordService recordService;

    private void checkAdmin(String openId) {
        User user = userService.getByOpenId(openId);
        if(user == null || !user.getAdmin()) {
            throw new AuthenticationException();
        }
    }

    @ResponseBody
    @PostMapping("/signOutForce")
    @ApiOperation(value = "强制签退,不记录时长")
    public Result signOutForce(@RequestBody SignOutVo signOutVo, @RequestParam String adminOpenId) throws Exception {
        //校验管理员身份
        checkAdmin(adminOpenId);
        return Result.success(chairsService.signOutForce(signOutVo));
    }

    @ResponseBody
    @PostMapping("/updateNotice")
    @ApiOperation("修改公告内容,需要验证是否管理员")
    public Result updateNotice(@RequestParam String openid, @RequestBody Notice notice) {
        checkAdmin(openid);
        return Result.success(noticeService.updateNotice(notice, RedisKey.TURING_TEAM));
    }
    
    @ResponseBody
    @PostMapping("/resetRanking")
    @ApiOperation("重置排名")
    public Result resetRanking(@RequestParam String openid) {
        checkAdmin(openid);
        rankingService.resetRanking();
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/resetRankingAndRecord")
    @ApiOperation("重置排名以及逻辑删除学习记录")
    public Result resetRankingAndRecord(@RequestParam String openid) {
        checkAdmin(openid);
        rankingService.resetRanking();
        recordService.deleteLogical();
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/rm-rf")
    @ApiOperation("删除所有数据(包括用户数据,学习记录,学习排名以及所有座位信息)")
    public Result rmrf() {
        return Result.success("6");
    }

    @ResponseBody
    @GetMapping("/searchUser")
    @ApiOperation("根据名称获取用户信息")
    public Result searchUser(@RequestParam String username) {
        return Result.success(userService.getByName(username));
    }

    @ResponseBody
    @PostMapping
    @ApiOperation("/addAdmin")
    public Result addAdmin(@RequestParam String adminOpenId, @RequestParam String userOpenId) {
        checkAdmin(adminOpenId);
        User user = userService.getByOpenId(userOpenId);
        if(user == null) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "用户不存在"));
        }
        if(user.getAdmin()) {
            throw new RequestParamValidationException(ImmutableMap.of("cause", "用户已经是管理员"));
        }
        user.setAdmin(true);
        userService.update(user);
        return Result.success(user);
    }
}

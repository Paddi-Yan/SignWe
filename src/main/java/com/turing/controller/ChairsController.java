package com.turing.controller;


import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Chairs;
import com.turing.entity.Door;
import com.turing.entity.User;
import com.turing.entity.dto.SignOutDto;
import com.turing.entity.dto.SignDto;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
import com.turing.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
@RequestMapping("/chairs")
public class ChairsController {

    @Resource
    private ChairsService chairsService;

    @Resource
    private UserService userService;

    @Resource
    private DoorService doorService;

    @ResponseBody
    @GetMapping("/getAll")
    @ApiOperation(value = "获取座位信息")
    public Result getAllChairs() {
        return Result.success(chairsService.getChairsList());
    }

    @ResponseBody
    @GetMapping("/getByChairId/{id}")
    @ApiOperation(value = "通过桌号获取当前位置状态信息")
    public Result getByChairId(@PathVariable Integer id) {
        Chairs chair = chairsService.getById(id);
        if(chair == null) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR, "当前桌号不存在,查询失败!");
        }
        return Result.success(chair);
    }

    @ResponseBody
    @PostMapping("/signIn")
    @ApiOperation(value = "签到")
    public Result signIn(@RequestBody SignDto signDto) {
        Door door = doorService.getDoorStatus();
        if(!door.getOpen()) {
            return Result.success(HttpStatusCode.Accepted, "请先开门再入座!");
        }
        User user = userService.getByOpenId(signDto.getOpenid());
        if(user == null) {
            return Result.fail(HttpStatusCode.UNAUTHORIZED, "未登记信息,请登记信息后重试!");
        }
        if(User.SIGN_IN.equals(user.getStatus())) {
            return Result.fail(HttpStatusCode.NO_CONTENT, "已在其他位置签到,无法进行该操作,如有问题请联系管理员!");
        }
        Chairs chair = chairsService.getById(signDto.getChairId());
        if(chair == null || !chair.getIsEmpty()) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR, "座位信息有误或座位已经被其他人占下!");
        }
        try {
            return Result.success(chairsService.signIn(chair, signDto, user));
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.Accepted, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("/signOut")
    @ApiOperation(value = "签退")
    public Result signOut(@RequestBody SignOutDto signOutDto) {
        try {
            return Result.success(chairsService.signOut(signOutDto, false));
        } catch(Exception e) {
            return Result.fail(HttpStatusCode.REQUEST_PARAM_ERROR, e.getMessage());
        }
    }

}


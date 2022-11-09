package com.turing.controller;


import com.google.common.collect.ImmutableMap;
import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Chairs;
import com.turing.entity.Door;
import com.turing.entity.User;
import com.turing.entity.vo.SignOutVo;
import com.turing.entity.vo.SignVo;
import com.turing.exception.RequestParamValidationException;
import com.turing.exception.ResourceNotFoundException;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
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
            throw new ResourceNotFoundException(ImmutableMap.of("chairId",id));
        }
        return Result.success(chair);
    }

    @ResponseBody
    @PostMapping("/signIn")
    @ApiOperation(value = "签到")
    public Result signIn(@RequestBody SignVo signVo) throws Exception {
        return Result.success(chairsService.signIn(signVo));
    }

    @ResponseBody
    @PostMapping("/signOut")
    @ApiOperation(value = "签退")
    public Result signOut(@RequestBody SignOutVo signOutVo) throws Exception {
        return Result.success(chairsService.signOut(signOutVo));
    }

}


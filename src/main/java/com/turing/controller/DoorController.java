package com.turing.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.Chairs;
import com.turing.entity.Door;
import com.turing.service.ChairsService;
import com.turing.service.DoorService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.turing.common.RedisKey.TURING_TEAM;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@RestController
@RequestMapping("/door")
public class DoorController {

    @Resource
    private DoorService doorService;

    @Resource
    private ChairsService chairsService;


    @ResponseBody
    @GetMapping("/getStatus")
    @ApiOperation(value = "查询开门状态")
    public Result getDoorStatus() {
        return Result.success(doorService.getDoorStatus(TURING_TEAM));
    }

    @ResponseBody
    @PostMapping("/open")
    @ApiOperation(value = "开门")
    public Result openDoor(@RequestParam String username) {
        Door door = doorService.getDoorStatus(TURING_TEAM);
        if(door.getOpen()) {
            return Result.success(HttpStatusCode.NO_CONTENT, "已开门,请勿重复操作!");
        }
        return Result.success(doorService.openDoor(door, username));
    }

    @ResponseBody
    @PostMapping("/close")
    @ApiOperation(value = "关门")
    public Result closeDoor(@RequestParam String username) {
        Door door = doorService.getDoorStatus(TURING_TEAM);
        if(!door.getOpen()) {
            return Result.success(HttpStatusCode.NO_CONTENT, "已关门,请勿重复操作!");
        }
        int countOfStudyPeople = chairsService.count(new QueryWrapper<Chairs>().eq("is_empty", 0));
        if(countOfStudyPeople >= 1) {
            return Result.success(HttpStatusCode.NO_CONTENT, "有其他同学正在学习,请勿关门!");
        }
        return Result.success(doorService.closeDoor(door, username));
    }

}


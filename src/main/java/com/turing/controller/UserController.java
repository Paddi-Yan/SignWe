package com.turing.controller;


import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.User;
import com.turing.entity.dto.UserDto;
import com.turing.entity.vo.RegisterVo;
import com.turing.service.SignStatisticsService;
import com.turing.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Paddi-Yan
 * @since 2022-10-29
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final SignStatisticsService signStatisticsService;

    @PostMapping("/checkUser")
    @ResponseBody
    @ApiOperation(value = "检查用户是否存在,如果用户存在返回用户信息", notes = "对应云函数-isNewGuys")
    public Result init(@RequestBody String openid) {
        User user = userService.getByOpenId(openid);
        UserDto userDto = new UserDto();
        userDto.transform(user);
        return Result.success(userDto);
    }

    @PostMapping("/register")
    @ApiOperation(value = "登记信息", notes = "对应云函数-newOne")
    @ResponseBody
    public Result register(@RequestBody RegisterVo registerVo) {
        String openid = registerVo.getOpenid();
        User user = userService.getByOpenId(openid);
        if(user != null) {
            return Result.success(HttpStatusCode.NO_CONTENT, "该用户已经存在,请勿重复登记,如需更改信息,请联系管理员");
        }
        UserDto userDto = new UserDto();
        user = userService.register(registerVo);
        userDto.transform(user);
        return Result.success(userDto);
    }

    @GetMapping("/getSignStatistics/{userId}")
    @ApiOperation(value = "获取本月签到统计信息", notes = "keepSignInDays为本月连续签到天数")
    @ResponseBody
    public Result getSignStatistics(@PathVariable String userId) {
        return Result.success(signStatisticsService.getSignStatistics(userId));
    }
}


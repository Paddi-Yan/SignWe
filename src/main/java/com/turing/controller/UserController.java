package com.turing.controller;


import com.turing.common.HttpStatusCode;
import com.turing.common.Result;
import com.turing.entity.User;
import com.turing.entity.dto.RegisterDto;
import com.turing.entity.vo.UserVo;
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
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/checkUser")
    @ResponseBody
    @ApiOperation(value = "检查用户是否存在,如果用户存在返回用户信息", notes = "对应云函数-isNewGuys")
    public Result init(@RequestBody String openid) {
        return Result.success(userService.getByOpenId(openid));
    }

    @PostMapping("/register")
    @ApiOperation(value = "登记信息", notes = "对应云函数-newOne")
    @ResponseBody
    public Result register(@RequestBody RegisterDto registerDto) {
        String openid = registerDto.getOpenid();
        User user = userService.getByOpenId(openid);
        UserVo userVo = new UserVo();
        userVo.transform(user);
        if(!userVo.getIsNewGuys().booleanValue()) {
            return Result.success(HttpStatusCode.NO_CONTENT, "该用户已经存在,请勿重复登记,如需更改信息,请联系管理员");
        }
        return Result.success(userService.register(registerDto));
    }
}


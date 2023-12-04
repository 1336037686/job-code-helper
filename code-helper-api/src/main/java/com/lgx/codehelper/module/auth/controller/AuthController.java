package com.lgx.codehelper.module.auth.controller;

import cn.hutool.system.UserInfo;
import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.module.auth.model.request.LoginForm;
import com.lgx.codehelper.module.auth.model.response.UserInfoResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-03 12:59
 */
@RestController
@RequestMapping ("/api/v1/auth")
public class AuthController {

    @PostMapping("/user/login")
    public Result<Map<String, String>> dologin(@RequestBody LoginForm loginForm) {
        // 实际项目中，可以在这里进行用户名和密码的验证逻辑
        // 如果验证通过，返回一个包含用户信息和token的JSON字符串
        // 如果验证失败，可以返回一个错误信息
        Map<String, String> map = new HashMap<>();
        map.put("token", "code-helper");
        return Result.ok(map);
    }


    @GetMapping("/user/info")
    public Result<UserInfoResponse> doGetUserInfo() {
        return Result.ok(new UserInfoResponse());
    }

    @PostMapping("/user/logout")
    public Result<Boolean> doLogOut() {
        return Result.ok();
    }


}

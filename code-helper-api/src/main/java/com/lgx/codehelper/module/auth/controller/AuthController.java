package com.lgx.codehelper.module.auth.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.lgx.codehelper.common.filter.auth.UserInfo;
import com.lgx.codehelper.common.filter.auth.UserInfoContextHolder;
import com.lgx.codehelper.module.auth.domain.User;
import com.lgx.codehelper.module.auth.model.request.LoginFormRequest;
import com.lgx.codehelper.module.auth.model.request.RegisterFormRequest;
import com.lgx.codehelper.module.auth.model.response.UserInfoResponse;
import com.lgx.codehelper.module.auth.service.UserService;
import com.lgx.codehelper.util.JWTUtil;
import com.lgx.codehelper.util.ResultUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-03 12:59
 */
@RestController
@RequestMapping ("/api/v1/auth")
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param registerForm /
     * @return /
     */
    @PostMapping("/user/register")
    public Result<Object> doRegister(@RequestBody RegisterFormRequest registerForm) {
        User user = new User();
        BeanUtil.copyProperties(registerForm, user);
        user.setPassword(SecureUtil.md5(user.getPassword()));
        boolean save = userService.save(user);
        return ResultUtil.returnOrThrow(save, "注册失败");
    }

    /**
     * 用户登录
     * @param loginForm /
     * @return /
     */
    @PostMapping("/user/login")
    public Result<Map<String, String>> dologin(@RequestBody LoginFormRequest loginForm) {
        User user = userService.lambdaQuery().eq(User::getUsername, loginForm.getUsername()).one();
        if (Objects.isNull(user)) throw new ApiException(ResultStatus.FAIL, "当前用户不存在");
        String md5Password = SecureUtil.md5(loginForm.getPassword());
        if (!md5Password.equals(user.getPassword())) throw new ApiException(ResultStatus.FAIL, "用户名或密码错误");
        String token = JWTUtil.createAccessToken(user.getId(), user.getUsername());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     * @return /
     */
    @GetMapping("/user/info")
    public Result<UserInfoResponse> doGetUserInfo() {
        UserInfo userInfo = UserInfoContextHolder.getUserInfo();
        User user = userService.getById(userInfo.getId());
        UserInfoResponse response = new UserInfoResponse();
        BeanUtil.copyProperties(user, response);
        return Result.ok(response);
    }

    /**
     * 退出登录
     * @return /
     */
    @PostMapping("/user/logout")
    public Result<Boolean> doLogOut() {
        return Result.ok();
    }


}

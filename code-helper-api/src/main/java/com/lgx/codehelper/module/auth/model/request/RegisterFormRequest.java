package com.lgx.codehelper.module.auth.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-05 10:20
 */
@Data
public class RegisterFormRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 电话
     */
    @NotBlank(message = "电话不能为空")
    private String phone;

}

package com.lgx.codehelper.common.filter.auth;

import lombok.Data;

/**
 * 登录用户信息
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-05 08:46
 */
@Data
public class UserInfo {

    /**
     * 用户ID id
     */
    private Long id;

    /**
     * 用户名 username
     */
    private String username;

}

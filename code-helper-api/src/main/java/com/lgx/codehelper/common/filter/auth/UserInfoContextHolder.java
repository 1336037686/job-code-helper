package com.lgx.codehelper.common.filter.auth;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-05 08:45
 */
public class UserInfoContextHolder {

    private static final ThreadLocal<UserInfo> TOKEN_HOLDER = new ThreadLocal<>();

    public static UserInfo getUserInfo() {
        return TOKEN_HOLDER.get();
    }

    public static void setUserInfo(UserInfo userInfo) {
        TOKEN_HOLDER.set(userInfo);
    }

    public static void clearToken() {
        TOKEN_HOLDER.remove();
    }

}

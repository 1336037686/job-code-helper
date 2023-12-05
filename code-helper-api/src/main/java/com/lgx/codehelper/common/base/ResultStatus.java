package com.lgx.codehelper.common.base;

/**
 * 返回状态码
 *
 * 通用成功：000 200
 * 通用失败：000 400
 *
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-05 16:12 <br>
 */
public enum ResultStatus {

    // region--- 0000 xxxx 基础状态码 ---
    SUCCESS(20000, "操作成功"),

    FAIL(40000, "服务异常"),

    ACCOUNT_NOT_EXIST(50000, "登陆账号不存在"),

    ILLEGAL_TOKEN(50008, "非法Token"),

    TOKEN_EXPIRED(50014, "Token过期")
    // endregion------------------------
    ;

    // 状态码
    private final int value;

    // 提示
    private final String reasonPhrase;

    ResultStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int getValue() {
        return value;
    }


    public String getReasonPhrase() {
        return reasonPhrase;
    }

}

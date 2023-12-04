package com.lgx.codehelper.common.base;

/**
 * 返回状态码
 * 000 000
 * 前三位，表示具体模块
 * 后三位，表示具体状态
 *
 * 通用成功：000 200
 * 通用失败：000 400
 *
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-05 16:12 <br>
 * @description: JyResultStatus <br>
 */
public enum ResultStatus {

    // region--- 0000 xxxx 基础状态码 ---
    /**
     * 0000 0200 成功
     */
    SUCCESS(20000, "操作成功"),

    /**
     * 0000 0400 失败
     */
    FAIL(40000, "服务异常")
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

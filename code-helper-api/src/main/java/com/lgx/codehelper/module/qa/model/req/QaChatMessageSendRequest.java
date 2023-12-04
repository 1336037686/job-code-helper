package com.lgx.codehelper.module.qa.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:53
 */
@Data
public class QaChatMessageSendRequest extends QaChatItemRequest {

    /**
     * 消息ID
     */
    @NotNull(message = "mid不能为空")
    private Long mid;

    /**
     * 用户问题
     */
    @NotBlank(message = "用户问题不能为空")
    private String message;


}

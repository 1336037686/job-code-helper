package com.lgx.codehelper.module.qa.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:53
 */
@Data
public class QaChatRequest {

    /**
     * 唯一标识
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 聊天标题
     */
    private String title = "新建聊天";

    /**
     * 用户问题
     */
    @NotBlank(message = "用户问题不能为空")
    private String message;


}

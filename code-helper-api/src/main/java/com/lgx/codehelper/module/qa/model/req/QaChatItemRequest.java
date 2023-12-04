package com.lgx.codehelper.module.qa.model.req;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-04 10:46
 */
@Data
public class QaChatItemRequest {

    /**
     * 唯一标识
     */
    @NotNull(message = "id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 聊天标题
     */
    private String title = "新建聊天";

    /**
     * 模型
     */
    private String model;

    /**
     * 历史
     */
    private String history;

}

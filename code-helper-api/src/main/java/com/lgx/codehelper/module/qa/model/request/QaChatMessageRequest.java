package com.lgx.codehelper.module.qa.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:53
 */
@Data
public class QaChatMessageRequest {

    /**
     * 唯一标识
     */
    @NotNull(message = "id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 消息ID
     */
    @NotNull(message = "mid不能为空")
    private Long mid;


}

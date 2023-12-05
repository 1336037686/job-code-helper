package com.lgx.codehelper.module.qa.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:53
 */
@Data
public class QaChatResponse {

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
     * 用户消息
     */
    private List<Message> message;

}

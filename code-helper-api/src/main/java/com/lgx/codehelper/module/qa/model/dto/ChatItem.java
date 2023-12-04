package com.lgx.codehelper.module.qa.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 14:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatItem {

    /**
     * chat id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * title
     */
    private String title;

    /**
     * 模型
     */
    private String model;

    /**
     * 历史记录
     */
    private String history;

}

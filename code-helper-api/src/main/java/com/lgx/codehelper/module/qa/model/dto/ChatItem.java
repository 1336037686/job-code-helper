package com.lgx.codehelper.module.qa.model.dto;

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
    private Long id;

    /**
     * title
     */
    private String title;

}

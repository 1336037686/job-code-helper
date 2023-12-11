package com.lgx.codehelper.util;

import com.lgx.codehelper.module.qa.model.dto.ChatItem;
import com.unfbx.chatgpt.entity.chat.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 13:18
 */
public class SseUtil {

    // 聊天记录
    public static final ConcurrentHashMap<Long, ChatItem> CHAT_ITEMS = new ConcurrentHashMap<>();

    // SSE连接存储
    public static final ConcurrentHashMap<Long, SseEmitter> SSE_EMITTERS = new ConcurrentHashMap<>();

    // 用户消息存储
    public static final ConcurrentHashMap<Long, List<Message>> USER_MESSAGES= new ConcurrentHashMap<>();


}

package com.lgx.codehelper.module.qa.controller;

import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.lgx.codehelper.common.listener.OpenAiSseEventSourceListener;
import com.lgx.codehelper.module.qa.model.dto.ChatItem;
import com.lgx.codehelper.module.qa.model.req.QaChatItemRequest;
import com.lgx.codehelper.module.qa.model.req.QaChatMessageRequest;
import com.lgx.codehelper.module.qa.model.req.QaChatMessageSendRequest;
import com.lgx.codehelper.util.OpenAiUtil;
import com.lgx.codehelper.util.SseUtil;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * QA对话
 *
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:47
 */
@RestController
@RequestMapping("/api/v1/qa/chat")
@Slf4j
public class QaChatController {

    /**
     * 创建聊天
     * @param qaChatItemRequest /
     * @return
     */
    @PostMapping("/create")
    public Result<ChatItem> doCreateQaChat(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        ChatItem chatItem = new ChatItem(qaChatItemRequest.getId(), qaChatItemRequest.getTitle(), qaChatItemRequest.getModel(), qaChatItemRequest.getHistory());
        // 如果当前聊天不存在则创建
        if (!SseUtil.CHAT_ITEMS.containsKey(qaChatItemRequest.getId())) {
            SseUtil.CHAT_ITEMS.put(qaChatItemRequest.getId(), chatItem);
        }
        return Result.ok(chatItem);
    }

    /**
     * 修改聊天
     * @param qaChatItemRequest /
     * @return
     */
    @PostMapping("/update")
    public Result<ChatItem> doUpdateQaChat(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        if (!SseUtil.CHAT_ITEMS.containsKey(qaChatItemRequest.getId())) {
            throw new ApiException(ResultStatus.FAIL, "当前聊天不存在");
        }
        ChatItem chatItem = SseUtil.CHAT_ITEMS.get(qaChatItemRequest.getId());
        chatItem.setTitle(qaChatItemRequest.getTitle());
        chatItem.setModel(qaChatItemRequest.getModel());
        chatItem.setHistory(qaChatItemRequest.getHistory());
        return Result.ok(chatItem);
    }

    /**
     * 删除聊天
     * @param qaChatItemRequest /
     */
    @PostMapping("/delete")
    public Result<Object> doDeleteQaChat(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        log.info("id: {}, title: {}, delete", qaChatItemRequest.getId(), qaChatItemRequest.getTitle());
        // 用户聊天
        SseUtil.CHAT_ITEMS.remove(qaChatItemRequest.getId());
        return Result.ok(true);
    }

    /**
     * 获取聊天列表
     */
    @PostMapping("/query")
    public Result<List<ChatItem>> doQueryChatList() {
        ConcurrentHashMap.KeySetView<Long, ChatItem> keySet = SseUtil.CHAT_ITEMS.keySet();
        List<Long> comparatorKeys = keySet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        List<ChatItem> res = new ArrayList<>();
        for (Long comparatorKey : comparatorKeys) {
            ChatItem chatItem = SseUtil.CHAT_ITEMS.get(comparatorKey);
            res.add(chatItem);
        }
        return Result.ok(res);
    }

    /**
     * 获取聊天记录
     */
    @PostMapping("/query/{id}")
    public Result<List<Message>> doQueryChatMessageList(@PathVariable("id") Long id) {
        List<Message> messages = SseUtil.USER_MESSAGES.get(id);
        if (Objects.isNull(messages)) {
            messages = new CopyOnWriteArrayList<>();
        }
        return Result.ok(messages);
    }

    /**
     * 清除聊天列表
     */
    @PostMapping("/clean")
    public Result<Boolean> doCleanChatMessages(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        List<Message> messages = SseUtil.USER_MESSAGES.get(qaChatItemRequest.getId());
        messages.clear();
        return Result.ok(true);
    }


    /**
     * 创建消息连接
     *
     * @param qaChatMessageRequest /
     * @return /
     */
    @GetMapping("/message/create")
    public SseEmitter doCreatMessage(@Valid QaChatMessageRequest qaChatMessageRequest) {
        log.info("id: {}, mid: {}, create", qaChatMessageRequest.getId(), qaChatMessageRequest.getMid());
        SseEmitter emitter = new SseEmitter(600000L);
        SseUtil.SSE_EMITTERS.put(qaChatMessageRequest.getMid(), emitter);
        emitter.onCompletion(() -> SseUtil.SSE_EMITTERS.remove(qaChatMessageRequest.getMid()));
        return emitter;
    }

    /**
     * 检测消息通信是否准备完成
     */
    @PostMapping("/message/is-ready")
    public Result<Boolean> doChectMessageBeReady (@Valid @RequestBody QaChatMessageRequest qaChatMessageRequest) {
        SseEmitter currentSseEmitter = SseUtil.SSE_EMITTERS.get(qaChatMessageRequest.getMid());
        if (Objects.isNull(currentSseEmitter)) return Result.ok(false);
        return Result.ok(true);
    }

    /**
     * 关闭消息连接
     * @param qaChatMessageRequest /
     */
    @PostMapping("/message/close")
    public Result<Object> doCloseMessage(@Valid @RequestBody QaChatMessageRequest qaChatMessageRequest) {
        log.info("id: {}, mid: {}, close", qaChatMessageRequest.getId(), qaChatMessageRequest.getMid());
        SseEmitter sse = SseUtil.SSE_EMITTERS.get(qaChatMessageRequest.getMid());
        if (sse != null) {
            sse.complete();
            // 删除SSE和用户聊天
            SseUtil.SSE_EMITTERS.remove(qaChatMessageRequest.getMid());
        }
        return Result.ok(true);
    }

    @PostMapping("/message/send")
    public Result<Object> doSend(@Valid @RequestBody QaChatMessageSendRequest qaChatMessageRequest) {
        log.info("id: {}, mid: {}, title: {}, send message:{}", qaChatMessageRequest.getId(), qaChatMessageRequest.getMid(), qaChatMessageRequest.getTitle(), qaChatMessageRequest.getMessage());

        ChatItem chatItem = SseUtil.CHAT_ITEMS.get(qaChatMessageRequest.getId());

        List<Message> messages = new CopyOnWriteArrayList<>();
        if (SseUtil.USER_MESSAGES.containsKey(qaChatMessageRequest.getId())) {
            List<Message> messageList = SseUtil.USER_MESSAGES.get(qaChatMessageRequest.getId());
            if ("1".equals(chatItem.getHistory())) {
                messages = new CopyOnWriteArrayList<>();
            }
            if ("2".equals(chatItem.getHistory())) {
                messages = messageList.stream()
                        .skip(Math.max(0, messageList.size() - 5))
                        .collect(Collectors.toList());
            }
            if ("3".equals(chatItem.getHistory())) {
                messages = messageList.stream()
                        .skip(Math.max(0, messageList.size() - 10))
                        .collect(Collectors.toList());
            }
            if ("4".equals(chatItem.getHistory())) {
                messages = messageList;
            }
        }
        Message currentMessage = Message.builder().content(qaChatMessageRequest.getMessage()).role(Message.Role.USER).build();
        messages.add(currentMessage);
        SseUtil.USER_MESSAGES.put(qaChatMessageRequest.getId(), messages);

        SseEmitter currentSseEmitter = SseUtil.SSE_EMITTERS.get(qaChatMessageRequest.getMid());
        if (Objects.isNull(currentSseEmitter)) throw new ApiException(ResultStatus.FAIL, "未创建连接");
        OpenAiSseEventSourceListener openAIEventSourceListener = new OpenAiSseEventSourceListener(currentSseEmitter, qaChatMessageRequest);
        ChatCompletion completion = ChatCompletion.builder()
                .messages(messages).model(chatItem.getModel())
                .build();

        OpenAiStreamClient openAiStreamClient = OpenAiUtil.getOpenAiStreamClient();
        openAiStreamClient.streamChatCompletion(completion, openAIEventSourceListener);
        return Result.ok();
    }

}

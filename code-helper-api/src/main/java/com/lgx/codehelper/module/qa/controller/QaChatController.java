package com.lgx.codehelper.module.qa.controller;

import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.lgx.codehelper.common.listener.OpenAiSseEventSourceListener;
import com.lgx.codehelper.module.qa.model.dto.ChatItem;
import com.lgx.codehelper.module.qa.model.req.QaChatRequest;
import com.lgx.codehelper.util.OpenAiUtil;
import com.lgx.codehelper.util.SseUtil;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.BaseChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
     * 创建连接
     *
     * @param qaChatRequest /
     * @return /
     */
    @PostMapping("/create")
    public SseEmitter doCreatQaChat(@RequestBody QaChatRequest qaChatRequest) {
        log.info("id: {}, title: {}, create", qaChatRequest.getId(), qaChatRequest.getTitle());
        SseEmitter emitter = new SseEmitter(600000L);
        SseUtil.SSE_EMITTERS.put(qaChatRequest.getId(), emitter);
        emitter.onCompletion(() -> SseUtil.SSE_EMITTERS.remove(qaChatRequest.getId()));

        // 如果当前聊天不存在则创建
        if (!SseUtil.CHAT_ITEMS.containsKey(qaChatRequest.getId())) {
            SseUtil.CHAT_ITEMS.put(qaChatRequest.getId(), new ChatItem(qaChatRequest.getId(), qaChatRequest.getTitle()));
        }

        return emitter;
    }

    /**
     * 关闭连接
     *
     * @param qaChatRequest /
     */
    @PostMapping("/close")
    public Result<Object> doCloseQaChat(@RequestBody QaChatRequest qaChatRequest) {
        log.info("id: {}, title: {}, close", qaChatRequest.getId(), qaChatRequest.getTitle());
        SseEmitter sse = SseUtil.SSE_EMITTERS.get(qaChatRequest.getId());
        if (sse != null) {
            sse.complete();
            // 删除SSE和用户聊天
            SseUtil.SSE_EMITTERS.remove(qaChatRequest.getId());
        }
        return Result.build(ResultStatus.SUCCESS, "关闭成功");
    }

    @PostMapping("/send")
    @ResponseBody
    public void doSend(@RequestBody QaChatRequest qaChatRequest, HttpServletResponse response) {
        log.info("id: {}, title: {}, send message:{}", qaChatRequest.getId(), qaChatRequest.getTitle(), qaChatRequest.getMessage());
        List<Message> messages = new CopyOnWriteArrayList<>();
        if (SseUtil.USER_MESSAGES.containsKey(qaChatRequest.getId())) {
            messages = SseUtil.USER_MESSAGES.get(qaChatRequest.getId());
        }
        Message currentMessage = Message.builder().content(qaChatRequest.getMessage()).role(Message.Role.USER).build();
        messages.add(currentMessage);
        SseUtil.USER_MESSAGES.put(qaChatRequest.getId(), messages);

        SseEmitter currentSseEmitter = SseUtil.SSE_EMITTERS.get(qaChatRequest.getId());
        if (Objects.isNull(currentSseEmitter)) throw new ApiException(ResultStatus.FAIL, "未创建连接");
        OpenAiSseEventSourceListener openAIEventSourceListener = new OpenAiSseEventSourceListener(currentSseEmitter, qaChatRequest);
        ChatCompletion completion = ChatCompletion.builder()
                .messages(messages).model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .build();

        OpenAiStreamClient openAiStreamClient = OpenAiUtil.getOpenAiStreamClient();
        openAiStreamClient.streamChatCompletion(completion, openAIEventSourceListener);
    }

    /**
     * 删除聊天
     *
     * @param qaChatRequest /
     */
    @PostMapping("/delete")
    public Result<Object> doDeleteQaChat(@RequestBody QaChatRequest qaChatRequest) {
        log.info("id: {}, title: {}, delete", qaChatRequest.getId(), qaChatRequest.getTitle());
        SseEmitter sse = SseUtil.SSE_EMITTERS.get(qaChatRequest.getId());
        if (sse != null) {
            sse.complete();
            // 删除SSE和用户聊天
            SseUtil.CHAT_ITEMS.remove(qaChatRequest.getId());
            SseUtil.SSE_EMITTERS.remove(qaChatRequest.getId());
        }
        // 删除聊天记录
        SseUtil.USER_MESSAGES.remove(qaChatRequest.getId());
        return Result.build(ResultStatus.SUCCESS, "删除成功");
    }

    /**
     * 获取聊天列表
     */
    @PostMapping("/query/chats")
    public Result<List<ChatItem>> doQueryChatList() {
        ConcurrentHashMap.KeySetView<Long, ChatItem> keySet = SseUtil.CHAT_ITEMS.keySet();
        List<Long> comparatorKeys = keySet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        List<ChatItem> res = new ArrayList<>();
        for (Long comparatorKey : comparatorKeys) {
            ChatItem chatItem = SseUtil.CHAT_ITEMS.get(comparatorKey);
            res.add(chatItem);
        }
        return Result.build(ResultStatus.SUCCESS, "查找成功", true, res);
    }

    /**
     * 获取聊天记录
     */
    @PostMapping("/query/chats/{id}")
    public Result<List<Message>> doQueryChatMessageList(@PathVariable("id") Long id) {
        List<Message> messages = SseUtil.USER_MESSAGES.get(id);
        return Result.build(ResultStatus.SUCCESS, "查找成功", true, messages);
    }

}

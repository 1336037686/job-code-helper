package com.lgx.codehelper.module.qa.service.impl;

import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.lgx.codehelper.common.filter.auth.UserInfoContextHolder;
import com.lgx.codehelper.common.listener.OpenAiSseEventSourceListener;
import com.lgx.codehelper.module.qa.domain.Chat;
import com.lgx.codehelper.module.qa.model.request.QaChatMessageSendRequest;
import com.lgx.codehelper.module.qa.service.ChatService;
import com.lgx.codehelper.module.qa.service.MessageService;
import com.lgx.codehelper.module.qa.service.QaChatService;
import com.lgx.codehelper.util.OpenAiUtil;
import com.lgx.codehelper.util.SseUtil;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:57
 */
@Service
@Slf4j
public class QaChatServiceImpl implements QaChatService {

    @Resource
    private ChatService chatService;

    @Resource
    private MessageService messageService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void send(QaChatMessageSendRequest qaChatMessageRequest) {
        // 设置UserId
        qaChatMessageRequest.setUserId(UserInfoContextHolder.getUserInfo().getId());

        // 获取数据
        Chat chatItem = chatService.getById(qaChatMessageRequest.getId());
        List<com.lgx.codehelper.module.qa.domain.Message> messageList = messageService.lambdaQuery()
                .eq(com.lgx.codehelper.module.qa.domain.Message::getChatId, qaChatMessageRequest.getId())
                .eq(com.lgx.codehelper.module.qa.domain.Message::getUserId, UserInfoContextHolder.getUserInfo().getId())
                .orderByAsc(com.lgx.codehelper.module.qa.domain.Message::getCreateTime).list();

        // 筛选条数
        if ("1".equals(chatItem.getHistory())) messageList = new CopyOnWriteArrayList<>();
        if ("2".equals(chatItem.getHistory())) messageList = messageList.stream().skip(Math.max(0, messageList.size() - 5)).collect(Collectors.toList());
        if ("3".equals(chatItem.getHistory())) messageList = messageList.stream().skip(Math.max(0, messageList.size() - 10)).collect(Collectors.toList());
        if ("4".equals(chatItem.getHistory())) messageList = messageList;

        // 构建历史消息
        List<Message> messages = messageList.stream()
                .map(x -> Message.builder().content(x.getContent()).role(x.getRole()).build())
                .collect(Collectors.toList());

        // 构建当前消息
        Message currentMessage = Message.builder().content(qaChatMessageRequest.getMessage()).role(Message.Role.USER).build();
        messages.add(currentMessage);

        // 保存消息到数据库
        com.lgx.codehelper.module.qa.domain.Message message = new com.lgx.codehelper.module.qa.domain.Message();
        message.setUserId(UserInfoContextHolder.getUserInfo().getId());
        message.setChatId(qaChatMessageRequest.getId());
        message.setContent(qaChatMessageRequest.getMessage());
        message.setRole(Message.Role.USER.getName());
        messageService.save(message);

        // 调用接口
        SseEmitter currentSseEmitter = SseUtil.SSE_EMITTERS.get(qaChatMessageRequest.getMid());
        if (Objects.isNull(currentSseEmitter)) throw new ApiException(ResultStatus.FAIL, "未创建连接");
        OpenAiSseEventSourceListener openAIEventSourceListener = new OpenAiSseEventSourceListener(currentSseEmitter, qaChatMessageRequest);
        ChatCompletion completion = ChatCompletion.builder()
                .messages(messages).model(chatItem.getModel())
                .build();
        OpenAiStreamClient openAiStreamClient = OpenAiUtil.getOpenAiStreamClient();
        openAiStreamClient.streamChatCompletion(completion, openAIEventSourceListener);
    }


}

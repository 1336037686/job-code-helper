package com.lgx.codehelper.common.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgx.codehelper.common.filter.auth.UserInfoContextHolder;
import com.lgx.codehelper.module.qa.model.request.QaChatMessageSendRequest;
import com.lgx.codehelper.module.qa.service.MessageService;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

/**
 * 描述：OpenAIEventSourceListener
 *
 * @author https:www.unfbx.com
 * @date 2023-02-22
 */
@Slf4j
public class OpenAiSseEventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    private MessageService messageService;

    private QaChatMessageSendRequest requestParam;

    private StringBuffer completeMessage = new StringBuffer();

    public OpenAiSseEventSourceListener(SseEmitter sseEmitter, QaChatMessageSendRequest requestParam) {
        this.sseEmitter = sseEmitter;
        this.requestParam = requestParam;
        this.messageService = SpringUtil.getBean(MessageService.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if ("[DONE]".equals(data)) {
            log.info("OpenAI返回数据结束了");
            sseEmitter.send(SseEmitter.event().id("[DONE]").data("[DONE]").reconnectTime(30000));
            // 将返回消息添加到消息列表，可能再返回消息时候就被干掉
            com.lgx.codehelper.module.qa.domain.Message message = new com.lgx.codehelper.module.qa.domain.Message();
            message.setUserId(requestParam.getUserId());
            message.setChatId(requestParam.getId());
            message.setContent(completeMessage.toString());
            message.setRole(Message.Role.ASSISTANT.getName());
            messageService.save(message);
            // 传输完成后自动关闭sse
            sseEmitter.complete();
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        ChatCompletionResponse completionResponse = mapper.readValue(data, ChatCompletionResponse.class); // 读取Json
        try {
            Message delta = completionResponse.getChoices().get(0).getDelta();
            if (Objects.isNull(delta)) return;
            completeMessage.append(StringUtils.isNotEmpty(delta.getContent()) ? delta.getContent() : "");
            sseEmitter.send(SseEmitter.event().id(completionResponse.getId()).data(delta).reconnectTime(30000));
        } catch (Exception e) {
            log.error("sse信息推送失败！");
            eventSource.cancel();
            e.printStackTrace();
        }
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }

}

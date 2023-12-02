package com.lgx.codehelper.common.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgx.codehelper.module.qa.model.req.QaChatRequest;
import com.lgx.codehelper.util.SseUtil;
import com.unfbx.chatgpt.entity.chat.BaseMessage;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
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

    private QaChatRequest requestParam;

    private StringBuffer completeMessage = new StringBuffer();

    public OpenAiSseEventSourceListener(SseEmitter sseEmitter, QaChatRequest requestParam) {
        this.sseEmitter = sseEmitter;
        this.requestParam = requestParam;
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
            if (SseUtil.USER_MESSAGES.containsKey(requestParam.getId())) {
                List<Message> messages = SseUtil.USER_MESSAGES.get(requestParam.getId());
                messages.add(Message.builder().content(completeMessage.toString()).role(Message.Role.ASSISTANT).build());
            }
            // 传输完成后自动关闭sse
            sseEmitter.complete();
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        ChatCompletionResponse completionResponse = mapper.readValue(data, ChatCompletionResponse.class); // 读取Json
        try {
            completeMessage.append(completionResponse.getChoices().get(0).getDelta());
            Message delta = completionResponse.getChoices().get(0).getDelta();
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

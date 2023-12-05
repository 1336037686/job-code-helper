package com.lgx.codehelper.module.qa.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.lgx.codehelper.common.filter.auth.UserInfoContextHolder;
import com.lgx.codehelper.common.listener.OpenAiSseEventSourceListener;
import com.lgx.codehelper.module.qa.domain.Chat;
import com.lgx.codehelper.module.qa.model.dto.ChatItem;
import com.lgx.codehelper.module.qa.model.request.QaChatItemRequest;
import com.lgx.codehelper.module.qa.model.request.QaChatMessageRequest;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
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

    @Resource
    private QaChatService qaChatService;

    @Resource
    private ChatService chatService;

    @Resource
    private MessageService messageService;

    /**
     * 创建聊天
     * @param qaChatItemRequest /
     * @return /
     */
    @PostMapping("/create")
    public Result<Chat> doCreateQaChat(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        log.info("id: {}, title: {}, create", qaChatItemRequest.getId(), qaChatItemRequest.getTitle());
        Chat chat = new Chat();
        BeanUtil.copyProperties(qaChatItemRequest, chat);
        chat.setUserId(UserInfoContextHolder.getUserInfo().getId());
        boolean save = chatService.save(chat);
        if (Boolean.FALSE.equals(save)) throw new ApiException(ResultStatus.FAIL, "创建失败");
        return Result.ok(chat);
    }

    /**
     * 修改聊天
     * @param qaChatItemRequest /
     * @return
     */
    @PostMapping("/update")
    public Result<Chat> doUpdateQaChat(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        log.info("id: {}, title: {}, update", qaChatItemRequest.getId(), qaChatItemRequest.getTitle());
        Chat chat = chatService.getById(qaChatItemRequest.getId());
        if (Objects.isNull(chat)) {
            throw new ApiException(ResultStatus.FAIL, "当前聊天不存在");
        }
        BeanUtil.copyProperties(qaChatItemRequest, chat);
        boolean update = chatService.updateById(chat);
        if (Boolean.FALSE.equals(update)) throw new ApiException(ResultStatus.FAIL, "更新失败");
        return Result.ok(chat);
    }

    /**
     * 删除聊天
     * @param qaChatItemRequest /
     */
    @PostMapping("/delete/{id}")
    public Result<Object> doDeleteQaChat(@PathVariable("id") Long id) {
        log.info("id: {}, delete", id);
        boolean remove = chatService.removeById(id);
        if (Boolean.FALSE.equals(remove)) throw new ApiException(ResultStatus.FAIL, "删除失败");
        return Result.ok(remove);
    }

    /**
     * 获取聊天列表
     */
    @PostMapping("/query")
    public Result<List<Chat>> doQueryChatList() {
        List<Chat> chats = chatService.lambdaQuery()
                .eq(Chat::getUserId, UserInfoContextHolder.getUserInfo().getId())
                .orderByDesc(Chat::getCreateTime).list();
        return Result.ok(chats);
    }

    /**
     * 获取聊天记录
     */
    @PostMapping("/query/{id}")
    public Result<List<com.lgx.codehelper.module.qa.domain.Message>> doQueryChatMessageList(@PathVariable("id") Long id) {
        List<com.lgx.codehelper.module.qa.domain.Message> messages = messageService.lambdaQuery()
                .eq(com.lgx.codehelper.module.qa.domain.Message::getChatId, id)
                .eq(com.lgx.codehelper.module.qa.domain.Message::getUserId, UserInfoContextHolder.getUserInfo().getId())
                .orderByAsc(com.lgx.codehelper.module.qa.domain.Message::getCreateTime).list();
        return Result.ok(messages);
    }

    /**
     * 清除聊天列表
     */
    @PostMapping("/clean")
    public Result<Boolean> doCleanChatMessages(@Valid @RequestBody QaChatItemRequest qaChatItemRequest) {
        LambdaQueryWrapper<com.lgx.codehelper.module.qa.domain.Message> wrapper = new LambdaQueryWrapper<>();
        wrapper .eq(com.lgx.codehelper.module.qa.domain.Message::getChatId, qaChatItemRequest.getId());
        wrapper.eq(com.lgx.codehelper.module.qa.domain.Message::getUserId, UserInfoContextHolder.getUserInfo().getId());
        boolean remove = messageService.remove(wrapper);
        if (Boolean.FALSE.equals(remove)) throw new ApiException(ResultStatus.FAIL, "清除失败");
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
            SseUtil.SSE_EMITTERS.remove(qaChatMessageRequest.getMid()); // 删除SSE和用户聊天
        }
        return Result.ok(true);
    }

    @PostMapping("/message/send")
    public Result<Object> doSend(@Valid @RequestBody QaChatMessageSendRequest qaChatMessageRequest) {
        log.info("id: {}, mid: {}, send message:{}", qaChatMessageRequest.getId(), qaChatMessageRequest.getMid(), qaChatMessageRequest.getMessage());
        qaChatService.send(qaChatMessageRequest);
        return Result.ok();
    }

}

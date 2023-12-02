package com.lgx.codehelper.util;

import cn.hutool.core.collection.CollectionUtil;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.sse.ConsoleEventSourceListener;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-01 15:55
 */
public class OpenAiUtil {

    // 地址
    private static String API_HOST = "https://www.wsopenai.com/";

    // key
    private static List<String> API_KEYS = List.of("sk-5wo49kAHinIXxH2fB7FfEe3830454c318858AfD0E17b427c");

    //国内访问需要做代理，国外服务器不需要
    private static OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)//自定义超时时间
            .writeTimeout(300, TimeUnit.SECONDS)//自定义超时时间
            .readTimeout(300, TimeUnit.SECONDS)//自定义超时时间
            .build();


    /**
     * 获取stream客户端
     * @return
     */
    public static OpenAiStreamClient getOpenAiStreamClient() {
        return OpenAiStreamClient.builder()
                .apiHost(API_HOST).apiKey(API_KEYS)
                //自定义key使用策略 默认随机策略
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(OK_HTTP_CLIENT).build();
    }

    /**
     * 获取结果
     * @param messages 消息列表
     * @return //结果
     */
    public static Message getResult(List<Message> messages) {
        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(API_KEYS).okHttpClient(OK_HTTP_CLIENT) //设置自定义的okHttpClient客户端
                .apiHost(API_HOST).build(); //自定义ApiHost
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(messages).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
        List<ChatChoice> choices = chatCompletionResponse.getChoices();
        if (CollectionUtil.isEmpty(choices)) {
            throw new ApiException(ResultStatus.FAIL, "OpenAI接口服务异常！");
        }
        return choices.get(0).getMessage();
    }


    public static void main(String[] args) {

        List<Message> msgList = new ArrayList<>();
        msgList.add(Message.builder().role(Message.Role.SYSTEM).content("你是一个资深的Java Web开发工程师，精通前后端编码开发、BUG修改、项目部署。").build());
        msgList.add(Message.builder().role(Message.Role.USER).content("请问现在Java常用的技术有哪些？").build());
        Message result = getResult(msgList);
        System.out.println(result);

    }

}

package com.lgx.codehelper.module.qa.service;

import com.lgx.codehelper.module.qa.model.request.QaChatMessageSendRequest;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-02 12:57
 */
public interface QaChatService {
    void send(QaChatMessageSendRequest qaChatMessageRequest);
}

package com.lgx.codehelper.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 白名单配置
 * @author 13360
 * @version 1.0
 * @date 2023-12-05 10:38
 */
@ConfigurationProperties(prefix = "coder-helper.whitelists")
@Component
@Data
public class WhitelistsProperties {

    /**
     * 白名单URL
     */
    private String[] whiteList = new String[] {
            "/api/v1/auth/user/register",
            "/api/v1/auth/user/login",
            "/api/v1/qa/chat/message/create"
    };

}

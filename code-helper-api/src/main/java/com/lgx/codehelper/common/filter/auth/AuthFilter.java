package com.lgx.codehelper.common.filter.auth;

import cn.hutool.extra.spring.SpringUtil;
import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.properties.WhitelistsProperties;
import com.lgx.codehelper.util.JWTUtil;
import com.lgx.codehelper.util.ResponseUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;


public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求的路径
        String requestURI = httpRequest.getRequestURI();
        if (isExcluded(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 在这里进行权限判断和Token信息的处理
            String token = httpRequest.getHeader("token");
            // 校验Token是否合法
            if (!JWTUtil.verify(token)) {
                ResponseUtil.out(httpResponse, Result.fail(ResultStatus.ILLEGAL_TOKEN, "无效的Token"));
            }
            // 解析Token获取登录信息
            UserInfo userInfo = JWTUtil.parseToken(token);
            // 将Token信息存储在自定义上下文中，以便后续使用
            UserInfoContextHolder.setUserInfo(userInfo);
            chain.doFilter(request, response);
        } finally {
            // 无论请求是否成功，都在最终清除Token
            UserInfoContextHolder.clearToken();
        }
    }

    /**
     * 校验是否在白名单内
     */
    private boolean isExcluded(String requestURI) {
        WhitelistsProperties whitelistsProperties = SpringUtil.getBean(WhitelistsProperties.class);
        return Arrays.stream(whitelistsProperties.getWhiteList()).anyMatch(requestURI::startsWith);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 过滤器初始化操作（可选）
    }

    @Override
    public void destroy() {
        // 过滤器销毁操作（可选）
    }
}

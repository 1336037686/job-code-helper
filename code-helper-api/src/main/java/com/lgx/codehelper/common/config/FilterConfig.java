package com.lgx.codehelper.common.config;

import com.lgx.codehelper.common.filter.auth.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 13360
 * @version 1.0
 * @description: TODO
 * @date 2023-12-05 20:07
 */

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> myFilter() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthFilter());
        registrationBean.addUrlPatterns("/api/*"); // 设置拦截路径
        registrationBean.setOrder(1); // 设置过滤器执行顺序
        return registrationBean;
    }
}
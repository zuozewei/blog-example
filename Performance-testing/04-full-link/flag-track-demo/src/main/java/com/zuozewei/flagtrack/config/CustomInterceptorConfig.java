package com.zuozewei.flagtrack.config;

import com.zuozewei.flagtrack.interceptor.FlagTrackInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @Description: 注册自定义拦截器
 * @Param:
 * @return:
 * @Author: zuozewei
 * @Date: 2021/3/26
 */

@Configuration
public class CustomInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private FlagTrackInterceptor flagTrackInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(flagTrackInterceptor);
    }
}
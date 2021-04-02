package com.zuozewei.flagtrack.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

/**
 * @Description: 标记追踪拦截器
 * @Param:
 * @return:
 * @Author: zuozewei
 * @Date: 2021/3/15
 */

@Component
public class FlagTrackInterceptor implements HandlerInterceptor {
    /**
     * 存储 flag
     */
    private static final ThreadLocal<String> FLAG_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 获取请求头 header 中传递的 flag，若没有，则 UUID 代替
         */
        String flag = Optional.ofNullable(request.getHeader("flag")).orElse(UUID.randomUUID().toString().replaceAll("-",""));
        // 请求前设置
        FLAG_THREAD_LOCAL.set(flag);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除，防止内存泄漏
        FLAG_THREAD_LOCAL.remove();
    }

    public static String getFlag() {
        return FLAG_THREAD_LOCAL.get();
    }

    public static void setFlag(String flag){
        FLAG_THREAD_LOCAL.set(flag);
    }

}

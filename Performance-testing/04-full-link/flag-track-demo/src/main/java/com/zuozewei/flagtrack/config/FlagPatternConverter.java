package com.zuozewei.flagtrack.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.zuozewei.flagtrack.interceptor.FlagTrackInterceptor;
import org.springframework.util.StringUtils;


/**
 * @Description:  自定义日志格式化
 * @Param:
 * @return:
 * @Author: zuozewei
 * @Date: 2021/3/26
 *
 */

public class FlagPatternConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        String flag = FlagTrackInterceptor.getFlag();
        return StringUtils.isEmpty(flag) ? "flag" : flag;
    }
}
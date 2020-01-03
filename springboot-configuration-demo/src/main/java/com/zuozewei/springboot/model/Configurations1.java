package com.zuozewei.springboot.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 描述:
 * 配置文件实体类
 *
 * @author zuozewei
 * @create 2019-12-20 16:15
 */

@Component
@Data
public class Configurations1 {

    @Value("${Server.host}")
    private String host;

    @Value("${Server.port}")
    private String port;

}

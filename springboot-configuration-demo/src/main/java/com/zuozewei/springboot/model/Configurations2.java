package com.zuozewei.springboot.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "Server")
public class Configurations2 {

    private String host;
    private String port;

}

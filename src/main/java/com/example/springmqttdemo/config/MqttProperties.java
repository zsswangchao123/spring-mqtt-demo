package com.example.springmqttdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "mqtt")
@Data
@Component
@Configuration
public class MqttProperties {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接地址
     */
    private String hostUrl;

    /**
     * 进-客户Id
     */
    private String inClientId;

    /**
     * 出-客户Id
     */
    private String outClientId;

    /**
     * 客户Id
     */
   // private String clientId;

    /**
     * 默认连接话题
     */
    private String defaultTopic;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 保持连接数
     */
    private int keepalive;

    /**是否清除session*/
    private boolean clearSession;
}

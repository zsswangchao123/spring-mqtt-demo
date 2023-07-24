package com.example.springmqttdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "baidu.ocr")
@Data
@Component
@Configuration
public class BaiduOcrProperties {

    private String appId;

    private String apiKey;

    private String secretKey;
}

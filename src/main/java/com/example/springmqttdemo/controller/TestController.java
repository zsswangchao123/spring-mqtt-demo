package com.example.springmqttdemo.controller;

import com.example.springmqttdemo.config.MsgGateway;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final MsgGateway msgGateway;

    public final Cache<String, Object> caffeineCache;
    @GetMapping("/test")
    public String test() {
        msgGateway.sendToMqtt("test/3", "test");
        return "test";
        //
    }
    //获取缓存
    @GetMapping("/get")
    public Object get(String key) {
        return caffeineCache.getIfPresent(key);
    }

    //清除缓存
    @GetMapping("/clean")
    public void clean(String key) {
        caffeineCache.invalidate(key);
    }

}

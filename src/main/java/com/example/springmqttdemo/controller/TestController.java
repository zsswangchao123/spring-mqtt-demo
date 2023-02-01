package com.example.springmqttdemo.controller;

import com.example.springmqttdemo.config.MsgGateway;
import com.example.springmqttdemo.config.MyMessageSource;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final MsgGateway msgGateway;

    public final Cache<String, Object> caffeineCache;

    private final MyMessageSource myMessageSource;
    @GetMapping("/test")
    public String test(String lang) {
        Locale locale1 = new Locale("zh");
        String test = myMessageSource.getSourceFromCache("test.name", null);
        return test;
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

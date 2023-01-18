package com.example.springmqttdemo.controller;

import com.example.springmqttdemo.config.MsgGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final MsgGateway msgGateway;
    @GetMapping("/test")
    public String test() {
        msgGateway.sendToMqtt("test/3", "test");
        return "test";
        //
    }

}

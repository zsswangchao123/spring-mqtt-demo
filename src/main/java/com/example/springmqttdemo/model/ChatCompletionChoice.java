package com.example.springmqttdemo.model;

import lombok.Data;

@Data
public class ChatCompletionChoice {

    Integer index;

    ChatMessage message;

    String finishReason;
}
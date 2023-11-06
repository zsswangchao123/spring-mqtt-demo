//package com.example.springmqttdemo.config;
//
//import org.springframework.integration.annotation.MessagingGateway;
//import org.springframework.integration.mqtt.support.MqttHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
//@Component
//public interface MsgGateway {
//
//	void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);
//
//	void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);
//
//}

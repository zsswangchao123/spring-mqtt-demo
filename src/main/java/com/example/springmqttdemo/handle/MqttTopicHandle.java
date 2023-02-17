package com.example.springmqttdemo.handle;

import com.example.springmqttdemo.annotation.MqttService;
import com.example.springmqttdemo.annotation.MqttTopic;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

/**
 * MqttTopicHandle
 *
 * @author hengzi
 * @date 2022/8/24
 */
@MqttService
@Slf4j
public class MqttTopicHandle {

	// 这里的 # 号是通配符
	@MqttTopic("test/#")
	public void test(Message<?> message) {

		log.info("test=" + message.getPayload());
	}

	// 这里的 + 号是通配符
	@MqttTopic("topic/+/+/up")
	public void up(Message<?> message) {

		log.info("up=" + message.getPayload());
	}

	// 注意 你必须先订阅
	@MqttTopic("topic/1/2/down")
	public void down(Message<?> message) {

		log.info("down=" + message.getPayload());
	}

}

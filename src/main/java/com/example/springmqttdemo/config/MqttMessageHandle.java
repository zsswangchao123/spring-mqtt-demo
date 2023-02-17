package com.example.springmqttdemo.config;

import cn.hutool.extra.spring.SpringUtil;
import com.example.springmqttdemo.annotation.MqttService;
import com.example.springmqttdemo.annotation.MqttTopic;
import com.example.springmqttdemo.model.MessageDate;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Import;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@Import(SpringUtil.class)
public class MqttMessageHandle implements MessageHandler {

	// 包含 @MqttService注解 的类(Component)
	public static Map<String, Object> mqttServices;

	public final Cache<String, Object> caffeineCache;

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		String payload = (String) message.getPayload();
		String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);

		log.info("topic: {}, payload: {}", topic, payload);
		List<MessageDate> cha = (List<MessageDate>) caffeineCache.get(topic, key -> {
			List<MessageDate> list = new ArrayList<>();
			return list;
		});
		MessageDate messageDate = new MessageDate();
		messageDate.setTopic(topic);
		messageDate.setMessage(payload);
		cha.add(messageDate);
		caffeineCache.put(topic, cha);

		// for (Map.Entry<String, Object> entry : mqttServices.entrySet()) {
		// // 把所有带有 @MqttService 的类遍历
		// Class<?> clazz = entry.getValue().getClass();
		// // 获取他所有方法
		// Method[] methods = clazz.getDeclaredMethods();
		// for ( Method method: methods ){
		// if (method.isAnnotationPresent(MqttTopic.class)){
		// // 如果这个方法有 这个注解
		// MqttTopic handleTopic = method.getAnnotation(MqttTopic.class);
		// if(isMatch(topic,handleTopic.value())){
		// // 并且 这个 topic 匹配成功
		// try {
		// method.invoke(SpringUtil.getBean(clazz),message);
		// return;
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// log.error("代理炸了");
		// } catch (InvocationTargetException e) {
		// log.error("执行 {} 方法出现错误",handleTopic.value(),e);
		// }
		// }
		// }
		// }
		// }
	}

	/**
	 * mqtt 订阅的主题与我实际的主题是否匹配
	 * @param topic 是实际的主题
	 * @param pattern 是我订阅的主题 可以是通配符模式
	 * @return 是否匹配
	 */
	public static boolean isMatch(String topic, String pattern) {

		if ((topic == null) || (pattern == null)) {
			return false;
		}

		if (topic.equals(pattern)) {
			// 完全相等是肯定匹配的
			return true;
		}

		if ("#".equals(pattern)) {
			// # 号代表所有主题 肯定匹配的
			return true;
		}
		String[] splitTopic = topic.split("/");
		String[] splitPattern = pattern.split("/");

		boolean match = true;

		// 如果包含 # 则只需要判断 # 前面的
		for (int i = 0; i < splitPattern.length; i++) {
			if (!"#".equals(splitPattern[i])) {
				// 不是# 号 正常判断
				if (i >= splitTopic.length) {
					// 此时长度不相等 不匹配
					match = false;
					break;
				}
				if (!splitTopic[i].equals(splitPattern[i]) && !"+".equals(splitPattern[i])) {
					// 不相等 且不等于 +
					match = false;
					break;
				}
			}
			else {
				// 是# 号 肯定匹配的
				break;
			}
		}

		return match;
	}

}

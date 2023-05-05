package com.example.springmqttdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.springmqttdemo.component.OpenAiApi;
import com.example.springmqttdemo.model.*;
import com.fhs.trans.service.impl.DictionaryTransService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestController {


	public final Cache<String, Object> caffeineCache;

	// 注入字典翻译服务
	private final DictionaryTransService dictionaryTransService;


	private final OpenAiApi openAiApi;

	@PostConstruct
	public void init() {
		// 在某处将字典缓存刷新到翻译服务中，以下是demo
		Map<String, String> transMap = new HashMap<>();
		transMap.put("0", "男");
		transMap.put("1", "女");
		dictionaryTransService.refreshCache("sex", transMap);

	}

	@GetMapping("/test")
	public Object test(String lang) {
		ChatMessage systemMessage = new ChatMessage("user", lang);
		List<ChatMessage> messages = Arrays.asList(systemMessage);
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
				.model("gpt-3.5-turbo-0301")
				.messages(messages)
				.user("testing")
				.max_tokens(500)
				.temperature(1.0)
				.build();
		ExecuteRet executeRet = openAiApi.post(PathConstant.COMPLETIONS.CREATE_CHAT_COMPLETION, JSONObject.toJSONString(chatCompletionRequest),
				null);
		JSONObject result = JSONObject.parseObject(executeRet.getRespStr());
		List<ChatCompletionChoice> choices = result.getJSONArray("choices").toJavaList(ChatCompletionChoice.class);
		System.out.println(choices.get(0).getMessage().getContent());
		ChatMessage context = new ChatMessage(choices.get(0).getMessage().getRole(), choices.get(0).getMessage().getContent());
		System.out.println(context.getContent());
		return context;
	}

	// 获取缓存
	@GetMapping("/get")
	public Object get(String key) {
		return caffeineCache.getIfPresent(key);
	}

	// 清除缓存
	@GetMapping("/clean")
	public void clean(String key) {
		caffeineCache.invalidate(key);
	}

}

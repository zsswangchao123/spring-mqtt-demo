package com.example.springmqttdemo.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springmqttdemo.config.MsgGateway;
import com.example.springmqttdemo.config.MyMessageSource;
import com.example.springmqttdemo.mapper.StudentMapper;
import com.example.springmqttdemo.model.School;
import com.example.springmqttdemo.model.Student;
import com.fhs.trans.service.impl.DictionaryTransService;
import com.fhs.trans.service.impl.SimpleTransService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestController {

	private final MsgGateway msgGateway;

	public final Cache<String, Object> caffeineCache;

	// 注入字典翻译服务
	private final DictionaryTransService dictionaryTransService;

	private final SimpleTransService simpleTransService;

	private final StudentMapper studentMapper;

	@PostConstruct
	public void init() {
		// 在某处将字典缓存刷新到翻译服务中，以下是demo
		Map<String, String> transMap = new HashMap<>();
		transMap.put("0", "男");
		transMap.put("1", "女");
		dictionaryTransService.refreshCache("sex", transMap);

	}

	@GetMapping("/test")
	public List<Student> test(String lang) {
		List<Student> list = studentMapper.selectList(Wrappers.query());

		return list;
		//
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

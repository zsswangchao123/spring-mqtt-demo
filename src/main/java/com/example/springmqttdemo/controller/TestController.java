package com.example.springmqttdemo.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.springmqttdemo.component.IOcrService;
import com.example.springmqttdemo.component.OpenAiApi;
import com.example.springmqttdemo.model.*;
import com.fhs.trans.service.impl.DictionaryTransService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class TestController {


	public final Cache<String, Object> caffeineCache;

	// 注入字典翻译服务
	private final DictionaryTransService dictionaryTransService;


	private final OpenAiApi openAiApi;

	private final IOcrService iOrcService;

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


	@RequestMapping("/actionOcr")
	@ResponseBody
	public String actionOcr(MultipartFile file) throws Exception {
		return this.iOrcService.actionOcr(file);
//		Tesseract tesseract = new Tesseract();
//		File convFile = convert(file);
//		tesseract.setDatapath("D:\\Program Files\\Tesseract-OCR\\tessdata");
//		tesseract.setLanguage("chi_sim");
//		String result = tesseract.doOCR(convFile);
//		System.out.println(result);

		//return "";

	}

	public static File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}


	// 单次对话
	@GetMapping("/chat")
	public String chatSingle(String content) {
		HashMap<String, String> msg = new HashMap<>();
		msg.put("grant_type", "client_credentials");
		msg.put("client_id", "RiHROZe6bwhdCahP1Ho02ixN");
		msg.put("client_secret","jAEvtWUvj4bSgW5jkfA8pycwWGLu3tqE");
		String post = HttpUtil.post("https://aip.baidubce.com/oauth/2.0/token", JSONObject.toJSONString(msg));
		return post;
	}


	public static void main(String[] args) {
		String token = HttpUtil.post("https://aip.baidubce.com/oauth/2.0/token??grant_type=client_credentials&client_id=RiHROZe6bwhdCahP1Ho02ixN&client_secret=jAEvtWUvj4bSgW5jkfA8pycwWGLu3tqE", JSONObject.toJSONString(new HashMap<>()));
		System.out.println(token);
		JSONObject jsonObject = JSONObject.parseObject(token);
		String accessToken = jsonObject.getString("access_token");
		Integer expires_in = jsonObject.getInteger("expires_in");
		System.out.println(accessToken);
		System.out.println(expires_in);

		HashMap<String, String> msg = new HashMap<>();
		msg.put("role", "user");
		msg.put("content", "我叫什么");

		ArrayList<HashMap> messages = new ArrayList<>();
		messages.add(msg);

		HashMap<String, Object> requestBody = new HashMap<>();
		requestBody.put("messages", messages);
		String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
		String response = HttpUtil.post(url + "?access_token=" + accessToken, JSONUtil.toJsonStr(requestBody));
		System.out.println(response);
	}


}

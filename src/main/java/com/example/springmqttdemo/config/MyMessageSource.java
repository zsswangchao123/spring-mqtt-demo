package com.example.springmqttdemo.config;

import cn.hutool.core.collection.CollectionUtil;
import com.example.springmqttdemo.model.ConfigI18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 使用@Compnent("messageSource")注解注入
@Service("messageSource")
public class MyMessageSource extends AbstractMessageSource implements ResourceLoaderAware {
    private final Logger logger = LoggerFactory.getLogger(MyMessageSource.class);

    ResourceLoader resourceLoader;

    // 这个是用来缓存数据库中获取到的配置的 数据库配置更改的时候可以调用reload方法重新加载
    private static final Map<String, Map<String, String>> LOCAL_CACHE = new ConcurrentHashMap<>(256);

    @Autowired
    private HttpServletRequest request;

    /**
     * 初始化
     * Java中该注解的说明：@PostConstruct该注解被用来修饰一个非静态的void（）方法。
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     * PostConstruct在构造函数之后执行，init（）方法之前执行。
     */
    @PostConstruct
    public void init() {
        this.reload();
    }

    /**
     * 重新将数据库中的国际化配置加载
     */
    public void reload() {
        LOCAL_CACHE.clear();
        LOCAL_CACHE.putAll(loadAllMessageResourcesFromDB());
    }

    /**
     * 从数据库中获取所有国际化配置 这边可以根据自己数据库表结构进行相应的业务实现
     * 对应的语言能够取出来对应的值就行了 无需一定要按照这个方法来
     */
    public Map<String, Map<String, String>> loadAllMessageResourcesFromDB() {
        // 获取数据库配置
        List<ConfigI18n> list = new ArrayList<>();
        ConfigI18n configI18n = new ConfigI18n();
        configI18n.setLanguage("zh");
        configI18n.setModel("test");
        configI18n.setModelId("name");
        configI18n.setName("test");
        configI18n.setText("测试");
        configI18n.setId("1");
        ConfigI18n configI18n2 = new ConfigI18n();
        configI18n2.setLanguage("en");
        configI18n2.setModel("test");
        configI18n2.setModelId("name");
        configI18n2.setName("test");
        configI18n2.setText("test");
        configI18n2.setId("1");
        list.add(configI18n);
        list.add(configI18n2);
        if (CollectionUtil.isNotEmpty(list)) {
            final Map<String, String> zhCnMessageResources = new HashMap<>(list.size());
            final Map<String, String> enUsMessageResources = new HashMap<>(list.size());
            final Map<String, String> myMessageResources = new HashMap<>(list.size());
            for (ConfigI18n item : list) {
                // 根据不同语言，分配到对应语言的值中
                if (item.getLanguage().equals("zh")){
                    zhCnMessageResources.put(item.getModel() + "." + item.getModelId(), item.getText());
                }else if (item.getLanguage().equals("en")){
                    enUsMessageResources.put(item.getModel() + "." + item.getModelId(), item.getText());
                }else if (item.getLanguage().equals("my")){
                    myMessageResources.put(item.getModel() + "." + item.getModelId(), item.getText());
                }
            }

            // 加入缓存
            LOCAL_CACHE.put("zh", zhCnMessageResources);
            LOCAL_CACHE.put("en", enUsMessageResources);
            LOCAL_CACHE.put("my", myMessageResources);
        }
        return new HashMap<>();
    }

    /**
     * 从缓存中取出国际化配置对应的数据 或者从父级获取
     *
     * @param code
     * @param locale 可以为null, 表示从当前HttpServletRequest中获取语言
     * @return
     */
    public String getSourceFromCache(String code, Locale locale) {
        String language = locale == null ? RequestContextUtils.getLocale(request).getLanguage() : locale.getLanguage();
        // 获取缓存中对应语言的所有数据项
        Map<String, String> props = LOCAL_CACHE.get(language);
        if (null != props && props.containsKey(code)) {
            // 如果对应语言中能匹配到数据项，那么直接返回
            return props.get(code);
        } else {
            // 如果对应语言中不能匹配到数据项，从上级获取返回
            try {
                if (null != this.getParentMessageSource()) {
                    return this.getParentMessageSource().getMessage(code, null, locale);
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            // 如果上级也没有找到，那么返回请求键值
            return code;
        }
    }

    // 下面三个重写的方法是比较重要的
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader == null ? new DefaultResourceLoader() : resourceLoader);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String msg = getSourceFromCache(code, locale);
        MessageFormat messageFormat = new MessageFormat(msg, locale);
        return messageFormat;
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return getSourceFromCache(code, locale);
    }

}

package com.example.springmqttdemo.config;


import com.example.springmqttdemo.annotation.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * MqttConfig
 *
 * @author hengzi
 * @date 2022/8/23
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConfig implements InitializingBean {


    /**
     *  以下属性将在配置文件中读取
     **/
    private final MqttProperties mqttProperties;

    private final MqttMessageHandle mqttMessageHandle;

    private final ConfigurableListableBeanFactory beanFactory;
    //Mqtt 客户端工厂
    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(){
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(mqttProperties.getHostUrl().split(","));
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    // Mqtt 管道适配器
    @Bean
    public MqttPahoMessageDrivenChannelAdapter adapter(MqttPahoClientFactory factory){
        return new MqttPahoMessageDrivenChannelAdapter(mqttProperties.getInClientId(),factory,mqttProperties.getDefaultTopic().split(","));
    }


    // 消息生产者
    @Bean
    public MessageProducer mqttInbound(MqttPahoMessageDrivenChannelAdapter adapter){
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        //入站投递的通道
        adapter.setOutputChannel(mqttInboundChannel());
        adapter.setQos(1);
        adapter.addTopic("$queue/test/#");
        return adapter;
    }


    // 出站处理器
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory factory){
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(mqttProperties.getOutClientId(),factory);
        handler.setAsync(true);
        handler.setConverter(new DefaultPahoMessageConverter());
        handler.setDefaultTopic(mqttProperties.getDefaultTopic().split(",")[0]);
        return handler;
    }

    @Bean
    public ThreadPoolTaskExecutor mqttThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 最大可创建的线程数
        int maxPoolSize = 200;
        executor.setMaxPoolSize(maxPoolSize);
        // 核心线程池大小
        int corePoolSize = 50;
        executor.setCorePoolSize(corePoolSize);
        // 队列最大长度
        int queueCapacity = 1000;
        executor.setQueueCapacity(queueCapacity);
        // 线程池维护线程所允许的空闲时间
        int keepAliveSeconds = 300;
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Bean
    //使用ServiceActivator 指定接收消息的管道为 mqttInboundChannel，投递到mqttInboundChannel管道中的消息会被该方法接收并执行
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public MessageHandler handleMessage() {
        return mqttMessageHandle;
    }

    //出站消息管道，
    @Bean
    public MessageChannel mqttOutboundChannel(){
        return new DirectChannel();
    }


    // 入站消息管道
    @Bean
    public MessageChannel mqttInboundChannel(){
        return new ExecutorChannel(mqttThreadPoolTaskExecutor());
    }

    @Override
    public void afterPropertiesSet() {
        log.info("MqttConfig 初始化完成");
        MqttMessageHandle.mqttServices = beanFactory.getBeansWithAnnotation(MqttService.class);
    }
}



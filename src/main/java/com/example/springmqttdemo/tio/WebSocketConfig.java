package com.example.springmqttdemo.tio;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tio.cluster.TioClusterConfig;
import org.tio.cluster.redisson.RedissonTioClusterTopic;
import org.tio.server.ServerTioConfig;
import org.tio.utils.thread.pool.DefaultThreadFactory;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;
import org.tio.utils.thread.pool.TioCallerRunsPolicy;
import org.tio.websocket.server.WsServerStarter;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: zdd
 * @date: 2023/05/10
 */
@RequiredArgsConstructor
@Configuration
public class WebSocketConfig {
    @Value("${tio.websocket.server.port}")
    public int wsPort;
    @Value("${tio.websocket.server.heartbeat-timeout}")
    private int wsTimeout;
    /**
     * 注入消息处理器
     */
    private final MyWebSocketMsgHandler myWsMsgHandler;

    private final RedissonClient redissonClient;

    /**
     * TIO-WEBSOCKET 配置信息
     */
    public static ServerTioConfig serverTioConfig;

    @Bean
    public WsServerStarter wsServerStarter() throws IOException {
        // 设置处理器
        WsServerStarter wsServerStarter = new WsServerStarter(wsPort, myWsMsgHandler, getTioExecutor(), getGroupExecutor());
        // 获取到ServerTioConfig
        serverTioConfig = wsServerStarter.getServerTioConfig();
        // 设置心跳超时时间，默认：1000 * 120
        serverTioConfig.setHeartbeatTimeout(wsTimeout);

        serverTioConfig.setServerAioListener(ShowcaseServerTioListener.me);

        //实例化t-io集群配置
        TioClusterConfig tioClusterConfig = new TioClusterConfig(new RedissonTioClusterTopic("pzg", redissonClient));
        //开启群组集群-默认不集群
        tioClusterConfig.setCluster4group(true);
        //配置t-io集群
        serverTioConfig.setTioClusterConfig(tioClusterConfig);
        // 启动
        wsServerStarter.start();
        return wsServerStarter;
    }

    public static ThreadPoolExecutor getGroupExecutor() {
        int CORE_POOL_SIZE_FOR_GROUP = Integer.getInteger("TIO_CORE_POOL_SIZE_FOR_GROUP", Math.min(SysCommonConstant.AVAILABLE_PROCESSORS * 3, 64));
        int MAX_POOL_SIZE_FOR_GROUP = Integer.getInteger("TIO_MAX_POOL_SIZE_FOR_GROUP", Math.min(SysCommonConstant.AVAILABLE_PROCESSORS * 8, 128));
        LinkedBlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue();
        String threadName = "tio-group";
        DefaultThreadFactory threadFactory = DefaultThreadFactory.getInstance(threadName, 10);
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new TioCallerRunsPolicy();
        ThreadPoolExecutor groupExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE_FOR_GROUP, MAX_POOL_SIZE_FOR_GROUP, 10L, TimeUnit.SECONDS, runnableQueue, threadFactory, callerRunsPolicy);
        groupExecutor.prestartCoreThread();
        return groupExecutor;

    }

    public static SynThreadPoolExecutor getTioExecutor() {
        int CORE_POOL_SIZE_FOR_TIO = Integer.getInteger("TIO_CORE_POOL_SIZE_FOR_TIO", Math.min(SysCommonConstant.AVAILABLE_PROCESSORS, 32));
        int MAX_POOL_SIZE_FOR_TIO = Integer.getInteger("TIO_MAX_POOL_SIZE_FOR_TIO", Math.min(SysCommonConstant.AVAILABLE_PROCESSORS * 3, 64));
        LinkedBlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue();
        String threadName = "tio-worker";
        DefaultThreadFactory defaultThreadFactory = DefaultThreadFactory.getInstance(threadName, 10);
        SynThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new TioCallerRunsPolicy();
        SynThreadPoolExecutor tioExecutor = new SynThreadPoolExecutor(CORE_POOL_SIZE_FOR_TIO, MAX_POOL_SIZE_FOR_TIO, 10L, runnableQueue, defaultThreadFactory, threadName, callerRunsPolicy);
        tioExecutor.prestartCoreThread();
        return tioExecutor;

    }
}

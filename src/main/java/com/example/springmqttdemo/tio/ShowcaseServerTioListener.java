package com.example.springmqttdemo.tio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsServerAioListener;

/**
 * @author tanyaowu
 * 用户根据情况来完成该类的实现
 */
public class ShowcaseServerTioListener extends WsServerAioListener {
    private static Logger log = LoggerFactory.getLogger(ShowcaseServerTioListener.class);

    public static final ShowcaseServerTioListener me = new ShowcaseServerTioListener();

    private ShowcaseServerTioListener() {

    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        super.onAfterConnected(channelContext, isConnected, isReconnect);
        if (log.isInfoEnabled()) {
            log.info("onAfterConnected\r\n{}", channelContext);
        }

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        super.onAfterSent(channelContext, packet, isSentSuccess);
        if (log.isInfoEnabled()) {
            log.info("onAfterSent\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
        super.onBeforeClose(channelContext, throwable, remark, isRemove);
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();
//        if (wsSessionContext != null && wsSessionContext.isHandshaked()) {
//            String msg = channelContext.userid + " 离开了";
//            //用tio-websocket，服务器发送到客户端的Packet都是WsResponse
//            WsResponse wsResponse = WsResponse.fromText(msg, "utf-8");
//            //群发
//            SetWithLock<String> groups = channelContext.getGroups();
//            if (groups != null) {
//                for (String group : groups.getObj()) {
//                    Tio.sendToGroup(channelContext.tioConfig, group, wsResponse);
//                }
//            }
//        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        super.onAfterDecoded(channelContext, packet, packetSize);
        if (log.isInfoEnabled()) {
            log.info("onAfterDecoded\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        super.onAfterReceivedBytes(channelContext, receivedBytes);
        if (log.isInfoEnabled()) {
            log.info("onAfterReceivedBytes\r\n{}", channelContext);
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        super.onAfterHandled(channelContext, packet, cost);
        if (log.isInfoEnabled()) {
            log.info("onAfterHandled\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

}

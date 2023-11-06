package com.example.springmqttdemo.tio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.maintain.Groups;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.io.IOException;
import java.util.Enumeration;


@Component
@Slf4j
@RequiredArgsConstructor
public class MyWebSocketMsgHandler implements IWsMsgHandler {


    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        return httpResponse;
    }

    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        // String userId = httpRequest.getParam("userId");
        String roomId = httpRequest.getParam("roomId");
        String userId = httpRequest.getParam("userId");
        channelContext.setUserid(userId);
        Tio.bindGroup(channelContext, roomId);
        Tio.bindUser(channelContext, roomId + ":" + userId);

//        int size = channelContext.getTioConfig().users.getMap().size();
//        log.info("当前在线人数：{}", size);
//        String msg = "{name:'admin',message:'" + channelContext.userid + " 进来了，共【" + size + "】人在线" + "'}";
//        Tio.sendToGroup(channelContext.getTioConfig(), roomId, WsResponse.fromText(msg, "utf-8"));
    }

    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        log.info("onBytes  接收到bytes消息");
        return null;
    }

    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        channelContext.setAttribute("kickOut", true);
        Tio.remove(channelContext, "WebSocket Close");
        return null;
    }

    @Override
    public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {
        //log.info("onText 接收到文本消息：" + s);

        SetWithLock<ChannelContext> contextSetWithLock = Tio.getByGroup(channelContext.getTioConfig(), "12");
        contextSetWithLock.getObj().forEach(c -> {
            log.info(c.userid);
            Tio.send(c, WsResponse.fromText(s, "utf-8"));
        });


        return null;
    }



}

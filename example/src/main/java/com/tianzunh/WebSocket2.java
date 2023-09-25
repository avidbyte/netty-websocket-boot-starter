package com.tianzunh;

import com.tianzunh.annotation.*;
import com.tianzunh.standard.Session;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aaron
 * @since 2022-10-21 10:15
 */
@Slf4j
@Component
@ServerEndpoint("/im2")
public class WebSocket2 {

    private static int onlineCount = 0;
    private static final Map<String, Session> CLIENTS = new ConcurrentHashMap<>();


    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers) {
        String token = headers.get("token");
        System.out.println("token:" + token);
    }

    @OnOpen
    public void onOpen(@PathParam("connectName") String connectName, Session session) {
        if (!StringUtils.hasLength(connectName)) {
            log.error("connectName is empty");
            return;
        }
        log.info("connectName={}", connectName);
        addOnlineCount();
        session.setAttribute("name", connectName);
        CLIENTS.put(connectName, session);
        log.info("New connection, current number of connections = {}", onlineCount);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String name = session.getAttribute("name");
        log.info("{}disconnected, current number of connections = {}", name, onlineCount);
        CLIENTS.remove(name);
        session.close();
        subOnlineCount();
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        log.info("message={}", message);
        String ping = "ping";
        if (ping.equals(message)) {
            session.sendText("pong");
        }
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    log.info("read idle");
                    break;
                case WRITER_IDLE:
                    log.info("write idle");
                    break;
                case ALL_IDLE:
                    log.info("all idle");
                    break;
                default:
                    break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        String name = session.getAttribute("name");
        log.error("A communication error occurred and the connection was closed = {}", name);
        CLIENTS.remove(name);
        session.close();
        subOnlineCount();
    }

    public void sendMessageTo(String message, Session session) {
        session.sendText(message);
    }

    public void sendMessageAll(String message) {
        log.info("Message sent by server: {}, number of clients: {}", message, onlineCount);
        for (Session session : CLIENTS.values()) {
            session.sendText(message);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket2.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket2.onlineCount--;
    }

}

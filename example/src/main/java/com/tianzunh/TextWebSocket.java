package com.tianzunh;

import com.tianzunh.annotation.*;
import com.tianzunh.standard.Session;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aaron
 * @since 2022-10-21 10:15
 */
@Slf4j
@Service
@ServerEndpoint("/chat/text")
public class TextWebSocket {
    private static final Map<String, Session> CLIENTS = new ConcurrentHashMap<>();


    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers) {
        String token = headers.get("token");
        System.out.println("token:" + token);
    }

    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        if (!StringUtils.hasLength(username)) {
            log.error("username is empty");
            return;
        }
        log.info("username={}", username);
        session.setAttribute("name", username);
        CLIENTS.put(username, session);
    }

    @OnClose
    public void onClose(Session session){
        String name = session.getAttribute("name");
        CLIENTS.remove(name);
        session.close();
    }

    @OnMessage
    public void onMessage(Session session, String message){
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
    public void onError(Session session, Throwable error){
        String name = session.getAttribute("name");
        log.error("A communication error occurred and the connection was closed = {}", name);
        CLIENTS.remove(name);
        session.close();
    }

    public void sendMessageTo(String message, Session session) {
        session.sendText(message);
    }

    public void sendMessageAll(String message) {
        for (Session session : CLIENTS.values()) {
            session.sendText(message);
        }
    }

}

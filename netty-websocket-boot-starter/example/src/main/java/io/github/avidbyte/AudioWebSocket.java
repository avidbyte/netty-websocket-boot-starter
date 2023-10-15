package io.github.avidbyte;


import io.github.avidbyte.annotation.*;
import io.github.avidbyte.standard.Session;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aaron
 * @since 2022-10-21 10:15
 */
@Slf4j
@Component
@ServerEndpoint("/chat/audio")
public class AudioWebSocket {

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
    public void onClose(Session session){
        String name = session.getAttribute("name");
        log.info("{}disconnected, current number of connections = {}", name, onlineCount);
        CLIENTS.remove(name);
        session.close();
        subOnlineCount();
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {

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
    public void onError(Session session, Throwable error)  {
        String name = session.getAttribute("name");
        log.error("A communication error occurred and the connection was closed = {}", name);
        CLIENTS.remove(name);
        session.close();
        subOnlineCount();
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        AudioWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        AudioWebSocket.onlineCount--;
    }

}

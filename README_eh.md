netty-websocket-boot-starter
===================================

[![License](https://img.shields.io/badge/license-Apache--2.0-4D7A97)](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.1-blue)](https://mvnrepository.com/artifact/io.github.avidbyte/netty-websocket-boot-starter/1.0.1)
[![Maven Central](https://img.shields.io/badge/PRs--welcome-red)](https://github.com/avidbyte/netty-websocket-boot-starter/pulls)

[English](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/README_en.md) | [简体中文](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/README.md)


### Overview
This is a lightweight, high-performance WebSocket framework based on Netty, which improves your WebSocket development experience and brings new WebSocket features to Spring Boot.
This project allows you to easily integrate WebSocket functionality into your Spring Boot project, providing the simplicity of Tomcat WebSocket while enjoying the performance and scalability advantages of Netty.

main feature:
- Seamless integration with Spring Boot
- Lightweight and high performance
- Simplify real-time application development
- Built on the powerful Netty framework

If you think this project is good, please click a Star
### Requirement
- jdk version 1.8 or 1.8+

### Quick Start

- add Dependencies:

```xml

<dependency>
    <groupId>io.github.avidbyte</groupId>
    <artifactId>netty-websocket-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

- annotate `@ServerEndpoint` on endpoint class，and annotate `@BeforeHandshake`,`@OnOpen`,`@OnClose`,`@OnError`,`@OnMessage`,`@OnBinary`,`@OnEvent` on the method.

```java
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
```

- use Websocket client to connect `ws://127.0.0.1:80/chat/text?username=Aaron`


### Annotation
###### @ServerEndpoint
> Each class marked with @ServerEndpoint will start a websocket service for it. Each service can specify its port in the path of the configuration file.

###### @BeforeHandshake
> when there is a connection accepted,the method annotated with `@BeforeHandshake` will be called  
> classes which be injected to the method are: Session,HttpHeaders

###### @OnOpen
> when there is a WebSocket connection completed,the method annotated with `@OnOpen` will be called  
> classes which be injected to the method are:Session,HttpHeaders...

###### @OnClose
> when a WebSocket connection closed,the method annotated with `@OnClose` will be called
> classes which be injected to the method are:Session

###### @OnError
> when a WebSocket connection throw Throwable, the method annotated with `@OnError` will be called
> classes which be injected to the method are:Session,Throwable

###### @OnMessage
> when a WebSocket connection received a message,the method annotated with `@OnMessage` will be called
> classes which be injected to the method are:Session,String

###### @OnBinary
> when a WebSocket connection received the binary,the method annotated with `@OnBinary` will be called
> classes which be injected to the method are:Session,byte[]

###### @OnEvent
> when a WebSocket connection received the event of Netty,the method annotated with `@OnEvent` will be called
> classes which be injected to the method are:Session,Object

### Configuration

> @ServerEndpoint only needs to configure the path, and the configuration corresponding to this path is all in application.yml
> The default configuration is host: 0.0.0.0, port: 80


#### Configuration by application.yml
```yaml
netty:
  websocket:
    endpoint:
      chat/text:
        port: 80
        reader-idle-time-seconds: 10
        writer-idle-time-seconds: 20
        all-idle-time-seconds: 30
      chat/audio:
        port: 81
        reader-idle-time-seconds: 10
        writer-idle-time-seconds: 20
        all-idle-time-seconds: 30
```

#### Configuration by application.properties
```properties
netty.websocket.endpoint.chat/text.port=80
netty.websocket.endpoint.chat/text.reader-idle-time-seconds=10
netty.websocket.endpoint.chat/text.writer-idle-time-seconds=20
netty.websocket.endpoint.chat/text.all-idle-time-seconds=30
netty.websocket.endpoint.chat/audio.port=81
netty.websocket.endpoint.chat/audio.reader-idle-time-seconds=10
netty.websocket.endpoint.chat/audio.writer-idle-time-seconds=20
netty.websocket.endpoint.chat/audio.all-idle-time-seconds=30
```

#### All configuration parameters


| property                                  | default          | description                                                                                                                |
|-------------------------------------------|------------------|----------------------------------------------------------------------------------------------------------------------------|
| host                                      | "0.0.0.0"        | host of WebSocket.`"0.0.0.0"` means all of local addresses                                                                 |
| port                                      | 80               | websocket service port                                                                                                     |
| boss-loop-group-threads                   | 1                | The number of threads of bossEventLoopGroup                                                                                |
| worker-loop-group-threads                 | 0                | The number of threads of workerEventLoopGroup                                                                              |
| use-compression-handler                   | false            | whether add WebSocketServerCompressionHandler to pipeline                                                                  |
| option-connect-timeout-millis             | 30000            | the same as `ChannelOption.CONNECT_TIMEOUT_MILLIS` in Netty                                                                |
| option-so-backlog                         | 128              | the same as `ChannelOption.SO_BACKLOG` in Netty                                                                            |
| child-option-write-spin-count             | 16               | the same as `ChannelOption.WRITE_SPIN_COUNT` in Netty                                                                      |
| child-option-write-buffer-high-water-mark | 64*1024          | the same as `ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK` in Netty,but use `ChannelOption.WRITE_BUFFER_WATER_MARK` in fact. |
| child-option-write-buffer-low-water-mark  | 32*1024          | the same as `ChannelOption.WRITE_BUFFER_LOW_WATER_MARK` in Netty,but use `ChannelOption.WRITE_BUFFER_WATER_MARK` in fact.  |
| child-option-so-rcv-buf                   | -1(mean not set) | the same as `ChannelOption.SO_RCVBUF` in Netty                                                                             |
| child-option-so-snd-buf                   | -1(mean not set) | the same as `ChannelOption.SO_SNDBUF` in Netty                                                                             |
| child-option-tcp-nodelay                  | true             | the same as `ChannelOption.TCP_NODELAY` in Netty                                                                           |
| child-option-so-keepalive                 | false            | the same as `ChannelOption.SO_KEEPALIVE` in Netty                                                                          |
| child-option-so-linger                    | -1               | the same as `ChannelOption.SO_LINGER` in Netty                                                                             |
| child-option-allow-half-closure           | false            | the same as `ChannelOption.ALLOW_HALF_CLOSURE` in Netty                                                                    |
| reader-idle-time-seconds                  | 0                | the same as `readerIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0        |
| writer-idle-time-seconds                  | 0                | the same as `writerIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0        |
| all-idle-time-seconds                     | 0                | the same as `allIdleTimeSeconds` in `IdleStateHandler` and add `IdleStateHandler` to `pipeline` when it is not 0           |
| max-frame-payload-length                  | 65536            | Maximum allowable frame payload length.                                                                                    |
| use-event-executor-group                  | true             | Whether to use another thread pool to perform time-consuming synchronous business logic                                    |
| event-executor-group-threads              | 16               | The number of threads of bossEventLoopGroup                                                                                |
| ssl-key-password                          | ""(mean not set) | the same as `server.ssl.key-password` in spring-boot                                                                       |
| ssl-key-store                             | ""(mean not set) | the same as `server.ssl.key-store` in spring-boot                                                                          |
| ssl-key-password                          | ""(mean not set) | the same as `server.ssl.key-store-password` in spring-boot                                                                 |
| ssl-key-store-type                        | ""(mean not set) | the same as `server.ssl.key-store-type` in spring-boot                                                                     |
| ssl-trust-store                           | ""(mean not set) | the same as `server.ssl.trust-store` in spring-boot                                                                        |
| ssl-trust-store-password                  | ""(mean not set) | the same as `server.ssl.trust-store-password` in spring-boot                                                               |
| ssl-trust-store-type                      | ""(mean not set) | the same as `server.ssl.trust-store-type` in spring-boot                                                                   |
| cors-origins                              | {}(mean not set) | the same as `@CrossOrigin#origins` in spring-boot                                                                          |
| cors-allow-credentials                    | ""(mean not set) | the same as `@CrossOrigin#allowCredentials` in spring-boot                                                                 |

---



### Change Log

#### 1.0.0

- Implement basic functions of websocket service

#### 1.0.1

- fix: adjust the project structure to solve the problem of jar dependencies not being found
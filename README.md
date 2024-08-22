netty-websocket-boot-starter
===================================

[![License](https://img.shields.io/badge/license-Apache--2.0-4D7A97)](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.1-blue)](https://mvnrepository.com/artifact/io.github.avidbyte/netty-websocket-boot-starter/1.0.1)
[![Maven Central](https://img.shields.io/badge/PRs--welcome-red)](https://github.com/avidbyte/netty-websocket-boot-starter/pulls)

[简体中文](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/README.md) ｜ [English](https://github.com/avidbyte/netty-websocket-boot-starter/blob/main/README_en.md) 


### 概述
这是一个轻量级、高性能的基于Netty的WebSocket框架，提升你的WebSocket开发体验，为Spring Boot带来全新的WebSocket功能。
该项目使你可以轻松集成WebSocket功能到你的Spring Boot项目中，提供了Tomcat WebSocket的简单性，同时享受Netty性能和可扩展性的优势。

主要特点：
- 无缝集成Spring Boot
- 轻量级和高性能
- 简化实时应用程序开发
- 基于强大的Netty框架构建

如果您觉得这个项目不错，请点一个Star吧
### 要求
- jdk version 1.8 or 1.8+

### 快速开始

- 添加依赖:

```xml

<dependency>
    <groupId>io.github.avidbyte</groupId>
    <artifactId>netty-websocket-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

- 在端点类上注释`@ServerEndpoint`，并在方法上注释`@BeforeHandshake`,`@OnOpen`,`@OnClose`,`@OnError`,`@OnMessage`,`@OnBinary`,`@OnEvent`。

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

- 使用Websocket客户端连接 `ws://127.0.0.1:80/chat/text?username=Aaron`


### Annotation
###### @ServerEndpoint
> 每个标有@ServerEndpoint的类都会为其启动一个websocket服务。 每个服务都可以在配置文件的路径中指定其端口。

###### @BeforeHandshake
> 当有新的连接进入时，将调用`@BeforeHandshake`注释的方法
> 注入到方法中的类有：Session、HttpHeaders

###### @OnOpen
> 当WebSocket连接完成时，会调用`@OnOpen`注解的方法
> 注入到方法中的类有：Session、HttpHeaders

###### @OnClose
> 当WebSocket连接关闭时，将调用`@OnClose`注释的方法
> 注入到方法中的类有：Session

###### @OnError
> 当WebSocket连接抛出Throwable时，将调用带有`@OnError`注释的方法
> 注入到方法中的类有：Session、Throwable

###### @OnMessage
> 当WebSocket连接收到消息时，将调用带有`@OnMessage`注释的方法
> 注入到方法中的类有：Session、String

###### @OnBinary
> 当WebSocket连接收到二进制文件时，将调用带有`@OnBinary`注释的方法
> 注入到方法中的类有：Session、byte[]

###### @OnEvent
> 当WebSocket连接收到Netty的事件时，会调用`@OnEvent`注解的方法
> 注入到方法中的类有：Session、Object

### Configuration

> @ServerEndpoint只需要配置路径，该路径对应的配置全部在application.yml 中
> 默认配置为host: 0.0.0.0, 端口: 80

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



#### 所有配置参数


| property                                  | default          | description                                                                                             |
|-------------------------------------------|------------------|---------------------------------------------------------------------------------------------------------|
| host                                      | "0.0.0.0"        | WebSocket 的主机。`"0.0.0.0"` 表示所有本地地址                                                                      |
| port                                      | 80               | websocket 服务端口                                                                                          |
| boss-loop-group-threads                   | 1                | bossEventLoopGroup 的线程数                                                                                 |
| worker-loop-group-threads                 | 0                | workerEventLoopGroup 的线程数                                                                               |
| use-compression-handler                   | false            | 是否将WebSocketServerCompressionHandler添加到管道                                                               |
| option-connect-timeout-millis             | 30000            | 与 Netty 中的 `ChannelOption.CONNECT_TIMEOUT_MILLIS` 相同                                                    |
| option-so-backlog                         | 128              | 与 Netty 中的 `ChannelOption.SO_BACKLOG` 相同                                                                |
| child-option-write-spin-count             | 16               | 与 Netty 中的 `ChannelOption.WRITE_SPIN_COUNT` 相同                                                          |
| child-option-write-buffer-high-water-mark | 64*1024          | 与 Netty 中的`ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK`相同，但实际上使用`ChannelOption.WRITE_BUFFER_WATER_MARK`。 |
| child-option-write-buffer-low-water-mark  | 32*1024          | 与 Netty 中的`ChannelOption.WRITE_BUFFER_LOW_WATER_MARK`相同，但实际上使用`ChannelOption.WRITE_BUFFER_WATER_MARK`。  |
| child-option-so-rcv-buf                   | -1(mean not set) | 与 Netty 中的`ChannelOption.SO_RCVBUF`相同                                                                   |
| child-option-so-snd-buf                   | -1(mean not set) | 与 Netty 中的`ChannelOption.SO_SNDBUF`相同                                                                   |
| child-option-tcp-nodelay                  | true             | 与 Netty 中的 `ChannelOption.TCP_NODELAY` 相同                                                               |
| child-option-so-keepalive                 | false            | 与 Netty 中的`ChannelOption.SO_KEEPALIVE`相同                                                                |
| child-option-so-linger                    | -1               | 与 Netty 中的 `ChannelOption.SO_LINGER` 相同                                                                 |
| child-option-allow-half-closure           | false            | 与 Netty 中的 `ChannelOption.ALLOW_HALF_CLOSURE` 相同                                                        |
| reader-idle-time-seconds                  | 0                | 与`IdleStateHandler`中的`readerIdleTimeSeconds`相同，当不为0时将`IdleStateHandler`添加到`pipeline`中                   |
| writer-idle-time-seconds                  | 0                | 与`IdleStateHandler`中的`writerIdleTimeSeconds`相同，当不为0时将`IdleStateHandler`添加到`pipeline`中                   |
| all-idle-time-seconds                     | 0                | 与`IdleStateHandler`中的`allIdleTimeSeconds`相同，当不为0时将`IdleStateHandler`添加到`pipeline`中                      |
| max-frame-payload-length                  | 65536            | 最大允许帧有效负载长度。                                                                                            |
| use-event-executor-group                  | true             | 是否使用另一个线程池来执行耗时的同步业务逻辑                                                                                  |
| event-executor-group-threads              | 16               | bossEventLoopGroup 的线程数                                                                                 |
| ssl-key-password                          | ""(mean not set) | 与 spring-boot 中的 `server.ssl.key-password` 相同                                                           |
| ssl-key-store                             | ""(mean not set) | 与 spring-boot 中的 `server.ssl.key-store` 相同                                                              |
| ssl-key-password                          | ""(mean not set) | 与 spring-boot 中的 `server.ssl.key-store-password` 相同                                                     |
| ssl-key-store-type                        | ""(mean not set) | 与 spring-boot 中的 `server.ssl.key-store-type` 相同                                                         |
| ssl-trust-store                           | ""(mean not set) | 与 spring-boot 中的 `server.ssl.trust-store` 相同                                                            |
| ssl-trust-store-password                  | ""(mean not set) | 与 spring-boot 中的 `server.ssl.trust-store-password` 相同                                                   |
| ssl-trust-store-type                      | ""(mean not set) | 与 spring-boot 中的 `server.ssl.trust-store-type` 相同                                                       |
| cors-origins                              | {}(mean not set) | 与 spring-boot 中的“@CrossOrigin#origins”相同                                                                |
| cors-allow-credentials                    | ""(mean not set) | 与 spring-boot 中的“@CrossOrigin#allowCredentials”相同                                                       |

---


### Explanation
#### WebSocketServerCompressionHandler
WebSocketServerCompressionHandler 是 Netty 中用于 WebSocket 消息压缩的处理器。它的主要作用是启用 WebSocket 消息压缩以减少数据传输的大小，从而提高网络传输效率。
#### ChannelOption.CONNECT_TIMEOUT_MILLIS
用于设置连接超时的时间（以毫秒为单位）。它用于控制在尝试建立连接时，如果连接不能在指定的时间内建立，将会触发连接超时异常;实际的超时行为可能会受到操作系统和网络条件的影响，因此超时时间并不总是精确的。但它可以作为一种有效的手段来避免无限等待并处理连接失败的情况。

### 更新日志

#### 1.0.0

- 实现 websocket 服务的基本功能
-
#### 1.0.1

- fix:调整项目结构，解决jar依赖找不到的问题


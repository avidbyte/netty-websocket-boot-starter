package org.avidbyte;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;

public class WebSocketClient {

    private static final String URL = "ws://127.0.0.1:80/im?connectName=test"; // 替换成实际的WebSocket服务器URL

    public static void main(String[] args) throws Exception {
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;

        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new WebSocketClientInitializer(sslCtx, uri));

            Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();

            // 等待WebSocket客户端关闭
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    private final URI uri;

    WebSocketClientInitializer(SslContext sslCtx, URI uri) {
        this.sslCtx = sslCtx;
        this.uri = uri;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
        }

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));

        // 更正WebSocketClientProtocolHandler的参数
        pipeline.addLast("websocketProtocol", new WebSocketClientProtocolHandler(uri,
                WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), 1280000));

        // 在WebSocketClientInitializer中初始化ChannelHandlerContext
        ChannelHandlerContext ctx = ch.pipeline().context(WebSocketClientHandler.class);
        pipeline.addLast(new WebSocketClientHandler(ctx,uri));
    }
}

class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final ChannelHandlerContext ctx;
    private final URI uri;

    public WebSocketClientHandler(ChannelHandlerContext ctx,URI uri) {
        this.ctx = ctx;
        this.handshaker = null; // 初始化handshaker为null
        this.uri=uri;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), 1280000);
        handshakeFuture = ctx.newPromise();
        handshaker.handshake(ctx.channel()); // 在这里初始化并进行握手
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.err.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.err.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            if (response.status() == HttpResponseStatus.SWITCHING_PROTOCOLS) {
                System.out.println("WebSocket Client received response:\n" + response);
            } else {
                System.err.println("WebSocket Client failed to connect");
            }
        } else if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            System.out.println("WebSocket Client received message: " + textFrame.text());
        } else if (msg instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (msg instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            ByteBuf content = binaryFrame.content();
            // 处理二进制消息
        } else if (msg instanceof ContinuationWebSocketFrame) {
            ContinuationWebSocketFrame continuationFrame = (ContinuationWebSocketFrame) msg;
            // 处理分片消息
        }
    }

    public void sendWebSocketMessage(String message) {
        Channel channel = ctx.channel();
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}

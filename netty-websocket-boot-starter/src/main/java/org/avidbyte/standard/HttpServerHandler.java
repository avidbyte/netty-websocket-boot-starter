package org.avidbyte.standard;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.avidbyte.support.ValueConstants;
import org.avidbyte.support.WsPathMatcher;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;

import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Aaron
 * @version 1.0
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final WebSocketEventServer webSocketEventServer;
    private final ServerEndpointConfig config;
    private final EventExecutorGroup eventExecutorGroup;
    private final boolean isCors;

    public HttpServerHandler(WebSocketEventServer webSocketEventServer, ServerEndpointConfig config, EventExecutorGroup eventExecutorGroup, boolean isCors) {
        this.webSocketEventServer = webSocketEventServer;
        this.config = config;
        this.eventExecutorGroup = eventExecutorGroup;
        this.isCors = isCors;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            handleHttpRequest(ctx, msg);
        } catch (TypeMismatchException e) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        } catch (Exception e) {
            FullHttpResponse res;
            res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        webSocketEventServer.doOnError(ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        webSocketEventServer.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse res;
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        HttpHeaders headers = req.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        if (StringUtils.isEmpty(host)) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!StringUtils.isEmpty(webSocketEventServer.getHost()) && !ValueConstants.DEFAULT_ADDRESS.equals(webSocketEventServer.getHost()) && !webSocketEventServer.getHost().equals(host.split(ValueConstants.COLON)[0])) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());

        Channel channel = ctx.channel();

        //path match
        String pattern = null;
        Set<WsPathMatcher> pathMatcherSet = webSocketEventServer.getPathMatcherSet();
        for (WsPathMatcher pathMatcher : pathMatcherSet) {
            if (pathMatcher.matchAndExtract(decoder, channel)) {
                pattern = pathMatcher.getPattern();
                break;
            }
        }

        if (pattern == null) {
            res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendHttpResponse(ctx, req, res);
            return;
        }

        String subprotocols = null;

        if (webSocketEventServer.hasBeforeHandshake(channel, pattern)) {
            webSocketEventServer.doBeforeHandshake(channel, req, pattern);
            if (!channel.isActive()) {
                return;
            }

            AttributeKey<String> subProtocolsAttrKey = AttributeKey.valueOf("subprotocols");
            if (channel.hasAttr(subProtocolsAttrKey)) {
                subprotocols = ctx.channel().attr(subProtocolsAttrKey).get();
            }
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), subprotocols, true, config.getMaxFramePayloadLength());
        WebSocketServerHandshaker webSocketServerHandshaker = wsFactory.newHandshaker(req);
        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());
            if (config.getReaderIdleTimeSeconds() != 0 || config.getWriterIdleTimeSeconds() != 0 || config.getAllIdleTimeSeconds() != 0) {
                pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
            }
            if (config.isUseCompressionHandler()) {
                pipeline.addLast(new WebSocketServerCompressionHandler());
            }
            pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
            if (config.isUseEventExecutorGroup()) {
                pipeline.addLast(eventExecutorGroup, new WebSocketServerHandler(webSocketEventServer));
            } else {
                pipeline.addLast(new WebSocketServerHandler(webSocketEventServer));
            }
            String finalPattern = pattern;
            webSocketServerHandshaker.handshake(channel, req).addListener(future -> {
                if (future.isSuccess()) {
                    if (isCors) {
                        pipeline.remove(CorsHandler.class);
                    }
                    webSocketEventServer.doOnOpen(channel, req, finalPattern);
                } else {
                    webSocketServerHandshaker.close(channel, new CloseWebSocketFrame());
                }
            });
        }

    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        int statusCode = res.status().code();
        if (statusCode != OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        HttpUtil.setContentLength(res, res.content().readableBytes());

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != OK.code()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}
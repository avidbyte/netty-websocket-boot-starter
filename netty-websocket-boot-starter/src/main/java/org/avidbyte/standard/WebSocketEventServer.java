package org.avidbyte.standard;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.avidbyte.support.*;
import org.springframework.beans.TypeMismatchException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Aaron
 * @since 1.0
 */
public class WebSocketEventServer {

    private static final AttributeKey<Object> IMPLEMENT_KEY = AttributeKey.valueOf("WEBSOCKET_IMPLEMENT");
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("WEBSOCKET_SESSION");
    private static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("WEBSOCKET_PATH");
    public static final AttributeKey<Map<String, String>> URI_TEMPLATE = AttributeKey.valueOf("WEBSOCKET_URI_TEMPLATE");
    public static final AttributeKey<Map<String, List<String>>> URI_PARAM = AttributeKey.valueOf("WEBSOCKET_URI_PARAM");
    private final Map<String, MethodMapping> pathMethodMappingMap = new HashMap<>();
    private final ServerEndpointConfig config;
    private final Set<WsPathMatcher> pathMatchers = new HashSet<>();
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(WebSocketEventServer.class);

    public WebSocketEventServer(MethodMapping methodMapping, ServerEndpointConfig config, String path) {
        addPathMethodMapping(path, methodMapping);
        this.config = config;
    }

    public boolean hasBeforeHandshake(Channel channel, String path) {
        MethodMapping methodMapping = getMethodMapping(path, channel);
        return methodMapping.getBeforeHandshake() != null;
    }

    public void doBeforeHandshake(Channel channel, FullHttpRequest req, String path) {
        MethodMapping methodMapping = null;
        methodMapping = getMethodMapping(path, channel);

        Object implement = null;
        try {
            implement = methodMapping.getEndpointInstance();
        } catch (Exception e) {
            LOGGER.error(e);
            return;
        }
        channel.attr(IMPLEMENT_KEY).set(implement);
        Session session = new Session(channel);
        channel.attr(SESSION_KEY).set(session);
        Method beforeHandshake = methodMapping.getBeforeHandshake();
        if (beforeHandshake != null) {
            try {
                beforeHandshake.invoke(implement, methodMapping.getBeforeHandshakeArgs(channel, req));
            } catch (TypeMismatchException e) {
                throw e;
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public void doOnOpen(Channel channel, FullHttpRequest req, String path) {
        MethodMapping methodMapping = getMethodMapping(path, channel);

        Object implement = channel.attr(IMPLEMENT_KEY).get();
        if (implement == null) {
            try {
                implement = methodMapping.getEndpointInstance();
                channel.attr(IMPLEMENT_KEY).set(implement);
            } catch (Exception e) {
                LOGGER.error(e);
                return;
            }
            Session session = new Session(channel);
            channel.attr(SESSION_KEY).set(session);
        }

        Method onOpenMethod = methodMapping.getOnOpen();
        if (onOpenMethod != null) {
            try {
                onOpenMethod.invoke(implement, methodMapping.getOnOpenArgs(channel, req));
            } catch (TypeMismatchException e) {
                LOGGER.error(e);
                throw e;
            } catch (InvocationTargetException e) {
                LOGGER.error(e);
                Throwable cause = e.getCause();
                cause.printStackTrace(); // 这将打印出实际的原因
            } catch (IllegalAccessException e) {
                LOGGER.error(e);
                e.printStackTrace();
            }
            catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public void doOnClose(Channel channel) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                return;
            }
        }
        if (methodMapping.getOnClose() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(IMPLEMENT_KEY).get();
            try {
                methodMapping.getOnClose().invoke(implement, methodMapping.getOnCloseArgs(channel));
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }


    public void doOnError(Channel channel, Throwable throwable) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnError() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(IMPLEMENT_KEY).get();
            try {
                Method method = methodMapping.getOnError();
                Object[] args = methodMapping.getOnErrorArgs(channel, throwable);
                method.invoke(implement, args);
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public void doOnMessage(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnMessage() != null) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            Object implement = channel.attr(IMPLEMENT_KEY).get();
            try {
                methodMapping.getOnMessage().invoke(implement, methodMapping.getOnMessageArgs(channel, textFrame));
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public void doOnBinary(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnBinary() != null) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            Object implement = channel.attr(IMPLEMENT_KEY).get();
            try {
                methodMapping.getOnBinary().invoke(implement, methodMapping.getOnBinaryArgs(channel, binaryWebSocketFrame));
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public void doOnEvent(Channel channel, Object evt) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnEvent() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(IMPLEMENT_KEY).get();
            try {
                methodMapping.getOnEvent().invoke(implement, methodMapping.getOnEventArgs(channel, evt));
            } catch (Throwable t) {
                LOGGER.error(t);
            }
        }
    }

    public String getHost() {
        return config.getHost();
    }

    public int getPort() {
        return config.getPort();
    }

    public Set<WsPathMatcher> getPathMatcherSet() {
        return pathMatchers;
    }

    public void addPathMethodMapping(String path, MethodMapping methodMapping) {
        pathMethodMappingMap.put(path, methodMapping);
        for (MethodArgumentResolver onOpenArgResolver : methodMapping.getOnOpenArgResolvers()) {
            if (onOpenArgResolver instanceof PathParamMethodArgumentResolver) {
                pathMatchers.add(new AntPathMatcherWrapper(path));
                return;
            }
        }
        pathMatchers.add(new DefaultPathMatcher(path));
    }

    private MethodMapping getMethodMapping(String path, Channel channel) {
        MethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            Attribute<String> attrPath = channel.attr(PATH_KEY);
            attrPath.set(path);
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                throw new RuntimeException("path " + path + " is not in pathMethodMappingMap ");
            }
        }
        return methodMapping;
    }

}

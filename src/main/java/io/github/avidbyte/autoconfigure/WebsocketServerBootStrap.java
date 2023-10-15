package io.github.avidbyte.autoconfigure;

import io.github.avidbyte.annotation.ServerEndpoint;
import io.github.avidbyte.exception.DeploymentException;
import io.github.avidbyte.standard.MethodMapping;
import io.github.avidbyte.standard.ServerEndpointConfig;
import io.github.avidbyte.standard.WebSocketEventServer;
import io.github.avidbyte.standard.WebsocketServer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author Aaron
 * @since 1.0
 */
public class WebsocketServerBootStrap extends ApplicationObjectSupport implements SmartInitializingSingleton, BeanFactoryAware, ResourceLoaderAware {

    private final WebSocketProperties webSocketProperties;

    public WebsocketServerBootStrap(WebSocketProperties webSocketProperties) {
        this.webSocketProperties = webSocketProperties;
    }

    private AbstractBeanFactory beanFactory;

    private final Map<InetSocketAddress, WebsocketServer> addressWebsocketServerMap = new HashMap<>();


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof AbstractBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a AbstractBeanFactory: " + beanFactory);
        }
        this.beanFactory = (AbstractBeanFactory) beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        registerEndpoints();
    }

    protected void registerEndpoints() {
        ApplicationContext context = getApplicationContext();
        if (context != null) {
            String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
            Set<Class<?>> endpointClasses = new LinkedHashSet<>();
            for (String beanName : endpointBeanNames) {
                endpointClasses.add(context.getType(beanName));
            }
            for (Class<?> endpointClass : endpointClasses) {
                if (ClassUtils.isCglibProxyClass(endpointClass)) {
                    registerEndpoint(endpointClass.getSuperclass());
                } else {
                    registerEndpoint(endpointClass);
                }
            }
        } else {
            logger.info("context == null");
        }
        init();
    }

    private void registerEndpoint(Class<?> endpointClass) {
        ServerEndpoint annotation = AnnotatedElementUtils.findMergedAnnotation(endpointClass, ServerEndpoint.class);
        if (annotation == null) {
            throw new IllegalStateException("missingAnnotation ServerEndpoint");
        }
        String path = annotation.value();
        checkPath(path);
        String processedPath = removeSlash(path);
        NettyProperties nettyProperties = webSocketProperties.getEndpoint().getOrDefault(processedPath, new NettyProperties());

        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(nettyProperties);

        ApplicationContext context = getApplicationContext();
        MethodMapping methodMapping;
        try {
            methodMapping = new MethodMapping(endpointClass, context, beanFactory);
        } catch (DeploymentException e) {
            throw new IllegalStateException("Failed to register ServerEndpointConfig: " + serverEndpointConfig, e);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverEndpointConfig.getHost(), serverEndpointConfig.getPort());

        WebsocketServer websocketServer = addressWebsocketServerMap.get(inetSocketAddress);
        if (websocketServer == null) {
            WebSocketEventServer webSocketEventServer = new WebSocketEventServer(methodMapping, serverEndpointConfig, path);
            websocketServer = new WebsocketServer(webSocketEventServer, serverEndpointConfig);
            addressWebsocketServerMap.put(inetSocketAddress, websocketServer);
        } else {
            websocketServer.getWebSocketEventServer().addPathMethodMapping(path, methodMapping);
        }
    }

    private void init() {
        for (Map.Entry<InetSocketAddress, WebsocketServer> entry : addressWebsocketServerMap.entrySet()) {
            WebsocketServer websocketServer = entry.getValue();
            try {
                websocketServer.init();
                WebSocketEventServer webSocketEventServer = websocketServer.getWebSocketEventServer();
                StringJoiner stringJoiner = new StringJoiner(",");
                webSocketEventServer.getPathMatcherSet().forEach(pathMatcher -> stringJoiner.add("'" + pathMatcher.getPattern() + "'"));
                logger.info(String.format("\033[34mNetty WebSocket started on port: %s with context path(s): %s \033[0m", webSocketEventServer.getPort(), stringJoiner));
            } catch (InterruptedException e) {
                logger.error(String.format("websocket [%s] init fail", entry.getKey()), e);
            } catch (SSLException e) {
                logger.error(String.format("websocket [%s] ssl create fail", entry.getKey()), e);
            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

    }


    private void checkPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path may not be null");
        }
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Path may not be empty");
        }
        char pathPrefix = '/';
        if (path.charAt(0) != pathPrefix) {
            throw new IllegalArgumentException("Path must start with '/'");
        }
    }

    private String removeSlash(String path) {
        return path.replaceAll("/", "");
    }

}

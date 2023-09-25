package org.tianzunh.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Aaron
 * @since 1.0
 */
@ConfigurationProperties(prefix = WebSocketProperties.NETTY_PREFIX)
public class WebSocketProperties {
    public static final String NETTY_PREFIX = "netty.websocket";

    public WebSocketProperties() {
    }

    private Map<String, NettyProperties> endpoint = new LinkedHashMap<>();

    public Map<String, NettyProperties> getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Map<String, NettyProperties> endpoint) {
        this.endpoint = endpoint;
    }

}

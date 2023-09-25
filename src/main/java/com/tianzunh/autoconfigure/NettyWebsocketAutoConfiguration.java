package com.tianzunh.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;

/**
 * @author Aaron
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({WebSocketProperties.class})
public class NettyWebsocketAutoConfiguration extends ApplicationObjectSupport{

    public NettyWebsocketAutoConfiguration(WebSocketProperties webSocketProperties) {
        this.webSocketProperties = webSocketProperties;
    }

    private final WebSocketProperties webSocketProperties;

    @Bean
    @ConditionalOnMissingBean
    public WebsocketServerBootStrap websocketServerBootStrap() {
        return new WebsocketServerBootStrap(webSocketProperties);
    }

}

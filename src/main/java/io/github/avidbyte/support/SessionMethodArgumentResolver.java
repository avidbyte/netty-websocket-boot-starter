package io.github.avidbyte.support;


import io.netty.channel.Channel;
import io.github.avidbyte.standard.Session;
import io.github.avidbyte.standard.WebSocketEventServer;
import org.springframework.core.MethodParameter;


/**
 * @author Aaron
 * @version 1.0
 */
public class SessionMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Session.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        return channel.attr(WebSocketEventServer.SESSION_KEY).get();
    }
}

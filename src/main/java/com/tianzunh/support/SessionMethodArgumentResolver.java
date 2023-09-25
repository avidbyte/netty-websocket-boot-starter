package com.tianzunh.support;


import com.tianzunh.standard.Session;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

import static com.tianzunh.standard.WebSocketEventServer.SESSION_KEY;


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
        return channel.attr(SESSION_KEY).get();
    }
}

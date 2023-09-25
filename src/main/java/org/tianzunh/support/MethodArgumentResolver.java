package org.tianzunh.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;


/**
 * 方法参数解析
 *
 * @author Aaron
 * @since 1.0
 */
public interface MethodArgumentResolver {

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by this resolver.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    boolean supportsParameter(MethodParameter parameter);


    /**
     * resolve argument
     *
     * @param parameter the method parameter to resolve
     * @param channel   channel
     * @param object    object
     * @return Object
     * @throws Exception
     */
    @Nullable
    Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception;

}

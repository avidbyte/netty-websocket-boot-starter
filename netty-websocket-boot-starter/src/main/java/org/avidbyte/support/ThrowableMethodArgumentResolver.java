package org.avidbyte.support;

import io.netty.channel.Channel;
import org.avidbyte.annotation.OnError;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author Aaron
 * @version 1.0
 */
public class ThrowableMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Objects.requireNonNull(parameter.getMethod()).isAnnotationPresent(OnError.class) && Throwable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        if (object instanceof Throwable) {
            return object;
        }
        return null;
    }
}

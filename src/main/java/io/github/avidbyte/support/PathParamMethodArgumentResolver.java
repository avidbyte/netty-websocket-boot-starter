package io.github.avidbyte.support;

import io.github.avidbyte.annotation.PathParam;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.github.avidbyte.standard.WebSocketEventServer;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;

import java.util.List;
import java.util.Map;


/**
 * @author Aaron
 */
public class PathParamMethodArgumentResolver implements MethodArgumentResolver {


    private AbstractBeanFactory beanFactory;

    public PathParamMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        PathParam ann = parameter.getParameterAnnotation(PathParam.class);
        if(ann == null){
            throw new IllegalArgumentException("");
        }

        String value = ann.value();
        if(value.isEmpty()){
            value = parameter.getParameterName();
            if(value == null){
                throw new IllegalArgumentException(
                        "Name for argument type [" + parameter.getNestedParameterType().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }

        if (!channel.hasAttr(WebSocketEventServer.URI_PARAM)) {
            QueryStringDecoder decoder = new QueryStringDecoder(((FullHttpRequest) object).uri());
            channel.attr(WebSocketEventServer.URI_PARAM).set(decoder.parameters());
        }

        Map<String, List<String>> pathParams = channel.attr(WebSocketEventServer.URI_PARAM).get();
        List<String> arg = (pathParams != null ? pathParams.get(value) : null);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        if (arg == null) {
            if (ValueConstants.DEFAULT_NONE.equals(ann.defaultValue())) {
                return null;
            }else {
                return typeConverter.convertIfNecessary(ann.defaultValue(), parameter.getParameterType());
            }
        }
        if (List.class.isAssignableFrom(parameter.getParameterType())) {
            return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
        } else {
            return typeConverter.convertIfNecessary(arg.get(0), parameter.getParameterType());
        }

    }
}

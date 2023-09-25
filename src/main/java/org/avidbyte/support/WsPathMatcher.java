package org.avidbyte.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


/**
 * @author Aaron
 * @version 1.0
 */
public interface WsPathMatcher {

    /**
     * pattern
     * @return String
     */
    String getPattern();

    /**
     * 匹配路径
     * @param decoder decoder
     * @param channel channel
     * @return boolean
     */
    boolean matchAndExtract(QueryStringDecoder decoder, Channel channel);
}

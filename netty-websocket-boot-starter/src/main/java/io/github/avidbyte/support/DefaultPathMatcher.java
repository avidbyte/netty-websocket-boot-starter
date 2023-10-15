package io.github.avidbyte.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Objects;


/**
 * @author Aaron
 * @version 1.0
 */
public class DefaultPathMatcher implements WsPathMatcher {

    private final String pattern;

    public DefaultPathMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        return pattern.equals(decoder.path());
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultPathMatcher that = (DefaultPathMatcher) o;

        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern != null ? pattern.hashCode() : 0;
    }

}

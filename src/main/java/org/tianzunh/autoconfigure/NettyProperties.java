package org.tianzunh.autoconfigure;

import org.tianzunh.support.ValueConstants;

/**
 * @author Aaron
 * @since 1.0
 */
public class NettyProperties {

    private String host = ValueConstants.DEFAULT_ADDRESS;

    private int port = 80;

    private int bossLoopGroupThreads = 1;

    private int workerLoopGroupThreads = 0;

    private boolean useCompressionHandler = false;


    // --------------option------------
    private int optionConnectTimeoutMillis = 30000;

    private int optionSoBacklog = 128;

    //--------------childOption------------
    private int childOptionWriteSpinCount = 16;

    private int childOptionWriteBufferHighWaterMark = 65536;

    private int childOptionWriteBufferLowWaterMark = 32768;

    private int childOptionSoRcvBuf = 1;
    private int childOptionSoSndBuf = 1;

    private boolean childOptionTcpNodelay = true;

    private boolean childOptionSoKeepalive = false;

    private int childOptionSoLinger = -1;

    private boolean childOptionAllowHalfClosure = false;


    //---- idleEvent ----
    private int readerIdleTimeSeconds = 0;

    private int writerIdleTimeSeconds = 0;

    private int allIdleTimeSeconds = 0;

    //----- handshake ----
    private int maxFramePayloadLength = 65536;

    //----- eventExecutorGroup ----
    /**
     * useEventExecutorGroup(another thread pool) to perform time-consuming synchronous business logic
     */
    private boolean useEventExecutorGroup = true;

    private int eventExecutorGroupThreads = 16;

    //------------------------- ssl (refer to spring Ssl) -------------------------

    /**
     * {@link org.springframework.boot.web.server.Ssl}
     */
    private String sslKeyPassword = "";

    private String sslKeyStore = "";

    private String sslKeyStorePassword = "";

    private String sslKeyStoreType = "";

    private String sslTrustStore = "";
    private String sslTrustStorePassword = "";
    private String sslTrustStoreType = "";

    //------------------------- cors (refer to spring CrossOrigin) -------------------------

    /**
     * {@link org.springframework.web.bind.annotation.CrossOrigin}
     */
    private String[] corsOrigins = new String[0];

    private Boolean corsAllowCredentials;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossLoopGroupThreads() {
        return bossLoopGroupThreads;
    }

    public void setBossLoopGroupThreads(int bossLoopGroupThreads) {
        this.bossLoopGroupThreads = bossLoopGroupThreads;
    }

    public int getWorkerLoopGroupThreads() {
        return workerLoopGroupThreads;
    }

    public void setWorkerLoopGroupThreads(int workerLoopGroupThreads) {
        this.workerLoopGroupThreads = workerLoopGroupThreads;
    }

    public boolean isUseCompressionHandler() {
        return useCompressionHandler;
    }

    public void setUseCompressionHandler(boolean useCompressionHandler) {
        this.useCompressionHandler = useCompressionHandler;
    }

    public int getOptionConnectTimeoutMillis() {
        return optionConnectTimeoutMillis;
    }

    public void setOptionConnectTimeoutMillis(int optionConnectTimeoutMillis) {
        this.optionConnectTimeoutMillis = optionConnectTimeoutMillis;
    }

    public int getOptionSoBacklog() {
        return optionSoBacklog;
    }

    public void setOptionSoBacklog(int optionSoBacklog) {
        this.optionSoBacklog = optionSoBacklog;
    }

    public int getChildOptionWriteSpinCount() {
        return childOptionWriteSpinCount;
    }

    public void setChildOptionWriteSpinCount(int childOptionWriteSpinCount) {
        this.childOptionWriteSpinCount = childOptionWriteSpinCount;
    }

    public int getChildOptionWriteBufferHighWaterMark() {
        return childOptionWriteBufferHighWaterMark;
    }

    public void setChildOptionWriteBufferHighWaterMark(int childOptionWriteBufferHighWaterMark) {
        this.childOptionWriteBufferHighWaterMark = childOptionWriteBufferHighWaterMark;
    }

    public int getChildOptionWriteBufferLowWaterMark() {
        return childOptionWriteBufferLowWaterMark;
    }

    public void setChildOptionWriteBufferLowWaterMark(int childOptionWriteBufferLowWaterMark) {
        this.childOptionWriteBufferLowWaterMark = childOptionWriteBufferLowWaterMark;
    }

    public int getChildOptionSoRcvBuf() {
        return childOptionSoRcvBuf;
    }

    public void setChildOptionSoRcvBuf(int childOptionSoRcvBuf) {
        this.childOptionSoRcvBuf = childOptionSoRcvBuf;
    }

    public int getChildOptionSoSndBuf() {
        return childOptionSoSndBuf;
    }

    public void setChildOptionSoSndBuf(int childOptionSoSndBuf) {
        this.childOptionSoSndBuf = childOptionSoSndBuf;
    }

    public boolean isChildOptionTcpNodelay() {
        return childOptionTcpNodelay;
    }

    public void setChildOptionTcpNodelay(boolean childOptionTcpNodelay) {
        this.childOptionTcpNodelay = childOptionTcpNodelay;
    }

    public boolean isChildOptionSoKeepalive() {
        return childOptionSoKeepalive;
    }

    public void setChildOptionSoKeepalive(boolean childOptionSoKeepalive) {
        this.childOptionSoKeepalive = childOptionSoKeepalive;
    }

    public int getChildOptionSoLinger() {
        return childOptionSoLinger;
    }

    public void setChildOptionSoLinger(int childOptionSoLinger) {
        this.childOptionSoLinger = childOptionSoLinger;
    }

    public boolean isChildOptionAllowHalfClosure() {
        return childOptionAllowHalfClosure;
    }

    public void setChildOptionAllowHalfClosure(boolean childOptionAllowHalfClosure) {
        this.childOptionAllowHalfClosure = childOptionAllowHalfClosure;
    }

    public int getReaderIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeSeconds(int readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public int getWriterIdleTimeSeconds() {
        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeSeconds(int writerIdleTimeSeconds) {
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public int getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeSeconds(int allIdleTimeSeconds) {
        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    public int getMaxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    public void setMaxFramePayloadLength(int maxFramePayloadLength) {
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    public boolean isUseEventExecutorGroup() {
        return useEventExecutorGroup;
    }

    public void setUseEventExecutorGroup(boolean useEventExecutorGroup) {
        this.useEventExecutorGroup = useEventExecutorGroup;
    }

    public int getEventExecutorGroupThreads() {
        return eventExecutorGroupThreads;
    }

    public void setEventExecutorGroupThreads(int eventExecutorGroupThreads) {
        this.eventExecutorGroupThreads = eventExecutorGroupThreads;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public void setSslKeyPassword(String sslKeyPassword) {
        this.sslKeyPassword = sslKeyPassword;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public void setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public void setSslKeyStoreType(String sslKeyStoreType) {
        this.sslKeyStoreType = sslKeyStoreType;
    }

    public String getSslTrustStore() {
        return sslTrustStore;
    }

    public void setSslTrustStore(String sslTrustStore) {
        this.sslTrustStore = sslTrustStore;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public void setSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    public String getSslTrustStoreType() {
        return sslTrustStoreType;
    }

    public void setSslTrustStoreType(String sslTrustStoreType) {
        this.sslTrustStoreType = sslTrustStoreType;
    }

    public String[] getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String[] corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public Boolean getCorsAllowCredentials() {
        return corsAllowCredentials;
    }

    public void setCorsAllowCredentials(Boolean corsAllowCredentials) {
        this.corsAllowCredentials = corsAllowCredentials;
    }
}

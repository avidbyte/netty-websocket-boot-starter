package org.avidbyte.standard;

import org.avidbyte.autoconfigure.NettyProperties;
import org.avidbyte.support.ValueConstants;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Aaron
 * @version 1.0
 */
public class ServerEndpointConfig {

    private final String HOST;
    private final int PORT;
    private final int BOSS_LOOP_GROUP_THREADS;
    private final int WORKER_LOOP_GROUP_THREADS;
    private final boolean USE_COMPRESSION_HANDLER;
    private final int CONNECT_TIMEOUT_MILLIS;
    private final int SO_BACKLOG;
    private final int WRITE_SPIN_COUNT;
    private final int WRITE_BUFFER_HIGH_WATER_MARK;
    private final int WRITE_BUFFER_LOW_WATER_MARK;
    private final int SO_RCV_BUF;
    private final int SO_SND_BUF;
    private final boolean TCP_NODELAY;
    private final boolean SO_KEEPALIVE;
    private final int SO_LINGER;
    private final boolean ALLOW_HALF_CLOSURE;
    private final int READER_IDLE_TIME_SECONDS;
    private final int WRITER_IDLE_TIME_SECONDS;
    private final int ALL_IDLE_TIME_SECONDS;
    private final int MAX_FRAME_PAYLOAD_LENGTH;
    private final boolean USE_EVENT_EXECUTOR_GROUP;
    private final int EVENT_EXECUTOR_GROUP_THREADS;

    private final String KEY_PASSWORD;
    private final String KEY_STORE;
    private final String KEY_STORE_PASSWORD;
    private final String KEY_STORE_TYPE;
    private final String TRUST_STORE;
    private final String TRUST_STORE_PASSWORD;
    private final String TRUST_STORE_TYPE;

    private final String[] CORS_ORIGINS;
    private final Boolean CORS_ALLOW_CREDENTIALS;

    private static Integer randomPort;

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ServerEndpointConfig.class);

    public ServerEndpointConfig(NettyProperties nettyProperties){
        String host = nettyProperties.getHost();
        if (StringUtils.isEmpty(host) || ValueConstants.DEFAULT_ADDRESS.equals(host)) {
            this.HOST = ValueConstants.DEFAULT_ADDRESS;
        } else {
            this.HOST = host;
        }
        this.PORT = getAvailablePort(nettyProperties.getPort());
        this.BOSS_LOOP_GROUP_THREADS = nettyProperties.getBossLoopGroupThreads();
        this.WORKER_LOOP_GROUP_THREADS = nettyProperties.getWorkerLoopGroupThreads();
        this.USE_COMPRESSION_HANDLER = nettyProperties.isUseCompressionHandler();
        this.CONNECT_TIMEOUT_MILLIS = nettyProperties.getOptionConnectTimeoutMillis();
        this.SO_BACKLOG = nettyProperties.getOptionSoBacklog();
        this.WRITE_SPIN_COUNT = nettyProperties.getChildOptionWriteSpinCount();
        this.WRITE_BUFFER_HIGH_WATER_MARK = nettyProperties.getChildOptionWriteBufferHighWaterMark();
        this.WRITE_BUFFER_LOW_WATER_MARK = nettyProperties.getChildOptionWriteBufferLowWaterMark();
        this.SO_RCV_BUF = nettyProperties.getChildOptionSoRcvBuf();
        this.SO_SND_BUF = nettyProperties.getChildOptionSoSndBuf();
        this.TCP_NODELAY = nettyProperties.isChildOptionTcpNodelay();
        this.SO_KEEPALIVE = nettyProperties.isChildOptionSoKeepalive();
        this.SO_LINGER = nettyProperties.getChildOptionSoLinger();
        this.ALLOW_HALF_CLOSURE = nettyProperties.isChildOptionAllowHalfClosure();
        this.READER_IDLE_TIME_SECONDS = nettyProperties.getReaderIdleTimeSeconds();
        this.WRITER_IDLE_TIME_SECONDS = nettyProperties.getWriterIdleTimeSeconds();
        this.ALL_IDLE_TIME_SECONDS = nettyProperties.getAllIdleTimeSeconds();
        this.MAX_FRAME_PAYLOAD_LENGTH = nettyProperties.getMaxFramePayloadLength();
        this.USE_EVENT_EXECUTOR_GROUP = nettyProperties.isUseEventExecutorGroup();
        this.EVENT_EXECUTOR_GROUP_THREADS = nettyProperties.getEventExecutorGroupThreads();

        this.KEY_PASSWORD = nettyProperties.getSslKeyPassword();
        this.KEY_STORE = nettyProperties.getSslKeyStore();
        this.KEY_STORE_PASSWORD = nettyProperties.getSslKeyStorePassword();
        this.KEY_STORE_TYPE = nettyProperties.getSslKeyStoreType();
        this.TRUST_STORE = nettyProperties.getSslTrustStore();
        this.TRUST_STORE_PASSWORD = nettyProperties.getSslTrustStorePassword();
        this.TRUST_STORE_TYPE = nettyProperties.getSslTrustStoreType();

        this.CORS_ORIGINS = nettyProperties.getCorsOrigins();
        this.CORS_ALLOW_CREDENTIALS = nettyProperties.getCorsAllowCredentials();
    }


    private int getAvailablePort(int port) {
        if (port != 0) {
            return port;
        }
        if (randomPort != null && randomPort != 0) {
            return randomPort;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
        Socket socket = new Socket();
        try {
            socket.bind(inetSocketAddress);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        int localPort = socket.getLocalPort();
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e);
        }
        randomPort = localPort;
        return localPort;
    }

    public String getHost() {
        return HOST;
    }

    public int getPort() {
        return PORT;
    }

    public int getBossLoopGroupThreads() {
        return BOSS_LOOP_GROUP_THREADS;
    }

    public int getWorkerLoopGroupThreads() {
        return WORKER_LOOP_GROUP_THREADS;
    }

    public boolean isUseCompressionHandler() {
        return USE_COMPRESSION_HANDLER;
    }

    public int getConnectTimeoutMillis() {
        return CONNECT_TIMEOUT_MILLIS;
    }

    public int getSoBacklog() {
        return SO_BACKLOG;
    }

    public int getWriteSpinCount() {
        return WRITE_SPIN_COUNT;
    }

    public int getWriteBufferHighWaterMark() {
        return WRITE_BUFFER_HIGH_WATER_MARK;
    }

    public int getWriteBufferLowWaterMark() {
        return WRITE_BUFFER_LOW_WATER_MARK;
    }

    public int getSoRcvBuf() {
        return SO_RCV_BUF;
    }

    public int getSoSndBuf() {
        return SO_SND_BUF;
    }

    public boolean isTcpNodelay() {
        return TCP_NODELAY;
    }

    public boolean isSoKeepalive() {
        return SO_KEEPALIVE;
    }

    public int getSoLinger() {
        return SO_LINGER;
    }

    public boolean isAllowHalfClosure() {
        return ALLOW_HALF_CLOSURE;
    }

    public static Integer getRandomPort() {
        return randomPort;
    }

    public int getReaderIdleTimeSeconds() {
        return READER_IDLE_TIME_SECONDS;
    }

    public int getWriterIdleTimeSeconds() {
        return WRITER_IDLE_TIME_SECONDS;
    }

    public int getAllIdleTimeSeconds() {
        return ALL_IDLE_TIME_SECONDS;
    }

    public int getMaxFramePayloadLength() {
        return MAX_FRAME_PAYLOAD_LENGTH;
    }

    public boolean isUseEventExecutorGroup() {
        return USE_EVENT_EXECUTOR_GROUP;
    }

    public int getEventExecutorGroupThreads() {
        return EVENT_EXECUTOR_GROUP_THREADS;
    }

    public String getKeyPassword() {
        return KEY_PASSWORD;
    }

    public String getKeyStore() {
        return KEY_STORE;
    }

    public String getKeyStorePassword() {
        return KEY_STORE_PASSWORD;
    }

    public String getKeyStoreType() {
        return KEY_STORE_TYPE;
    }

    public String getTrustStore() {
        return TRUST_STORE;
    }

    public String getTrustStorePassword() {
        return TRUST_STORE_PASSWORD;
    }

    public String getTrustStoreType() {
        return TRUST_STORE_TYPE;
    }

    public String[] getCorsOrigins() {
        return CORS_ORIGINS;
    }

    public Boolean getCorsAllowCredentials() {
        return CORS_ALLOW_CREDENTIALS;
    }
}

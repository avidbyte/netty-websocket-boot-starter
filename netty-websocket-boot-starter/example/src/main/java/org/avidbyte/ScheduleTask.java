package org.avidbyte;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Aaron
 * @since 1.0
 */
@Slf4j
@Component
public class ScheduleTask {

    @Resource
    TextWebSocket webSocket;

    /**
     * 开始job
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void startJob() {
        webSocket.sendMessageAll("test");
    }
}

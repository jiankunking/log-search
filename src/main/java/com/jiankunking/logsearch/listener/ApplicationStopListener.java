package com.jiankunking.logsearch.listener;


import com.jiankunking.logsearch.client.ConsulClientSingleton;
import com.jiankunking.logsearch.client.ESClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Service
public class ApplicationStopListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        try {
            log.info(" start close es client ");
            ESClients.closeESClients();
            log.info(" es client has been closed ");

            log.info(" start close consul client ");
            ConsulClientSingleton.destroyConsulClient();
            log.info(" consul client has been closed ");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
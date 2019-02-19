package com.jiankunking.logsearch.listener;


import com.jiankunking.logsearch.cache.IndexRetentionTimeCache;
import com.jiankunking.logsearch.cache.NameSpacesCache;
import com.jiankunking.logsearch.client.ESClients;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.queue.OffLineTask;
import com.jiankunking.logsearch.queue.ZipTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * @author jiankunking.
 * @date：2018/9/28 16:49
 * @description:
 */
@Slf4j
@Service
public class ApplicationStartListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    OffLineTask offLineTask;
    @Autowired
    ZipTask zipTask;
    @Autowired
    NameSpacesCache nameSpacesCache;
    @Autowired
    IndexRetentionTimeCache indexRetentionTimeCache;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            log.info(" start init es client ");
            ESClients.initAllESClients();
            log.info(" es client has been initialized ");

            if (EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION) {
                log.info(" start offLineTask.downLoadLogToFileStart ");
                offLineTask.downLoadLogToFileStart();
                log.info(" offLineTask.downLoadLogToFileStart has been started ");

                log.info(" start offLineTask.zipLogStart ");
                zipTask.zipLogStart();
                log.info(" offLineTask.zipLogStart has been started ");
            }

            log.info(" start init namespaces ");
            nameSpacesCache.init();
            log.info(" namespaces has been initialized ");

            log.info(" start init IndexRetentionTimeCache ");
            indexRetentionTimeCache.init();
            log.info(" IndexRetentionTimeCache has been initialized ");

        } catch (UnsupportedEncodingException e) {
            log.error("initAllESClients error:", e);
        }

    }
}

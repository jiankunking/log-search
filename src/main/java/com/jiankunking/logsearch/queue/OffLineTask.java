package com.jiankunking.logsearch.queue;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.model.offline.OffLineLogMetaData;
import com.jiankunking.logsearch.services.OffLineLogDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author jiankunking.
 * @date：2018/9/29 15:18
 * @description:
 */
@Slf4j
@Component
public class OffLineTask implements IQueue<OffLineLogMetaData> {

    private static BlockingQueue<OffLineLogMetaData> taskQueue = new LinkedBlockingQueue<>(10000);
    private static volatile boolean initialized = false;

    @Autowired
    OffLineLogDownloadService offLineLogDownloadService;
    @Autowired
    PoolSizeService poolSizeService;

    public static int getOffLineTaskSize() {
        return taskQueue.size();
    }

    /**
     * 该方法内部创建了线程池 只允许调用一次
     *
     * @throws InterruptedException
     */
    public void downLoadLogToFileStart() {
        if (!EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION) {
            return;
        }
        if (initialized) {
            log.info("OffLineTask Repeated initialization");
            return;
        }
        initialized = true;
        new Thread(() -> {
            int poolSize = poolSizeService.getOffLineTaskPoolSize();
            log.info("OffLineTask poolSize:" + Integer.toString(poolSize));
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("OffLineTask-pool").build();

            //Common Thread Pool
            ExecutorService pool = new ThreadPoolExecutor(poolSize, poolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(100), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
            OffLineLogMetaData offLineLogMetaData = null;
            while (true) {
                try {
                    offLineLogMetaData = taskQueue.take();
                } catch (Exception e) {
                    log.error("downLoadLogToFileStart taskQueue.take() ", e);
                }
                final OffLineLogMetaData tempOffLineLogMetaData = offLineLogMetaData;
                pool.execute(() -> offLineLogDownloadService.downloadByKeyWord(tempOffLineLogMetaData));
            }
        }).start();
    }

    /**
     * 添加下载任务到 阻塞队列
     *
     * @param item
     */
    @Override
    public void add(OffLineLogMetaData item) {
        if (item == null) {
            return;
        }
        taskQueue.add(item);
        log.info("OffLineTask taskQueue size:" + taskQueue.size());
    }
}

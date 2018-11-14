package com.jiankunking.logsearch.queue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.services.OffLineLogDownloadService;
import com.jiankunking.logsearch.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author jiankunking.
 * @date：2018/10/15 15:55
 * @description:
 */
@Slf4j
@Component
public class ZipTask implements IQueue<String> {
    private static BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>(10000);
    private static volatile boolean initialized = false;

    @Autowired
    OffLineLogDownloadService offLineLogDownloadService;
    @Autowired
    PoolSizeService poolSizeService;

    public static int getZipTaskSize() {
        return taskQueue.size();
    }

    /**
     * 压缩日志
     * 日志压缩改为线程池压缩，防止多个日志文件同时下载完成，同时压缩，占用cpu过多的问题
     */
    public void zipLogStart() {
        if (!EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION) {
            return;
        }
        if (initialized) {
            log.info("ZipTask Repeated initialization");
            return;
        }
        initialized = true;
        new Thread(() -> {
            int poolSize = poolSizeService.getZipTaskPoolSize();
            log.info("ZipTask poolSize:" + Integer.toString(poolSize));

            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("ZipTask-pool").build();

            //Common Thread Pool
            ExecutorService pool = new ThreadPoolExecutor(poolSize, poolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(100), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
            String fileFullPath = null;
            while (true) {
                try {
                    fileFullPath = taskQueue.take();
                } catch (InterruptedException e) {
                    log.error("zipLogStart taskQueue.take() ", e);
                }
                final String tempFileFullPath = fileFullPath;
                pool.execute(() -> offLineLogDownloadService.zipLog(tempFileFullPath));
            }
        }).start();
    }

    /**
     * 添加下载任务到 阻塞队列
     *
     * @param item
     */
    @Override
    public void add(String item) {
        if (StringUtils.isEmpty(item)) {
            return;
        }
        taskQueue.add(item);
        log.info("ZipTask taskQueue size:" + taskQueue.size());
    }
}

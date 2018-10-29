package com.jiankunking.logsearch.cron;

import com.jiankunking.logsearch.config.EnvionmentVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/9/29 9:22
 * @description:
 */
@Slf4j
@Component
public class OffLineLogsClearJob {

    /**
     * 每天凌晨2点 启动
     * 测试每五秒执行一次：0/5 * * * * ?
     */
    //@Scheduled(cron = "0 0 2 1 * ?")
    @Scheduled(cron = "0 0 * * * ?")
    public void clearOldFiles() throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("clearOldFiles:" + df.format(new Date()));
        //毫秒
        long diff = 7 * 24 * 60 * 60 * 1000;
        List<String> dirs = this.getDirectories(EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH);
        String[] names;
        for (String dir : dirs) {
            names = dir.split("/");
            //兼容window路径
            if (names.length == 1) {
                names = dir.split("\\\\");
            }
            //大于7天 删除文件目录
            if (System.currentTimeMillis() - Long.valueOf(names[names.length - 1]) >= diff) {
                log.info(dir);
                FileUtils.deleteDirectory(new File(dir));
            }
        }
        System.out.println("clearOldFiles finish");
    }

    public List<String> getDirectories(String path) {
        List<String> result = new ArrayList<>();
        File dir = new File(path);
        //项目
        File[] projects = dir.listFiles();
        if (projects == null || projects.length == 0) {
            return result;
        }
        File[] temp = null;
        for (File project : projects) {
            //时间
            temp = project.listFiles();
            if (temp == null || temp.length == 0) {
                result.add(project.getAbsolutePath());
                continue;
            }
            for (File time : temp) {
                if (time.isDirectory()) {
                    result.add(time.getAbsolutePath());
                }
            }
        }
        return result;
    }

}

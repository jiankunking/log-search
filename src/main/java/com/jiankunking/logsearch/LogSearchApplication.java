package com.jiankunking.logsearch;

import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.model.LocationEnum;
import com.jiankunking.logsearch.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/8/1 13:41
 * @description:
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class LogSearchApplication {

    public static void main(String[] args) {

        String consulHost = "CONSUL_HOST";
        String consulPort = "CONSUL_PORT";
        String ansibleExtUrl = "ANSIBLE_EXT_URL";
        String filebeatDownloadURL = "FILEBEAT_DOWNLOAD_URL";
        String onLineDownloadSize = "ON_LINE_DOWNLOAD_SIZE";
        String cmdbURL = "CMDB_URL";
        String fileBeatVersion = "FILEBEAT_VERSION";
        String offLineLogDownloadPath = "OFF_LINE_LOG_DOWNLOAD_PATH";
        String enableOfflineDownloadFunction = "ENABLE_OFFLINE_DOWNLOAD_FUNCTION";
        String offlineDownloadServer = "OFFLINE_DOWNLOAD_SERVER";
        String offlineDownloadServerPort = "OFFLINE_DOWNLOAD_SERVER_PORT";
        String esExtDeployLocation = "ES_EXT_DEPLOY_LOCATION";
        String downloadPageSize = "DOWNLOAD_PAGE_SIZE";

        Map<String, String> envs = System.getenv();

        //consul 地址
        if (StringUtils.isEmpty(envs.get(consulHost))) {
            log.info("CONSUL_HOST must have value!");
            return;
        } else {
            EnvionmentVariables.CONSUL_HOST = envs.get(consulHost);
        }
        log.info("CONSUL_HOST:" + EnvionmentVariables.CONSUL_HOST);

        //consul 端口
        if (StringUtils.isEmpty(envs.get(consulPort))) {
            EnvionmentVariables.CONSUL_PORT = 8500;
        } else {
            EnvionmentVariables.CONSUL_PORT = Integer.valueOf(envs.get(consulPort)).intValue();
        }
        log.info("CONSUL_PORT:" + EnvionmentVariables.CONSUL_PORT);

        //ansible 拓展地址
        if (StringUtils.isEmpty(envs.get(ansibleExtUrl))) {
            log.info("ANSIBLE_EXT_URL must have value!");
            return;
        }
        EnvionmentVariables.ANSIBLE_EXT_URL = envs.get(ansibleExtUrl);
        log.info("ANSIBLE_EXT_URL:" + EnvionmentVariables.ANSIBLE_EXT_URL);

        //filebeat 下载地址
        if (StringUtils.isEmpty(envs.get(filebeatDownloadURL))) {
            log.info("FILEBEAT_DOWNLOAD_URL must have value!");
            return;
        }
        EnvionmentVariables.FILEBEAT_DOWNLOAD_URL = envs.get(filebeatDownloadURL);
        log.info("FILEBEAT_DOWNLOAD_URL:" + EnvionmentVariables.FILEBEAT_DOWNLOAD_URL);

        //filebeat version
        if (StringUtils.isEmpty(envs.get(fileBeatVersion))) {
            log.info("FILEBEAT_VERSION must have value!");
            return;
        }
        EnvionmentVariables.FILEBEAT_VERSION = envs.get(fileBeatVersion);
        log.info("FILEBEAT_VERSION:" + EnvionmentVariables.FILEBEAT_VERSION);


        //cmdb 地址
        if (StringUtils.isEmpty(envs.get(cmdbURL))) {
            log.info("CMDB_URL must have value!");
            return;
        } else {
            EnvionmentVariables.CMDB_URL = envs.get(cmdbURL);
        }
        log.info("CMDB_URL:" + EnvionmentVariables.CMDB_URL);

        //日志下载最大量限制
        if (!StringUtils.isEmpty(envs.get(onLineDownloadSize))) {
            EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE = Integer.valueOf(envs.get(onLineDownloadSize));
        }
        log.info("ON_LINE_DOWNLOAD_SIZE:" + EnvionmentVariables.ON_LINE_DOWNLOAD_SIZE);

        //es-ext 部署机房
        if (!StringUtils.isEmpty(envs.get(esExtDeployLocation))) {
            EnvionmentVariables.ES_EXT_DEPLOY_LOCATION = LocationEnum.valueOf(envs.get(esExtDeployLocation));
        }
        log.info("ES_EXT_DEPLOY_LOCATION:" + EnvionmentVariables.ES_EXT_DEPLOY_LOCATION);


        //离线下载日志默认路径
        if (!StringUtils.isEmpty(envs.get(offLineLogDownloadPath))) {
            EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH = envs.get(offLineLogDownloadPath);
        }
        log.info("OFF_LINE_LOG_DOWNLOAD_PATH:" + EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH);

        //离线下载日志功能是否启用
        if (!StringUtils.isEmpty(envs.get(enableOfflineDownloadFunction))) {
            EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION = Boolean.parseBoolean(envs.get(enableOfflineDownloadFunction));
        }
        log.info("ENABLE_OFFLINE_DOWNLOAD_FUNCTION:" + EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION);

        if (EnvionmentVariables.ENABLE_OFFLINE_DOWNLOAD_FUNCTION) {
            if (StringUtils.isEmpty(envs.get(offlineDownloadServer))) {
                log.info("Enabling offline download must configure the server address!");
                return;
            } else {
                EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER = envs.get(offlineDownloadServer);
                log.info("OFFLINE_DOWNLOAD_SERVER:" + EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER);
            }
            //  端口
            if (StringUtils.isEmpty(envs.get(offlineDownloadServerPort))) {
                EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER_PORT = 80;
            } else {
                EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER_PORT = Integer.valueOf(envs.get(offlineDownloadServerPort)).intValue();
            }
            log.info("OFFLINE_DOWNLOAD_SERVER_PORT:" + EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER_PORT);
        }

        //日志下载 每次获取条数
        if (!StringUtils.isEmpty(envs.get(downloadPageSize))) {
            EnvionmentVariables.DOWNLOAD_PAGE_SIZE = Integer.valueOf(envs.get(downloadPageSize));
        }
        log.info("DOWNLOAD_PAGE_SIZE:" + EnvionmentVariables.DOWNLOAD_PAGE_SIZE);

        SpringApplication.run(LogSearchApplication.class, args);
    }
}

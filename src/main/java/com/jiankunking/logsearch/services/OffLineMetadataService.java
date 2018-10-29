package com.jiankunking.logsearch.services;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.model.offline.DownLoadStatusEnum;
import com.jiankunking.logsearch.model.offline.OffLineLogMetaData;
import com.jiankunking.logsearch.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @author jiankunking.
 * @date：2018/10/23 13:41
 * @description:
 */
@Slf4j
@Service
public class OffLineMetadataService {

    /**
     * 更新任务状态
     *
     * @param fileFullPath
     * @param downLoadStatusEnum
     * @return
     * @throws IOException
     */
    public boolean updateTaskState(String fileFullPath, DownLoadStatusEnum downLoadStatusEnum) {
        File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
        OffLineLogMetaData offLineLogMetaData = null;
        try {
            offLineLogMetaData = JSON.parseObject(FileUtils.readFileToString(file, GlobalConfig.CHARSET), OffLineLogMetaData.class);
            offLineLogMetaData.setDownLoadStatus(downLoadStatusEnum);
            FileUtils.writeStringToFile(file, JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
        } catch (IOException e) {
            log.error("updateTaskState error:", e);
            return false;
        }
        return true;
    }

    /**
     * 更新任务状态及待下载总条数
     *
     * @param fileFullPath
     * @param downLoadStatusEnum
     * @param total
     * @return
     */
    public boolean updateTaskStateAndTotal(String fileFullPath, DownLoadStatusEnum downLoadStatusEnum, int total) {
        File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
        OffLineLogMetaData offLineLogMetaData = null;
        try {
            offLineLogMetaData = JSON.parseObject(FileUtils.readFileToString(file, GlobalConfig.CHARSET), OffLineLogMetaData.class);
            offLineLogMetaData.setDownLoadStatus(downLoadStatusEnum);
            offLineLogMetaData.setTotal(total);
            FileUtils.writeStringToFile(file, JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
        } catch (IOException e) {
            log.error("updateTaskStateAndTotal error:", e);
            return false;
        }
        return true;
    }

    /**
     * 更新任务任务下载总条数及耗时
     *
     * @param fileFullPath
     * @param total
     * @param cost
     * @return
     */
    public boolean updateTotalCost(String fileFullPath, int total, long cost) {
        File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
        OffLineLogMetaData offLineLogMetaData = null;
        try {
            offLineLogMetaData = JSON.parseObject(FileUtils.readFileToString(file, GlobalConfig.CHARSET), OffLineLogMetaData.class);
            offLineLogMetaData.setTotal(total);
            offLineLogMetaData.setDownLoadCost(cost);
            FileUtils.writeStringToFile(file, JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
        } catch (IOException e) {
            log.error("updateTotalCost error:", e);
            return false;
        }
        return true;
    }

    /**
     * 更新待下载日志文件大小
     *
     * @param fileFullPath
     * @param size
     * @return
     */
    public boolean updateZipFileSize(String fileFullPath, long size) {
        File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
        OffLineLogMetaData offLineLogMetaData = null;
        try {
            offLineLogMetaData = JSON.parseObject(FileUtils.readFileToString(file, GlobalConfig.CHARSET), OffLineLogMetaData.class);
            offLineLogMetaData.setZipFileSize(size);
            FileUtils.writeStringToFile(file, JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
        } catch (IOException e) {
            log.error("updateZipfileSize error:", e);
            return false;
        }
        return true;
    }
}

package com.jiankunking.logsearch.services;

import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.SearchResult;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.model.SearchIDEntity;
import com.jiankunking.logsearch.model.offline.DownLoadStatusEnum;
import com.jiankunking.logsearch.model.offline.LogDownLoadTypeEnum;
import com.jiankunking.logsearch.model.offline.OffLineLogMetaData;
import com.jiankunking.logsearch.model.offline.QueryCondition;
import com.jiankunking.logsearch.queue.ZipTask;
import com.jiankunking.logsearch.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.jiankunking.logsearch.config.EnvionmentVariables.DOWNLOAD_PAGE_SIZE;

/**
 * @author jiankunking.
 * @date：2018/9/28 18:22
 * @description:
 */
@Slf4j
@Service
public class OffLineLogDownloadService {

    @Autowired
    LogSearchService logSearchService;
    @Autowired
    LogDownloadService logDownloadService;
    @Autowired
    OffLineMetadataService offLineMetadataService;
    @Autowired
    ZipTask zipTask;

    public void downloadByKeyWord(OffLineLogMetaData offLineLogMetaData) {
        QueryCondition queryCondition = offLineLogMetaData.getQueryCondition();
        String fileFullPath = null;
        try {
            String fileName = this.getLogFileName(queryCondition);
            fileFullPath = this.getFullPathName(fileName, queryCondition.getProject(), offLineLogMetaData.getCreatTime());
            log.info("fileFullPath : " + fileFullPath);
            this.downloadByKeyWord(queryCondition.getCluster(), queryCondition.getProject(), queryCondition.getKeyword(), queryCondition.getApp(),
                    queryCondition.getInstance(), queryCondition.getHostID(), queryCondition.getSource(), queryCondition.getFromTime(), queryCondition.getToTime(), fileFullPath);
            offLineMetadataService.updateTaskState(fileFullPath, DownLoadStatusEnum.ziping);
            zipTask.add(fileFullPath);
        } catch (NoSuchAlgorithmException | ESClientNotFoundException | IOException | ESClusterNotFoundException e) {
            offLineMetadataService.updateTaskState(fileFullPath, DownLoadStatusEnum.fail);
            log.error("downloadByKeyWord", e);
        }
    }

    public void zipLog(String fileFullPath) {
        File srcFile = new File(fileFullPath + GlobalConfig.LOG_FILE_SUFFIX);
        //压缩
        try {
            String zipFile = fileFullPath + GlobalConfig.ZIP_FILE_SUFFIX;
            ZipUtils.zipFile(zipFile, srcFile);
            long size = FileUtils.sizeOf(new File(zipFile));
            offLineMetadataService.updateZipFileSize(fileFullPath, size);
        } catch (IOException e) {
            log.error("downloadByKeyWord zipFile error :", e);
            offLineMetadataService.updateTaskState(fileFullPath, DownLoadStatusEnum.zipFail);
        }
        offLineMetadataService.updateTaskState(fileFullPath, DownLoadStatusEnum.success);
    }

    private void downloadByKeyWord(String cluster, String project, String keyWord, String app, String instance,
                                   String hostID, String source,
                                   long fromTime, long toTime, String downloadFilePath) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        String downloadFileName = downloadFilePath + GlobalConfig.LOG_FILE_SUFFIX;
        long start = System.currentTimeMillis();
        int total = logDownloadService.getTotal(cluster, project, keyWord, app, instance, hostID, source, fromTime, toTime);
        log.info("total:" + total);
        if (total == 0) {
            FileUtils.writeStringToFile(new File(downloadFileName), "There is nothing to download ", GlobalConfig.CHARSET);
            return;
        }
        //记录总条数及下载状态
        offLineMetadataService.updateTaskStateAndTotal(downloadFilePath, DownLoadStatusEnum.downloading, total);
        int cycleIndex = IntUtils.getCycleCount(total, DOWNLOAD_PAGE_SIZE);
        log.info("cycleIndex: " + cycleIndex);
        Object[] searchAfterValues;
        SearchIDEntity searchIDEntity;
        int initialCapacity = MapUtils.getSize(DOWNLOAD_PAGE_SIZE);
        Map<String, Object> item = new HashMap<>(initialCapacity);
        for (int i = 0; i < cycleIndex; i++) {
            if (i == 0) {
                item = this.downLoadByPage(cluster, project, keyWord, app, instance, hostID, source, DOWNLOAD_PAGE_SIZE, fromTime, toTime, null, downloadFileName);
                continue;
            }
            searchIDEntity = (SearchIDEntity) item.get("searchAfter");
            searchAfterValues = new Object[]{searchIDEntity.getTimeStampSort(), searchIDEntity.getOffset(), searchIDEntity.getDocID()};
            item = this.downLoadByPage(cluster, project, keyWord, app, instance, hostID, source, DOWNLOAD_PAGE_SIZE, fromTime, toTime, searchAfterValues, downloadFileName);
        }
        long end = System.currentTimeMillis();
        offLineMetadataService.updateTotalCost(downloadFilePath, total, end - start);
        return;
    }

    /**
     * 根据搜索结果 按页下载
     *
     * @param cluster
     * @param project
     * @param keyWord
     * @param app
     * @param instance
     * @param hostID
     * @param source
     * @param pageSize
     * @param fromTime
     * @param toTime
     * @param searchAfterValues
     * @param downloadFileName
     * @return
     * @throws IOException
     * @throws ESClusterNotFoundException
     * @throws ESClientNotFoundException
     */
    private Map<String, Object> downLoadByPage(String cluster, String project, String keyWord, String app, String instance,
                                               String hostID, String source,
                                               int pageSize, long fromTime, long toTime,
                                               Object[] searchAfterValues, String downloadFileName) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        //识别是否是首次搜索
        boolean firstSearch = false;
        if (searchAfterValues == null) {
            firstSearch = true;
        } else {
            //根据上次结果搜索 结果中会带有上次的最后一条数据
            //所以加一 保持真正的pageSize
            pageSize = pageSize + 1;
        }
        SearchResult searchResult = logSearchService.queryStringByKeyWord(cluster, project, keyWord, app, instance, hostID, source, pageSize, fromTime, toTime, searchAfterValues, null);
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Object> item;
        int initialCapacity = MapUtils.getSize(searchResult.getItems().size());
        Map<String, Object> result = new HashMap<>(initialCapacity);
        SearchIDEntity searchIDEntity = new SearchIDEntity();
        for (int i = 0; i < searchResult.getItems().size(); i++) {
            //迭代的时候 会有一条上次结果中的数据
            //所以 移除掉
            if (!firstSearch && i == 0) {
                continue;
            }
            item = searchResult.getItems().get(i);
            if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
                stringBuilder.append(item.get("app"));
                stringBuilder.append(GlobalConfig.SPACE);
            }
            if (StringUtils.isEmpty(instance) || instance.equals(GlobalConfig.ALL_INSTANCES)) {
                stringBuilder.append(item.get("instance"));
                stringBuilder.append(GlobalConfig.SPACE);
            }
            stringBuilder.append(item.get("message"));
            stringBuilder.append(GlobalConfig.LINE_BREAK);
            if (i + 1 == searchResult.getItems().size()) {
                searchIDEntity.setDocID(String.valueOf(item.get("id")));
                searchIDEntity.setOffset(Integer.valueOf(String.valueOf(item.get("offset"))));
                searchIDEntity.setTimeStampSort(ObjectUtils.objectToLong(item.get("time")));
            }
        }
        result.put("searchAfter", searchIDEntity);
        FileUtils.writeStringToFile(new File(downloadFileName), stringBuilder.toString(), GlobalConfig.CHARSET, true);
        return result;
    }

    /**
     * 获取文件名称
     *
     * @param queryCondition
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String getLogFileName(QueryCondition queryCondition) throws NoSuchAlgorithmException {
        String fileName = queryCondition.getCluster() + "_" + queryCondition.getProject() + "_" + queryCondition.getApp() + "_" + queryCondition.getInstance();
        String md5;
        if (queryCondition.getLogDownLoadType() == LogDownLoadTypeEnum.keyword) {
            md5 = MD5Utils.getMD5String(String.valueOf(queryCondition.getFromTime()), String.valueOf(queryCondition.getToTime()), queryCondition.getKeyword());
        } else {
            md5 = MD5Utils.getMD5String(String.valueOf(queryCondition.getFromTime()), String.valueOf(queryCondition.getToTime()), queryCondition.getSource());
        }
        return fileName + "_" + md5;
    }

    /**
     * 获取完整路径的文件名
     *
     * @param fileName
     * @param project
     * @param creatTime
     * @return
     */
    public String getFullPathName(String fileName, String project, long creatTime) {
        return EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH + project + "/" + creatTime + "/" + fileName;
    }
}

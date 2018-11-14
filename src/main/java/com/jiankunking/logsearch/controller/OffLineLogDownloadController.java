package com.jiankunking.logsearch.controller;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.model.offline.DownLoadStatusEnum;
import com.jiankunking.logsearch.model.offline.LogDownLoadTypeEnum;
import com.jiankunking.logsearch.model.offline.OffLineLogMetaData;
import com.jiankunking.logsearch.model.offline.QueryCondition;
import com.jiankunking.logsearch.queue.OffLineTask;
import com.jiankunking.logsearch.queue.ZipTask;
import com.jiankunking.logsearch.services.LogSearchService;
import com.jiankunking.logsearch.services.OffLineLogDownloadService;
import com.jiankunking.logsearch.util.JsonUtils;
import com.jiankunking.logsearch.util.MapUtils;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author jiankunking.
 * @date：2018/9/28 18:15
 * @description:
 */
@Slf4j
@Controller
public class OffLineLogDownloadController implements IApiVersion {

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    OffLineLogDownloadService offLineLogDownloadService;

    @Autowired
    OffLineTask offLineTask;


    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/keyword/offline_download")
    public void downloadByKeyWord(@RequestParam(name = "fromTime", required = true) long fromTime,
                                  @RequestParam(name = "toTime", required = true) long toTime,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @PathVariable(name = "app", required = true) String app,
                                  @PathVariable(name = "instance", required = true) String instance,
                                  @PathVariable(value = "cluster", required = true) String cluster,
                                  @PathVariable(value = "project", required = true) String project,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws NoSuchAlgorithmException, IOException {
        if (fromTime >= toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
            throw new IllegalArgumentException(" The log download must specify the application name ");
        }
        String userName = request.getHeader(GlobalConfig.HEADER_UNAME);
        if (StringUtils.isEmpty(userName)) {
            throw new IllegalArgumentException(" X-UName are not allowed to be empty ");
        }
        long createTime = System.currentTimeMillis();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setCluster(cluster);
        queryCondition.setProject(project);
        queryCondition.setApp(app);
        queryCondition.setInstance(instance);
        queryCondition.setKeyword(keyword);
        queryCondition.setFromTime(fromTime);
        queryCondition.setToTime(toTime);
        queryCondition.setLogDownLoadType(LogDownLoadTypeEnum.keyword);

        String fileName = offLineLogDownloadService.getLogFileName(queryCondition);

        OffLineLogMetaData offLineLogMetaData = new OffLineLogMetaData();
        offLineLogMetaData.setQueryCondition(queryCondition);
        offLineLogMetaData.setCreator(userName);
        offLineLogMetaData.setCreatTime(createTime);
        offLineLogMetaData.setDownLoadStatus(DownLoadStatusEnum.downloading);

        String fileFullPath = offLineLogDownloadService.getFullPathName(fileName, project, createTime);
        //校验文件是否已经存在
        File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
        if (file.exists()) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
            log.info("file has exist:" + fileFullPath);
            return;
        }
        //创建文件
        FileUtils.writeStringToFile(new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX), JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
        offLineTask.add(offLineLogMetaData);
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/source/offline_download")
    public void downloadBySource(@RequestParam(name = "fromTime", required = true) long fromTime,
                                 @RequestParam(name = "toTime", required = true) long toTime,
                                 @PathVariable(value = "cluster", required = true) String cluster,
                                 @PathVariable(value = "project", required = true) String project,
                                 @PathVariable(name = "app", required = true) String app,
                                 @PathVariable(name = "instance", required = true) String instance,
                                 //@RequestParam(name = "startID", required = true) String startID,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        if (fromTime >= toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
            throw new IllegalArgumentException(" The log download must specify the application name ");
        }
        if (StringUtils.isEmpty(instance) || app.equals(GlobalConfig.ALL_INSTANCES)) {
            throw new IllegalArgumentException(" The log download must specify the instance");
        }
        long createTime = System.currentTimeMillis();
        String userName = request.getHeader(GlobalConfig.HEADER_UNAME);
        if (StringUtils.isEmpty(userName)) {
            throw new IllegalArgumentException(" X-UName are not allowed to be empty ");
        }
        if (StringUtils.isNotEmpty(app) && StringUtils.isNotEmpty(instance)) {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setCluster(cluster);
            queryCondition.setProject(project);
            queryCondition.setApp(app);
            queryCondition.setInstance(instance);
            queryCondition.setFromTime(fromTime);
            queryCondition.setToTime(toTime);
            queryCondition.setLogDownLoadType(LogDownLoadTypeEnum.instance);

            String fileName = offLineLogDownloadService.getLogFileName(queryCondition);

            OffLineLogMetaData offLineLogMetaData = new OffLineLogMetaData();
            offLineLogMetaData.setQueryCondition(queryCondition);
            offLineLogMetaData.setCreator(userName);
            offLineLogMetaData.setCreatTime(createTime);
            offLineLogMetaData.setDownLoadStatus(DownLoadStatusEnum.downloading);

            String fileFullPath = offLineLogDownloadService.getFullPathName(fileName, project, createTime);
            //校验文件是否已经存在
            File file = new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX);
            if (file.exists()) {
                ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
                log.info("file has exist:" + fileFullPath);
                return;
            }
            //创建文件
            FileUtils.writeStringToFile(new File(fileFullPath + GlobalConfig.JSON_FILE_SUFFIX), JsonUtils.toJSON(offLineLogMetaData), GlobalConfig.CHARSET);
            offLineTask.add(offLineLogMetaData);
        } else {
            throw new IllegalArgumentException(" Instance is not allowed to be empty at the same time ");
        }

        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/offline_task")
    public void getDownLoadStatus(@PathVariable(value = "cluster", required = true) String cluster,
                                  @PathVariable(value = "project", required = true) String project,
                                  HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        List<Map> result = new ArrayList<>();
        //判断 该项目是否下载过离线任务
        File projectDir = new File(EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH + project);
        if (!projectDir.exists()) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
            return;
        }
        //获取所有文件名称
        String[] extensions = {"json"};
        Collection<File> files = FileUtils.listFiles(new File(EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH + project), extensions, true);
        List<OffLineLogMetaData> data = new ArrayList<>();
        for (File file : files) {
            data.add(JSON.parseObject(FileUtils.readFileToString(file, GlobalConfig.CHARSET), OffLineLogMetaData.class));
        }
        for (OffLineLogMetaData offLineLogMetaData : data) {
            HashMap map = new HashMap(MapUtils.getSize(8));
            map.put("creator", offLineLogMetaData.getCreator());
            map.put("creatTime", offLineLogMetaData.getCreatTime());
            map.put("downLoadStatus", offLineLogMetaData.getDownLoadStatus());
            map.put("queryCondition", offLineLogMetaData.getQueryCondition());
            map.put("total", offLineLogMetaData.getTotal());
            map.put("downLoadCost", offLineLogMetaData.getDownLoadCost());
            map.put("error", offLineLogMetaData.getError());
            map.put("url", "");
            map.put("zipFileSize", offLineLogMetaData.getZipFileSize());
            if (offLineLogMetaData.getDownLoadStatus() == DownLoadStatusEnum.success) {
                String fileName = offLineLogDownloadService.getLogFileName(offLineLogMetaData.getQueryCondition());
                String url = "http://" + EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER + ":" + EnvionmentVariables.OFFLINE_DOWNLOAD_SERVER_PORT + "/" +
                        offLineLogMetaData.getQueryCondition().getProject() + "/" + offLineLogMetaData.getCreatTime() + "/" + fileName + GlobalConfig.ZIP_FILE_SUFFIX;
                map.put("url", url);
            }
            result.add(map);
        }
        //排序 创建时间越新 越在上面
        Collections.sort(result, (o1, o2) -> {
            long map1value = Long.parseLong(o1.get("creatTime").toString());
            long map2value = Long.parseLong(o2.get("creatTime").toString());
            return (int) (map2value - map1value);
        });
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    }

    /**
     * 获取待处理的离线任务数，以便排查问题
     *
     * @param response
     */
    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/uni/offline/download_task_count")
    public void getOfflineDownloadTaskCount(HttpServletResponse response) {
        HashMap map = new HashMap(MapUtils.getSize(1));
        map.put("total", OffLineTask.getOffLineTaskSize());
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, map);
    }

    /**
     * 获取待处理的压缩任务数，以便排查问题
     *
     * @param response
     */
    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/uni/offline/zip_task_count")
    public void getOfflineZipTaskCount(HttpServletResponse response) {
        HashMap map = new HashMap(MapUtils.getSize(1));
        map.put("total", ZipTask.getZipTaskSize());
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, map);
    }

}

package com.jiankunking.logsearch.controller;


import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.services.LogDownloadService;
import com.jiankunking.logsearch.services.LogSearchService;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:下载符合搜索条件的日志
 */
@Slf4j
@Controller
public class LogDownloadController implements IApiVersion {

    @Autowired
    LogDownloadService logDownloadService;
    @Autowired
    LogSearchService logSearchService;


    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/keyword/download")
    public void downloadByKeyWord(@RequestParam(name = "fromTime", required = true) long fromTime,
                                  @RequestParam(name = "toTime", required = true) long toTime,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @PathVariable(value = "cluster", required = true) String cluster,
                                  @PathVariable(value = "project", required = true) String project,
                                  @PathVariable(name = "app", required = true) String app,
                                  @PathVariable(name = "instance", required = true) String instance,
                                  HttpServletResponse response) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        if (fromTime >= toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
            throw new IllegalArgumentException(" The log download must specify the application name ");
        }
        logDownloadService.downloadByKeyWord(cluster, project, keyword, app, instance, null, null, fromTime, toTime, response);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/source/download")
    public void downloadBySource(@RequestParam(name = "fromTime", required = true) long fromTime,
                                 @RequestParam(name = "toTime", required = true) long toTime,
                                 //@RequestParam(name = "startID", required = true) String startID,
                                 @PathVariable(value = "cluster", required = true) String cluster,
                                 @PathVariable(value = "project", required = true) String project,
                                 @PathVariable(name = "app", required = true) String app,
                                 @PathVariable(name = "instance", required = true) String instance,
                                 HttpServletResponse response) throws IOException, ESClusterNotFoundException, ESClientNotFoundException {
        if (fromTime >= toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime ");
        }
        if (StringUtils.isEmpty(app) || app.equals(GlobalConfig.ALL_APPS)) {
            throw new IllegalArgumentException(" The log download must specify the application name ");
        }
        if (StringUtils.isEmpty(instance) || app.equals(GlobalConfig.ALL_INSTANCES)) {
            throw new IllegalArgumentException(" The log download must specify the instance");
        }
        logDownloadService.downloadByKeyWord(cluster, project, null, app, instance, null, null, fromTime, toTime, response);
    }

}

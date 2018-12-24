package com.jiankunking.logsearch.controller;


import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.exception.ESClustersResponseTimeoutException;
import com.jiankunking.logsearch.services.LogBusinessService;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Controller
public class LogBusinessController implements IApiVersion {

    @Autowired
    LogBusinessService logBusinessService;

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps")
    public void getAppNameList(@RequestParam(name = "fromTime", required = true) long fromTime,
                               @RequestParam(name = "toTime", required = true) long toTime,
                               @PathVariable(value = "cluster", required = true) String cluster,
                               @PathVariable(value = "project", required = true) String project,
                               @RequestParam(name = "size", required = false, defaultValue = "1000") int size,
                               HttpServletResponse response) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        if (fromTime > toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime");
        }
        if (StringUtils.isEmpty(project)) {
            throw new IllegalArgumentException(" project parameters are not allowed to be empty");
        }
        ArrayList list = logBusinessService.getAppList(cluster, project, fromTime, toTime, size);
        Collections.sort(list);
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, list);
    }


    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/clusters/{cluster}/projects/{project}/apps/{app}/instances")
    public void getInstancesList(@RequestParam(name = "fromTime", required = true) long fromTime,
                               @RequestParam(name = "toTime", required = true) long toTime,
                               @RequestParam(name = "size", required = false, defaultValue = "1000") int size,
                               @PathVariable(value = "cluster", required = true) String cluster,
                               @PathVariable(value = "project", required = true) String project,
                               @PathVariable(value = "app", required = true) String app,
                               HttpServletResponse response) throws IOException, ESClusterNotFoundException, ESClientNotFoundException, ESClustersResponseTimeoutException {
        if (fromTime > toTime) {
            throw new IllegalArgumentException(" fromTime must be smaller than the toTime");
        }
        if (StringUtils.isEmpty(project)) {
            throw new IllegalArgumentException(" project parameters are not allowed to be empty");
        }
        ArrayList list = logBusinessService.getInstances(cluster, project, fromTime, toTime, size, app);
        Collections.sort(list);
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, list);
    }
}

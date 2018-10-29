package com.jiankunking.logsearch.controller;


import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.services.AgentBatchDeployService;
import com.jiankunking.logsearch.services.IDService;
import com.jiankunking.logsearch.services.YmlService;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/9/10 10:45
 * @description: 批量部署filebeat
 */
@Slf4j
@Controller
public class AgentBatchDeployController implements IApiVersion {

    @Autowired
    IDService idService;
    @Autowired
    YmlService ymlService;
    @Autowired
    ConsulStorage consulStorage;
    @Autowired
    AgentBatchDeployService agentBatchDeployService;

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.POST, value = "/projects/{project}/batch")
    public void create(@RequestBody FileBeatConfig fileBeatConfig,
                       @PathVariable(value = "project", required = true) String project,
                       HttpServletResponse response) throws IOException, InterruptedException {
        boolean state = agentBatchDeployService.batchUpdateFileBeatOptions(project, fileBeatConfig, response);
        if (state) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_CREATED, null);
            return;
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Install Failed");
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.PUT, value = "/projects/{project}/batch")
    public void update(@RequestBody FileBeatConfig fileBeatConfig,
                       @PathVariable(value = "project", required = true) String project,
                       HttpServletResponse response) throws IOException, InterruptedException {
        boolean state = agentBatchDeployService.batchUpdateFileBeatOptions(project, fileBeatConfig, response);
        if (state) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_CREATED, null);
            return;
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Install Failed");
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.DELETE, value = "/projects/{project}/batch")
    public void delete(@PathVariable(value = "project", required = true) String project,
                       @RequestBody List<String> ips,
                       HttpServletResponse response) {
        boolean state = agentBatchDeployService.batchUninstallFileBeatOptions(project, ips, response);
        if (state) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_CREATED, null);
            return;
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Install Failed");

    }

}

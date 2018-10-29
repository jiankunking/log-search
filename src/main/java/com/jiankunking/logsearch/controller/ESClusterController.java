package com.jiankunking.logsearch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.dto.ESCluster;
import com.jiankunking.logsearch.services.ConsulService;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.JsonUtils;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import static com.jiankunking.logsearch.config.GlobalConfig.ES_CLUSTER_PREFIX;

/**
 * @author jiankunking.
 * @date：2018/9/25 14:03
 * @description:
 */
@Slf4j
@Controller
public class ESClusterController implements IApiVersion {

    @Autowired
    ConsulStorage consulStorage;
    @Autowired
    ConsulService consulService;

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/uni/projects/{project}/clusters")
    public void getProjectList(@PathVariable(value = "project", required = true) String project,
                               HttpServletResponse response) throws UnsupportedEncodingException {
        List<ESCluster> result = consulService.getESClusters(project);
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.POST, value = "/uni/clusters/{cluster}/projects/{project}")
    public void addCluster(@PathVariable(value = "cluster", required = true) String cluster,
                           @PathVariable(value = "project", required = true) String project,
                           @RequestBody ESCluster esCluster,
                           HttpServletResponse response) throws JsonProcessingException {
        if (StringUtils.isEmpty(esCluster.getAddress())) {
            throw new IllegalArgumentException(" address has to have a value!");
        }
        esCluster.setName(cluster);
        esCluster.setProject(project);
        esCluster.setId(UUID.randomUUID().toString());

        String key = ES_CLUSTER_PREFIX + project + "/" + cluster;
        consulStorage.save(key, JsonUtils.toJSON(esCluster));
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, esCluster);
    }

}

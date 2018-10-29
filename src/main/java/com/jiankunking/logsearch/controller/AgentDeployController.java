package com.jiankunking.logsearch.controller;

import com.alibaba.fastjson.JSON;
import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.services.AgentDeployService;
import com.jiankunking.logsearch.services.IDService;
import com.jiankunking.logsearch.services.YmlService;
import com.jiankunking.logsearch.storage.ConsulStorage;
import com.jiankunking.logsearch.util.Base64Utils;
import com.jiankunking.logsearch.util.HttpUtils;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.util.StringUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import com.orbitz.consul.model.kv.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Controller
public class AgentDeployController implements IApiVersion {

    @Autowired
    ConsulStorage consulStorage;
    @Autowired
    YmlService ymlService;
    @Autowired
    IDService idService;
    @Autowired
    HttpUtils httpUtils;
    @Autowired
    AgentDeployService agentDeployService;


    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.POST, value = "/projects/{project}/ip/{ip:.+}")
    public void create(@RequestBody FileBeatConfig fileBeatConfig,
                       @PathVariable(value = "project", required = true) String project,
                       @PathVariable(value = "ip", required = true) String ip,
                       HttpServletResponse response) throws IOException {
        boolean state = updateFileBeatOptions(project, ip, fileBeatConfig, response);
        if (state) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_CREATED, null);
            return;
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Install Failed");
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.PUT, value = "/projects/{project}/ip/{ip:.+}")
    public void update(@RequestBody FileBeatConfig fileBeatConfig,
                       @PathVariable(value = "project", required = true) String project,
                       @PathVariable(value = "ip", required = true) String ip,
                       HttpServletResponse response) throws IOException {
        boolean state = updateFileBeatOptions(project, ip, fileBeatConfig, response);
        if (state) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_CREATED, null);
            return;
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Install Failed");
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.GET, value = "/projects/{project}")
    public void get(@PathVariable(value = "project", required = true) String project,
                    HttpServletResponse response) throws IOException {
        ////校验
        //if (StringUtils.isEmpty(project)) {
        //    throw new IllegalArgumentException(" project has to have a value!");
        //}
        String prefix = idService.getFileBeatExtPrefix(project);
        List<Value> list = consulStorage.getByKeyPrefix(prefix);

        List<FileBeatConfig> result = new ArrayList<>();
        for (Value item : list) {
            if (!item.getValue().isPresent()) {
                continue;
            }
            result.add(JSON.parseObject(Base64Utils.decoder(item.getValue().get()), com.jiankunking.logsearch.dto.FileBeatConfig.class));
        }
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.DELETE, value = "/projects/{project}/ip/{ip:.+}/version/{version}")
    public void delete(@PathVariable(value = "project", required = true) String project,
                       @PathVariable(value = "ip", required = true) String ip,
                       @PathVariable(value = "version", required = true) String version,
                       HttpServletResponse response) throws IOException {
        //校验
        //if (StringUtils.isEmpty(project)) {
        //    throw new IllegalArgumentException(" project has to have a value!");
        //}
        //if (StringUtils.isEmpty(ip)) {
        //    throw new IllegalArgumentException(" ip has to have a value!");
        //}
        //if (StringUtils.isEmpty(version)) {
        //    throw new IllegalArgumentException(" version has to have a value!");
        //}
        List<String> hosts = new ArrayList<>();
        hosts.add(ip);

        String ymlID = idService.getFileBeatYmlID(project, ip);

        if (!agentDeployService.uninstallStreamProcess(ymlID, version, ip, response)) {
            ResponseUtils.handleReqJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Uninstall Failed");
            return;
        }

        consulStorage.delete(ymlID);
        String extID = idService.getFileBeatExtID(project, ip);
        consulStorage.delete(extID);
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_NO_CONTENT, null);
    }


    private boolean updateFileBeatOptions(String project, String ip, FileBeatConfig fileBeatConfig, HttpServletResponse response) throws IOException {
        //校验
        if (StringUtils.isEmpty(project)) {
            throw new IllegalArgumentException(" project has to have a value!");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new IllegalArgumentException(" ip has to have a value!");
        }
        if (fileBeatConfig.getEsHosts() == null || fileBeatConfig.getEsHosts().size() == 0) {
            throw new IllegalArgumentException(" esHosts has to have a value!");
        }
        if (fileBeatConfig.getFileBeatTypeConfig() == null) {
            throw new IllegalArgumentException(" fileBeatTypeConfig has to have a value!");
        }
        if (StringUtils.isEmpty(fileBeatConfig.getVersion())) {
            throw new IllegalArgumentException(" version has to have a value!");
        }

        //强制添加 host id信息
        fileBeatConfig.setAddHostMetadata(true);

        fileBeatConfig.setProject(project);
        fileBeatConfig.setIps(Arrays.asList(ip));
        fileBeatConfig.setYmlID(idService.getFileBeatYmlID(project, ip));
        fileBeatConfig.setExtID(idService.getFileBeatExtID(project, ip));
        //创建yml
        String yml = ymlService.toYml(fileBeatConfig, ip);
        //保存yml到consul中
        if (!consulStorage.save(fileBeatConfig.getYmlID(), yml)) {
            throw new InternalError(" filebeat yml save failed");
        }

        //流处理
        agentDeployService.installStreamProcess(fileBeatConfig, response);
        return true;
    }


}

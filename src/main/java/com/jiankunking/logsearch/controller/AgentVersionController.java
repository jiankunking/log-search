package com.jiankunking.logsearch.controller;


import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.jiankunking.logsearch.dto.VersionEntity;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author jiankunking.
 * @date：2018/8/28 10:31
 * @description:
 */
@Controller
public class AgentVersionController implements IApiVersion {

    @ResponseBody
    @ControllerTimeAnnotation
    @RequestMapping(method = {RequestMethod.GET}, value = "/projects/{project}/agent/version")
    public void getAgentVersion(HttpServletResponse response) {
        HashMap<String, Object> result = new HashMap<>(2);
        List<VersionEntity> versionEntityList = new ArrayList<>();
        VersionEntity logVersionEntity = new VersionEntity();
        logVersionEntity.setLatestVersion(EnvionmentVariables.FILEBEAT_VERSION);
        logVersionEntity.setAgentName("fileBeatVersion");
        versionEntityList.add(logVersionEntity);

        result.put("version", versionEntityList);

        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, result);
    }

}

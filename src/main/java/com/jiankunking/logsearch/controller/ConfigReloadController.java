package com.jiankunking.logsearch.controller;

import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.cache.IndexRetentionTimeCache;
import com.jiankunking.logsearch.cache.NameSpacesCache;
import com.jiankunking.logsearch.util.ResponseUtils;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;


/**
 * @author jiankunking.
 * @date：2019/01/11 18:22
 * @description:
 */
@Slf4j
@Controller
public class ConfigReloadController implements IApiVersion {

    @Autowired
    IndexRetentionTimeCache indexRetentionTimeCache;
    @Autowired
    NameSpacesCache nameSpacesCache;

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.PUT, value = "/uni/config/namespaces")
    public void updateNamespaceCache(HttpServletResponse response) {
        indexRetentionTimeCache.init();
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
    }

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.PUT, value = "/uni/config/retention")
    public void updateRetentionTimeCache(HttpServletResponse response) {
        indexRetentionTimeCache.init();
        ResponseUtils.handleReqJson(response, HttpServletResponse.SC_OK, null);
    }
}

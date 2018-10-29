package com.jiankunking.logsearch.controller;


import com.jiankunking.logsearch.aspect.ControllerTimeAnnotation;
import com.jiankunking.logsearch.dto.FileBeatConfig;
import com.jiankunking.logsearch.version.IApiVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jiankunking.
 * @date：2018/9/14 11:01
 * @description:
 */
@Slf4j
@Controller
public class PipelineController implements IApiVersion {

    @ControllerTimeAnnotation
    @RequestMapping(method = RequestMethod.POST, value = "/projects/{project}/ip/{ip:.+}/pipeline/time")
    public void create(@RequestBody FileBeatConfig fileBeatConfig,
                       @PathVariable(value = "project", required = true) String project,
                       @PathVariable(value = "ip", required = true) String ip,
                       HttpServletResponse response) throws IOException {

    }
}

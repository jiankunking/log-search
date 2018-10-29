package com.jiankunking.logsearch.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author jiankunking.
 * @date：2018/10/11 19:32
 * @description:
 */
public class UrlInterceptor implements HandlerInterceptor {
    String project = "project", cluster = "cluster";

    /**
     * 重写url path project参数
     * 在控制器执行前调用
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        Map pathVars = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVars == null || pathVars.size() == 0) {
            return true;
        }
        if (pathVars.containsKey(project)) {
            pathVars.put(project, pathVars.get(project).toString().toLowerCase());
        }
        if (pathVars.containsKey(cluster)) {
            pathVars.put(cluster, pathVars.get(cluster).toString().toLowerCase());
        }
        return true;
    }
}


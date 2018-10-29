package com.jiankunking.logsearch.advice;


import com.jiankunking.logsearch.exception.ESClientNotFoundException;
import com.jiankunking.logsearch.exception.ESClusterNotFoundException;
import com.jiankunking.logsearch.exception.LengthOutOfBoundsException;
import com.jiankunking.logsearch.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理服务端异常 默认为500
     *
     * @param response
     * @param ex
     * @throws Exception
     */
    @ExceptionHandler(value = Exception.class)
    public void defaultErrorHandler(HttpServletResponse response, Exception ex) {
        ServletRequestAttributes servletContainer = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = "";
        if (servletContainer != null) {
            uri = servletContainer.getRequest().getRequestURI() + "?" + servletContainer.getRequest().getQueryString();
        }

        log.info("uri=" + uri + " defaultErrorHandler:", ex);
        //参数异常
        if (ex instanceof IllegalArgumentException) {
            ResponseUtils.handleBadReq(response, HttpServletResponse.SC_BAD_REQUEST, ex);
            return;
        }
        if (ex instanceof LengthOutOfBoundsException) {
            ResponseUtils.handleBadReq(response, HttpServletResponse.SC_BAD_REQUEST, ex);
            return;
        }
        if (ex instanceof ESClientNotFoundException || ex instanceof ESClusterNotFoundException) {
            ResponseUtils.handleBadReq(response, HttpServletResponse.SC_BAD_REQUEST, ex);
            return;
        }
        ResponseUtils.handleBadReq(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
    }
}

package com.jiankunking.logsearch.aspect;

import com.jiankunking.logsearch.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Slf4j
@Aspect
@Component
public class ControllerTimeConsumeAspect {

    @Pointcut("@annotation(com.jiankunking.logsearch.aspect.ControllerTimeAnnotation)")
    public void controllerAspect() {
        log.info(" controller timeConsumeAspect  Pointcut ");
    }

    @Around("controllerAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = joinPoint.proceed();
        long end = System.currentTimeMillis();

        ServletRequestAttributes servletContainer = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletContainer.getRequest();
        HttpServletResponse response = servletContainer.getResponse();
        String uri = request.getRequestURI();
        if (StringUtils.isNotEmpty(request.getQueryString())) {
            uri = uri + "?" + request.getQueryString();
        }

        log.info(String.format("%s  %d %dms %s",
                request.getMethod(),
                response.getStatus(),
                end - start,
                uri));
        return obj;
    }

}


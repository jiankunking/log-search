package com.jiankunking.logsearch.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author jiankunking.
 * @date：2018/9/28 14:57
 * @description:
 */
@Slf4j
@Aspect
@Component
public class MethodTimeConsumeAspect {

    @Pointcut("@annotation(com.jiankunking.logsearch.aspect.MethodTimeAnnotation)")
    public void methodAspect() {
        log.info(" method timeConsumeAspect  Pointcut ");
    }

    @Around("methodAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = joinPoint.proceed();
        long end = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toString();
        String methodName = signature.substring(signature.lastIndexOf(".") + 1, signature.indexOf("("));
        log.info(String.format("%dms %s",
                end - start,
                methodName));
        return obj;
    }
}

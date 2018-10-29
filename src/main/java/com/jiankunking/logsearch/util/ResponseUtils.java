package com.jiankunking.logsearch.util;

import com.alibaba.fastjson.JSONObject;
import com.jiankunking.logsearch.config.GlobalConfig;
import com.jiankunking.logsearch.dto.CustomBadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class ResponseUtils {
    private static Logger logger = LoggerFactory.getLogger(ResponseUtils.class);

    public static void handleReqJson(HttpServletResponse response,
                                     int status,
                                     Object data) {
        response.setStatus(status);
        Object responseObject = JSONObject.toJSON(data);
        response.setCharacterEncoding(GlobalConfig.CHARSET);
        response.setContentType(GlobalConfig.CONTENT_TYPE);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (responseObject != null) {
                out.append(responseObject.toString());
            }
        } catch (IOException e) {
            logger.error("handleReqJson error:", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void handleBadReqString(HttpServletResponse response,
                                          int status,
                                          String exception) {
        response.setStatus(status);
        Object responseObject = JSONObject.toJSON(CustomBadResponse.fail(status, exception));
        response.setCharacterEncoding(GlobalConfig.CHARSET);
        response.setContentType(GlobalConfig.CONTENT_TYPE);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(responseObject.toString());
        } catch (IOException e) {
            logger.error("handleBadReqString error:", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void handleBadReq(HttpServletResponse response,
                                    int status,
                                    Exception exception) {
        response.setStatus(status);
        Object responseObject = JSONObject.toJSON(CustomBadResponse.fail(status, exception));
        response.setCharacterEncoding(GlobalConfig.CHARSET);
        response.setContentType(GlobalConfig.CONTENT_TYPE);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(responseObject.toString());
        } catch (IOException e) {
            logger.error("handleBadReq error:", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}

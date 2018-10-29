package com.jiankunking.logsearch.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class JsonUtils {

    /**
     * 将对象转换为json格式字符串
     *
     * @param obj
     * @return json string
     */
    public static String toJSON(Object obj) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(obj);
    }

    /**
     * 将json形式字符串转换为java实体类
     */
    public static <T> T parse(String jsonStr, Class<T> clazz) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(jsonStr, clazz);
    }
}

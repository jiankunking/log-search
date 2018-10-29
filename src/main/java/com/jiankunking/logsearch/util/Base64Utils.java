package com.jiankunking.logsearch.util;


import com.jiankunking.logsearch.config.GlobalConfig;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class Base64Utils {

    /**
     * 解码
     *
     * @param text
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decoder(String text) throws UnsupportedEncodingException {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(text), GlobalConfig.CHARSET);
    }

    /**
     * 编码
     *
     * @param text
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encoder(String text) throws UnsupportedEncodingException {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] bytes = text.getBytes(GlobalConfig.CHARSET);
        return encoder.encodeToString(bytes);
    }
}

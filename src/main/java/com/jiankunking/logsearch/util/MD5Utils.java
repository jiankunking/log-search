package com.jiankunking.logsearch.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jiankunking.
 * @date：2018/9/29 10:58
 * @description:
 */
public class MD5Utils {

    /**
     * 生成字符串的md5码
     *
     * @param args
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5String(String... args) throws NoSuchAlgorithmException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            if (StringUtils.isEmpty(arg)) {
                continue;
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append("_");
            }
            stringBuilder.append(arg);
        }
        return getMD5String(stringBuilder.toString());
    }

    /**
     * 生成字符串的md5码
     *
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5String(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        //使用指定的字节数组更新摘要。
        md.update(str.getBytes());
        //通过执行诸如填充之类的最终操作完成哈希计算。
        byte[] b = md.digest();
        //生成具体的md5密码到buf数组
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }
}

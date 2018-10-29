package com.jiankunking.logsearch.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class ExceptionUtils {

    public static String getException(Exception ex) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        ex.printStackTrace(printStream);
        String ret = new String(byteArrayOutputStream.toByteArray());
        printStream.close();
        byteArrayOutputStream.close();
        return ret;
    }

}

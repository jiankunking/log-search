package com.jiankunking.logsearch.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author jiankunking.
 * @date：2018/10/15 15:00
 * @description:
 */
public class ZipUtils {

    private static Logger logger = LoggerFactory.getLogger(ZipUtils.class);

    private static byte[] bytes = new byte[1024];

    /**
     * 压缩文件或路径
     *
     * @param zip     压缩的目的地址
     * @param srcFile 压缩的源文件
     */
    public static void zipFile(String zip, File srcFile) throws IOException {
        if (zip.endsWith(".zip") || zip.endsWith(".ZIP")) {
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(zip)))) {
                zipOut.setEncoding("GBK");
                handlerFile(zip, zipOut, srcFile, "");
            }
        } else {
            logger.info("target file[" + zip + "] is not .zip type file");
        }
    }

    /**
     * @param zip     压缩的目的地址
     * @param zipOut
     * @param srcFile 被压缩的文件信息
     * @param path    在zip中的相对路径
     * @throws IOException
     */
    private static void handlerFile(String zip, ZipOutputStream zipOut, File srcFile, String path) throws IOException {
        logger.info(" begin to compression file[" + srcFile.getName() + "]");
        if (!"".equals(path) && !path.endsWith(File.separator)) {
            path += File.separator;
        }
        if (!srcFile.getPath().equals(zip)) {
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                if (files.length == 0) {
                    zipOut.putNextEntry(new ZipEntry(path + srcFile.getName() + File.separator));
                    zipOut.closeEntry();
                } else {
                    for (File f : files) {
                        handlerFile(zip, zipOut, f, path + srcFile.getName());
                    }
                }
            } else {
                try (InputStream in = new FileInputStream(srcFile)) {
                    zipOut.putNextEntry(new ZipEntry(path + srcFile.getName()));
                    int len = 0;
                    while ((len = in.read(bytes)) > 0) {
                        zipOut.write(bytes, 0, len);
                    }
                } finally {
                    if (zipOut != null) {
                        zipOut.closeEntry();
                    }
                }

            }
        }
    }


}

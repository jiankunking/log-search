package com.jiankunking.logsearch.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Deprecated
public class AppendFile {
    /**
     * 使用RandomAccessFile
     *
     * @param fileName 文件名
     * @param content  追加的内容
     */
    public static void appendToEnd(String fileName, String content) throws IOException {
        try (FileChannel fileChannel = new RandomAccessFile(fileName, "rw").getChannel()) {
            fileChannel.write(ByteBuffer.wrap(content.getBytes()), fileChannel.size());
        }
    }
}

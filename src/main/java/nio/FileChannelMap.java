package nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Created by Steven on 2015/11/21 0021.
 */
public class FileChannelMap {

    /**
     * FileChanel.map 将文件按照一定大小块映射为内存区域，当程序访问这个内存区域时，将直接操作这个文件数据
     * 这种方式省去了数据从内核空间向用户空间复制的损耗。这种方式适合对大文件的只读性操作，入大文件的MD5校验。
     * 但这种方式适合操作系统的底层I/O实现相关的
     * @param atgs
     */
    public static void map(String[] atgs) {
        int BUFFER_SIZE = 1024;
        String filename = "test.db ";
        long fileLength = new File(filename).length();
        int buffercount = 1 + (int) (fileLength / BUFFER_SIZE);
        long remaining = fileLength;
        for (int i = 0; i < buffercount; i++) {
            RandomAccessFile file;
            try {
                file = new RandomAccessFile(filename, "r");
                file.getChannel().map(FileChannel.MapMode.READ_ONLY, i * BUFFER_SIZE, (int) Math.min(remaining, BUFFER_SIZE));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            remaining -= BUFFER_SIZE;
        }

    }

}

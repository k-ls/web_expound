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
     * FileChanel.map ���ļ�����һ����С��ӳ��Ϊ�ڴ����򣬵������������ڴ�����ʱ����ֱ�Ӳ�������ļ�����
     * ���ַ�ʽʡȥ�����ݴ��ں˿ռ����û��ռ临�Ƶ���ġ����ַ�ʽ�ʺ϶Դ��ļ���ֻ���Բ���������ļ���MD5У�顣
     * �����ַ�ʽ�ʺϲ���ϵͳ�ĵײ�I/Oʵ����ص�
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

package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Steven on 2015/11/21 0021.
 */
public class Select_Channel {

    // ���͵�NIO���룺
    public void selector() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // ���÷�����ʽ
        ssc.socket().bind(new InetSocketAddress(8080));
        ssc.register(selector, SelectionKey.OP_ACCEPT); // ע������¼�
        while (true) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();// ȡ������key����
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if ((selectionKey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                    ServerSocketChannel ssChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel sc = ssChannel.accept(); // ���շ���˵�����
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    iterator.remove();
                }else if ((selectionKey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    while (true) {
                        buffer.clear();
                        int i = sc.read(buffer);
                        if (i <= 0) {
                            break;
                        }
                        buffer.flip();
                    }
                    iterator.remove();
                }
            }
        }
    }
}

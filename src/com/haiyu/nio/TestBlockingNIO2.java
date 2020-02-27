package com.haiyu.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Desc:
 * @Author: liuxing
 * @Date: 2020/2/27 14:32
 * @Version 1.0
 */
public class TestBlockingNIO2 {

    //客户端
    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));

        FileChannel inChannel = FileChannel.open(Paths.get("1.jpeg"), StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (inChannel.read(buffer) != -1){
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }

        socketChannel.shutdownOutput();

        //接受服务端的反馈
        int len = 0;
        while ((len = socketChannel.read(buffer)) != -1){
            buffer.flip();
            System.out.println(new String(buffer.array(),0,len));
            buffer.clear();
        }

        inChannel.close();
        socketChannel.close();
    }

    //服务端
    @Test
    public void server() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        FileChannel outChannel = FileChannel.open(Paths.get("server2.jpeg"),StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        serverSocketChannel.bind(new InetSocketAddress(9898));

        SocketChannel socketChannel = serverSocketChannel.accept();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (socketChannel.read(buffer) != -1){
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }

        //发送数据反馈给客户端
        buffer.put("服务端接受数据成功".getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        socketChannel.close();
        outChannel.close();
        serverSocketChannel.close();

    }

}

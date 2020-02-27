package com.haiyu.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * @Desc:
 *  Pipe(管道)
 *  Java NIO管道是2个线程之间的单向数据连接。
 *  Pipe有一个source通道和一个sink通道。数据会
 *  被写到sink通道，从source通道读取。
 *
 *
 * @Author: liuxing
 * @Date: 2020/2/27 15:53
 * @Version 1.0
 */
public class TestPipe {

    @Test
    public void test1() throws IOException {
        //1.获取管道
        Pipe pipe = Pipe.open();

        //2.将缓存区中的数据写入管道
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        Pipe.SinkChannel sinkChannel = pipe.sink();
        buffer.put("通过单向管道发送数据".getBytes());
        buffer.flip();
        sinkChannel.write(buffer);

        //3.读取缓冲区中的数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        buffer.flip();
        int len = sourceChannel.read(buffer);
        System.out.println(new String(buffer.array(),0,len));

        sourceChannel.close();
        sinkChannel.close();
    }
}

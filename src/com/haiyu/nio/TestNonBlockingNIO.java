package com.haiyu.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @Desc:
 * 一、使用NIO完成网络通信的三个核心
 *
 * 1.通道（Channel）： 负责连接
 *
 *     java.nio.channels.Channel
 *          \--SelectableChannel
 *              \--SocketChannel
 *              \--ServerSocketChannel
 *              \--DatagramChannel
 *
 *              \--Pipe.SinkChannel
 *              \--Pipe.SourceChannel
 *
 * 2.缓冲区(Buffer)： 负责数据存取
 *
 * 3.选择器（Selector）：是SelectableChannel的多路复用器，用于监控SelectableChannel的IO状况
 *
 * @Author: liuxing
 * @Date: 2020/2/27 14:08
 * @Version 1.0
 */
public class TestNonBlockingNIO {

    //客户端
    @Test
    public void client() throws IOException {
        //1.获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));

        //2.切换非阻塞模式
        socketChannel.configureBlocking(false);

        //3.分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //4.发送数据到服务端
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String str = scanner.next();
            buffer.put((new Date().toString() + "\n" +str).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }

        //5.关闭通道
        socketChannel.close();
    }

    //服务端
    @Test
    public void server() throws IOException {
        //1.获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //2.切换非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //3.绑定端口连接
        serverSocketChannel.bind(new InetSocketAddress(9898));

        //4.获取选择器
        Selector selector = Selector.open();

        //5.将通道注册到选择器上,并且指定“监听接受事件”
        /**
         * SelectionKey的四个常量
         * 读: SelectionKey.OP_READ
         * 写: SelectionKey.OP_WRITE
         * 连接: SelectionKey.OP_CONNECT
         * 接收: SelectionKey.OP_ACCEPT
         *
         * 若注册时不止监听一个事件。则使用“位或”操作符连接。
         *   int interestSet = SelectionKey.OP_ACCEPT | SelectionKey.OP_READ;
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6.轮询式的获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0){
            //7.获取当前选择器中所有注册的“选择键（已就绪的监听事件）”
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while (it.hasNext()){
                //8.获取准备“就绪”的事件
                SelectionKey sk = it.next();

                //9.判断具体是什么事件准备就绪
                if(sk.isAcceptable()){
                    //10.若“接收就绪”,获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    //11.切换非阻塞模式
                    socketChannel.configureBlocking(false);

                    //12.将该通道注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(sk.isReadable()){
                    //13.获取当前选择器上“该就绪”状态的通道
                    SocketChannel socketChannel = (SocketChannel) sk.channel();

                    //14.读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = socketChannel.read(buffer)) > 0 ){
                        buffer.flip();
                        System.out.println(new String(buffer.array(),0,len));
                        buffer.clear();
                    }
                }

                //15. 取消选择键 SelectionKey
                it.remove();
            }

        }


    }

}

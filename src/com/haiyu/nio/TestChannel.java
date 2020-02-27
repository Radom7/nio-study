package com.haiyu.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 * @Desc:
 * 一、通道（Channel）: 用于源节点与目标节点的连接。
 *      在Java NIO 中负责缓冲区中数据的传输。Channel本身不存储数据，
 *      因此需要配合缓冲区进行传输。
 *
 * 二、通道的主要实现类
 * java.nio.channels.Channel 接口：
 *      \--FileChannel
 *      \--SocketChannel
 *      \--ServerSocketChannel
 *      \--DatagramChannel
 *
 * 三、获取通道
 * 1.Java针对支持通断的类提供了getChannel()方法
 *      本地IO：
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile
 *
 *      网络IO：
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 *
 * 2.在JDK1.7中的NIO.2针对各个通道提供了静态方法open()
 * 3.在JDK1.7中的NIO.2的Files工具类的newByteChannel()
 *
 *四、通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * 五、 分散（Scatter）与聚集（Gather）
 * 分散读取（Scattering Reads）: 将通道中的数据分散到多个缓冲区中。
 *      注意：按照缓冲区的顺序，将Channel中读取的数据依次将Buffer填满。
 * 聚集写入（Gathering Writes）: 将多个缓冲区中的数据聚集到通道中。
 *      注意：按照缓冲区的数据，写入position和limit之间的数据到Channel。
 *
 * 六、字符集：Charset
 * 编码：字符串 -> 字符数组
 * 解码：字符数组 -> 字符串
 *
 *
 * @Author: liuxing
 * @Date: 2020/2/27 11:46
 * @Version 1.0
 */
public class TestChannel {

    //1.利用通道完成文件的复制(非直接缓冲区)
    @Test
    public void test1(){
        long start = System.currentTimeMillis();

        FileInputStream fis = null;
        FileOutputStream fos = null;

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            fis = new FileInputStream("1.jpeg");
            fos = new FileOutputStream("2.jpeg");

            //1.获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //2.分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //3.将通道中的数据存入缓冲区中
            while (inChannel.read(buffer) != -1){
                buffer.flip();//切换读取数据的模式
                //4.将缓冲区中的数据写入通道中
                outChannel.write(buffer);
                buffer.clear();//清空缓冲区
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(outChannel != null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(inChannel != null){
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(fos != null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(fis != null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("耗费时间为："+(end - start));
    }

    //2. 使用直接缓冲区完成文件的复制(内存镜像文件)
    @Test
    public void test2() throws IOException {
        long start = System.currentTimeMillis();

        FileChannel inChannel = FileChannel.open(Paths.get("1.jpeg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("3.jpeg"), StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        //内存映射文件
        MappedByteBuffer  inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY,0,inChannel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE,0,inChannel.size());

        //直接对缓冲区进行数据的读写操作
        byte[] dis = new byte[inMappedBuf.limit()];
        //读
        inMappedBuf.get(dis);
        //写
        outMappedBuf.put(dis);

        inChannel.close();
        outChannel.close();

        long end = System.currentTimeMillis();
        System.out.println("耗费时间为："+(end - start));
    }


    //通道之间的数据传输(直接缓冲区)
    @Test
    public void test3() throws IOException {

        FileChannel inChannel = FileChannel.open(Paths.get("1.jpeg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("4.jpeg"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

//        inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel,0,inChannel.size());

        outChannel.close();
        inChannel.close();
    }

    //分散和聚集
    @Test
    public void test4() throws IOException {
        RandomAccessFile raf1 = new RandomAccessFile("1.txt","rw");

        //1.获取通道
        FileChannel fileChannel = raf1.getChannel();

        //2.分配指定大小的缓冲区
        ByteBuffer buffer1 = ByteBuffer.allocate(100);
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);

        //3.分散读取
        ByteBuffer[] buffers = {buffer1,buffer2};
        fileChannel.read(buffers);

        for(ByteBuffer buffer : buffers){
            buffer.flip();
        }

        System.out.println(new String(buffers[0].array(),0,buffers[0].limit()));
        System.out.println("-----------------------------");
        System.out.println(new String(buffers[1].array(),0,buffers[1].limit()));

        //4.聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt","rw");
        FileChannel channel2 = raf2.getChannel();
        channel2.write(buffers);
    }

    //字符集
    @Test
    public void test5(){
        Map<String,Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String,Charset>> set = map.entrySet();

        for(Map.Entry<String,Charset> entry :set){
            System.out.println(entry.getKey() + "=" +entry.getValue());
        }
    }

    //编码解码
    @Test
    public void test6() throws IOException {
        Charset charset = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = charset.newEncoder();

        //获取解码器
        CharsetDecoder de = charset.newDecoder();

        CharBuffer charBuffer = CharBuffer.allocate(1024);
        charBuffer.put("中国");
        charBuffer.flip();

        //编码
        ByteBuffer buffer = ce.encode(charBuffer);
        for (int i = 0 ; i < buffer.limit();i++) {
            System.out.println(buffer.get());
        }

        //解码
        buffer.flip();
        CharBuffer charBuffer2 = de.decode(buffer);
        System.out.println(charBuffer2.toString());

        System.out.println("---------------------------------------");
        //UTF-8解码
        Charset charset2 = Charset.forName("UTF-8");
        buffer.flip();
        CharBuffer charBuffer3 = charset2.decode(buffer);
        System.out.println(charBuffer3.toString());

    }
}

package com.haiyu.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @Desc: Buffer(缓冲区)
 * 一、缓冲区：在Java NIO 中负责数据的读取。缓冲区就是数组，用于存储不同数据类型的数据
 * ByteBuffer
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 *
 * 上述缓冲区高的管理方式几乎一致，通过allocate()获取缓冲区
 *
 *二、缓冲区存数据的两个核心方法
 * put() : 存入数据到缓冲区中
 * get() : 获取缓冲区中的数据
 *
 * 三、缓冲区的四个核心属性：
 * capacity : 容量，表示缓冲区中最大存储数据的容量，一旦声明不能改变。
 * limit : 界限，表示缓冲区中可以操作数据的大小（limit后数据不能进行读写）
 * position : 位置，表示缓冲区中正在操作数据的位置
 * mark ： 标志，表示记录当前position的位置。可以通过reset()恢复到mark的记录
 *
 * 大小排序：mark <= position <= limit <= capacity
 *
 * 四、直接缓冲区于非直接缓冲区
 * 非直接缓冲区： 通过allocate()方法分配缓冲区，将缓冲区建立在JVM的内存中
 * 直接缓冲区： 通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在物理内存中，可以提高效率
 *
 * @Author: liuxing
 * @Date: 2020/2/27 10:21
 * @Version 1.0
 */
public class TestBuffer {

    @Test
    public void test1(){
        String str = "abcde";

        //1.分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("********allocate*******");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //2.利用put()存入数据到缓冲区中
        buffer.put(str.getBytes());
        System.out.println("********put()*******");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //3.切换读取数据模式
        buffer.flip();
        System.out.println("********flip()*******");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //4.利用get()读取缓冲区中的数据
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        System.out.println("********get()*******");
        System.out.println(new String(dst,0,dst.length));
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //5. rewind(): 可重复读数据
        buffer.rewind();
        System.out.println("********rewind()*******");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //6. clear():清空缓冲区，但是缓冲区的数据依然存在。只是出于“被遗忘”状态
        buffer.clear();
        System.out.println("********clear()*******");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        System.out.println((char)buffer.get());
    }

    @Test
    public void test2(){
        String str = "abcde";

        //1.分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(str.getBytes());
        buffer.flip();
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst,0,2);
        System.out.println(new String(dst,0,2));
        System.out.println(buffer.position());

        //mark() : 标志
        buffer.mark();
        buffer.get(dst,2,2);
        System.out.println(new String(dst,2,2));
        System.out.println(buffer.position());

        //reset() : 回到mark 位置
        buffer.reset();
        System.out.println(buffer.position());

        //判断缓冲区中是否还有剩余数据
        if(buffer.hasRemaining()){
            //获取缓冲区中position位置之后可以操作的数量
            System.out.println(buffer.remaining());
        }
    }

    @Test
    public void test3(){
        //直接缓冲区 （直接放在物理内存中，无内存复制）
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        //非直接缓冲区（缓冲区复制）
        buffer = ByteBuffer.allocateDirect(1024);
    }
}

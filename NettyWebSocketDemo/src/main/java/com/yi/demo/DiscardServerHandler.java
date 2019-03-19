package com.yi.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerAdapter;

/**
 * Handles a server-side channel.
 * DISCARD服务(丢弃服务，指的是会忽略所有接收的数据的一种协议)
 */
public class DiscardServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //每当从客户端收到新的数据时，这个方法会在收到消息时被调用，这里收到的消息的类型是ByteBuf
        /*
            为了实现DISCARD协议，处理器不得不忽略所有接受到的消息。
            ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放。
            请记住处理器的职责是释放所有传递到处理器的引用计数对象
         */
        ((ByteBuf) msg).release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当Netty由于IO错误或者处理器在处理事件时抛出的异常时
        // 在大部分情况下，捕获的异常应该被记录下来并且把关联的channel给关闭掉
        cause.printStackTrace();
        ctx.close();
    }
}
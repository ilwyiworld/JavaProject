package com.yi.io.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

public class ServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        /**
         每当从客户端收到新的数据时，这个方法会在收到消息时被调用，这里收到的消息的类型是 ByteBuf，
         ByteBuf 是一个引用计数对象，这个对象必须显示地调用 release() 方法来释放；
         处理器的职责是释放所有传递到处理器的引用计数对象。
         */
        //do something msg
        ByteBuf buf = (ByteBuf) msg;
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        // 可以直接强转成 String 类型，也可以直接输出，因为 msg 已经是 String 类型了
        /*String data2 = (String) msg;
        System.out.println(new String(data2).trim());*/
        String request = new String(data, "utf-8");
        System.out.println("Server: " + request);
        //写给客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer("888".getBytes())).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，
        // 即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
        // 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉
        cause.printStackTrace();
        ctx.close();
    }
}
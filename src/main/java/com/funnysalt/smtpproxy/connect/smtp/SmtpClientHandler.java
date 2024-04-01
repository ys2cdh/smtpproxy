package com.funnysalt.smtpproxy.connect.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SmtpClientHandler extends ChannelInboundHandlerAdapter {

    SmtpConnector smtpConnector;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public SmtpClientHandler(SmtpConnector smtpConnector) {
        this.smtpConnector=smtpConnector;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("채널 등록");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("채널 연결이 종료됨.");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("channelRead");

        ByteBuf buf = (ByteBuf)msg;
        int n = buf.readableBytes();
        for( int i = 0; i < n ; i++) {
            byte b =buf.getByte(i);
            outputStream.write(b);
            if ('\n' == b) {
                String strTemp = new String(outputStream.toByteArray());
                String[] strChecks = strTemp.split("\r\n");
                for (int j = 0; j < strChecks.length; j++){
                    if (3 != strChecks[j].indexOf("-") && 2 < strChecks[j].length()) {

//                        System.out.println(strTemp);
                        smtpConnector.readComplete(ctx,strTemp);
                        outputStream.reset();

                    }
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("channelReadComplete");
    }

    public static void sendMsg(ChannelHandlerContext ctx, byte []b) {

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(b);
        ChannelFuture lastWriteFuture =ctx.writeAndFlush( messageBuffer ); //메시지를 발송하고 flush처리
        System.out.println(lastWriteFuture);

    }

    public static void sendMsg(ChannelHandlerContext ctx, String msg) {

        sendMsg(ctx,msg.getBytes());
        System.out.println(msg);
    }
}

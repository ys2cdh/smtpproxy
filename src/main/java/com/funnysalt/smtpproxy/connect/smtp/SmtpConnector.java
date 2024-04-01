package com.funnysalt.smtpproxy.connect.smtp;

import com.funnysalt.smtpproxy.BeanUtils;
import com.funnysalt.smtpproxy.smtpConfig.SmtpConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.smtp.SmtpCommand;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


//smtp로 서버에 연결하여 필요한 정보를 가져온다.
public class SmtpConnector {

    private Bootstrap bs = new Bootstrap();
    private SocketAddress addr;
    private ChannelFuture f;
    private SmtpCommand curSmtpCommand = SmtpCommand.EMPTY;

    public SmtpConnector(String host, int port){
        addr = new InetSocketAddress(host, port);
    }

    //smtp 연결
    public boolean connect(){
        if ( null == addr){
            return false;
        }

        SmtpConnector smtpConnector = this;

        bs.group(new NioEventLoopGroup(3))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("clientHandler", new SmtpClientHandler(smtpConnector));
                    }
                });


        f = bs.connect(addr);

        return true;
    }

    public void readComplete(ChannelHandlerContext ctx, String strTemp) {
        System.out.println(strTemp);

        SmtpConfig smtpConfig = (SmtpConfig) BeanUtils.getBean("smtpConfig");

        //처음 연결 했을 때
        if (SmtpCommand.EMPTY.equals(curSmtpCommand)) {
            SmtpClientHandler.sendMsg(ctx,"ehlo "+ smtpConfig.getServerName()+"\r\n");
            curSmtpCommand = SmtpCommand.EHLO;
        }
        // ehlo 연결 다음
        else if(SmtpCommand.EHLO.equals(curSmtpCommand)) {

        }
    }
}

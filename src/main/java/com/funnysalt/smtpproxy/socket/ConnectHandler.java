package com.funnysalt.smtpproxy.socket;

import com.funnysalt.smtpproxy.BeanUtils;
import com.funnysalt.smtpproxy.smtpConfig.SmtpConfig;
import com.funnysalt.smtpproxy.smtpSession.SmtpSessionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;


//최초 접속시 이벤트 처리 Class
//ChannelHandlerContext(ConnectHandler#0, [id: 0x872bf276, L:/127.0.0.1:8025 - R:/127.0.0.1:34360])
public class ConnectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        SmtpConfig smtpConfig = (SmtpConfig)BeanUtils.getBean("smtpConfig");
        ctx.channel().write("220 "+ smtpConfig.getServerName()+" ESMTP Ready\r\n");
        ctx.channel().flush();

        System.out.println(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            super.channelRead(ctx, msg);
            System.out.println(ctx);
        }catch ( Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SmtpSessionFactory smtpSessionFactory = (SmtpSessionFactory) BeanUtils.getBean("smtpSessionFactory");
        smtpSessionFactory.close(ctx.channel());
        System.out.println(LocalDateTime.now().toString() + " channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        SmtpSessionFactory smtpSessionFactory = (SmtpSessionFactory) BeanUtils.getBean("smtpSessionFactory");
        smtpSessionFactory.close(ctx.channel());
        System.out.println(LocalDateTime.now().toString() + " handlerRemoved");
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
        System.out.println(LocalDateTime.now().toString() + " exceptionCaught");
    }
}

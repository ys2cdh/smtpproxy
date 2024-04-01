package com.funnysalt.smtpproxy.smtpResponse;


import com.funnysalt.smtpproxy.BeanUtils;
import com.funnysalt.smtpproxy.smtpSession.SmtpSessionFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

public class SmtpResponse extends LineBasedFrameDecoder {

    private Executor executor;
    private SmtpSessionFactory smtpSessionFactory;

    public SmtpResponse(int maxLength) {
        super(maxLength);
        smtpSessionFactory = (SmtpSessionFactory)BeanUtils.getBean("smtpSessionFactory");
        executor = ((ThreadPoolTaskExecutor)BeanUtils.getBean("executor"));
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        System.out.println("ctx " + ctx);
        ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
        if ( null == frame){
            return null;
        }


        SmtpResponseWork smtpResopnseWork = new SmtpResponseWork(smtpSessionFactory.getSession(ctx).processData(frame.toString(CharsetUtil.UTF_8)));
        executor.execute(smtpResopnseWork);

        return null;
    }
    //    @Override
//    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
//        System.out.println(ctx);
//        System.out.println(msg);
//        System.out.println(out);
//    }
}

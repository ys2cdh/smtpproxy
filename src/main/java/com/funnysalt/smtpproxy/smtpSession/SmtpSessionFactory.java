package com.funnysalt.smtpproxy.smtpSession;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.context.annotation.Configuration;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

@Configuration
public class SmtpSessionFactory implements Closeable {

    private HashMap<ChannelId,SmtpSession> mapSmtpSession = new HashMap<ChannelId,SmtpSession>();
    private final ChannelGroup allChannels;

    public SmtpSessionFactory() {
        allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public SmtpSession getSession(ChannelHandlerContext ctx){
        Channel channel = null;
        if (null == (channel = allChannels.find(ctx.channel().id())) ) {
            allChannels.add(ctx.channel());
            channel = ctx.channel();
        }
        if ( !mapSmtpSession.containsKey(channel.id())){
            mapSmtpSession.put(channel.id(),new SmtpSession(channel));
        }

        return mapSmtpSession.get(channel.id());
    }

    public void close(Channel channel){
        channel = allChannels.find(channel.id());
        if (null != channel){
            SmtpSession smtpSession = mapSmtpSession.remove(channel.id());
            if (null != smtpSession){
                smtpSession.close();
            }
            channel.close();
            allChannels.remove(channel);


        }
    }


    @Override
    public void close() throws IOException {
        allChannels.close();
    }
}

package com.funnysalt.smtpproxy.smtpRequest;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;


import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

public class TestRequest extends MessageToMessageEncoder<Object>  {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        System.out.println(ctx);
        System.out.println(msg);
        System.out.println(out);
        out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap((String)msg), Charset.forName("UTF-8")));

    }
}

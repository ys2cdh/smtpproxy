package com.funnysalt.smtpproxy.smtpSession;

import io.netty.channel.Channel;
import io.netty.handler.codec.smtp.SmtpCommand;

import java.net.InetSocketAddress;

public class SmtpSession {
    private Channel channel;
    private String strData = "";
    private String strRemoteIP = "";
    private SmtpCommand curSmtpCommand;

    public SmtpSession(Channel channel) {
        this.channel = channel;
        strRemoteIP = ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
    }

    public SmtpSession processData(String strData) {
        this.strData = strData;

        return this;
    }

    public Channel  getChannel() {
        return channel;
    }
    public String  getSmtpData() {
        return strData;
    }

    public String getSmtpState() {

        if ( null == curSmtpCommand){
            return "first";
        }

        return curSmtpCommand.name().toString().toLowerCase();
    }

    public void setSmtpState(SmtpCommand smtpState) {
        curSmtpCommand = smtpState;
    }

    public String getRemoteIP(){
        return strRemoteIP;
    }

    public void sendMessage(String s) {
        channel.write(s);
        channel.flush();
    }

    //연결 Connect을 연결 해제할 필요가 있는 경우 해제 한다.
    public void close() {
    }
}

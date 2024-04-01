package com.funnysalt.smtpproxy.init;

import com.funnysalt.smtpproxy.socket.NettyServerSocket;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


// 서버가 시작하기 전에 초기화가 필요한 작업을 여기서 한다.
@Component
@RequiredArgsConstructor
public class SMTPProxyInit implements ApplicationListener<ApplicationReadyEvent> {

    private final NettyServerSocket nettyServerSocket;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        nettyServerSocket.start();
    }
}

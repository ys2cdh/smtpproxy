package com.funnysalt.smtpproxy.smtpConfig;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SmtpConfig {
    private String serverName="test.com";
    // 메일 하나 당 최대로 받을 수 있는 byte
    private String smtpMaxSize = "99000000";

    public String getServerName() {
        return serverName;
    }

    public String getSmtpMaxSize() {
        return smtpMaxSize;
    }
}

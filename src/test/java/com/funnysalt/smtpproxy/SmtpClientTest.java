package com.funnysalt.smtpproxy;

import com.funnysalt.smtpproxy.connect.smtp.SmtpConnector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class SmtpClientTest {

    @Test
    void connectTest() throws InterruptedException {
        SmtpConnector smtpConnector = new SmtpConnector("175.206.170.35",25);
        Assertions.assertThat(smtpConnector.connect()).isEqualTo(true);

        Thread.sleep(10000);
    }
}

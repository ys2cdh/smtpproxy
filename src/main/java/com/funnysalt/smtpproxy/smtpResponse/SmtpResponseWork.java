package com.funnysalt.smtpproxy.smtpResponse;

import com.funnysalt.smtpproxy.BeanUtils;
import com.funnysalt.smtpproxy.smtpConfig.SmtpConfig;
import com.funnysalt.smtpproxy.smtpSession.SmtpSession;
import com.funnysalt.smtpproxy.smtpSession.SmtpSessionFactory;
import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.util.CharsetUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmtpResponseWork implements Runnable{

    private SmtpSession smtpSession;
    private SmtpConfig smtpConfig;

    private String strAuthID;

    public SmtpResponseWork(SmtpSession smtpSession){
        this.smtpSession = smtpSession;
    }

    @Override
    public void run() {

        smtpConfig = (SmtpConfig)BeanUtils.getBean("smtpConfig");

        System.out.println(smtpSession.getSmtpData());

        //데이터가 없으면 skip처리
        if (null == smtpSession.getSmtpData() || 0 == smtpSession.getSmtpData().length()){
            return;
        }
        // command을 분리한다
        String strCommand = smtpSession.getSmtpData().split(" ")[0];
        boolean bBadSequence = true;
        switch (strCommand.toLowerCase()){
            case "ehlo":
                // ehlo는 접속 초기에만 사용 할 수 있다.
                if (smtpSession.getSmtpState().equals("first")){
                    ehlo();
                    bBadSequence = false;
                }
                break;
            //mail from 처리
            case "mail":
                // mail from은 auth , ehlo , reset 상태 에서만 사용 할 수 있다.
                if (smtpSession.getSmtpState().equals("mail") || smtpSession.getSmtpState().equals("rset")
                    || smtpSession.getSmtpState().equals("rcpt")){
                    mailFrom();
                    bBadSequence = false;
                }
                break;
            //rcpt to 처리
            case "rcpt":
                // rcpt to은 mail from , rcpt to, 상태 에서만 사용 할 수 있다.
                if (smtpSession.getSmtpState().equals("ehlo") || smtpSession.getSmtpState().equals("rset")
                        || smtpSession.getSmtpState().equals("auth")){
                    rcptTo();
                    bBadSequence = false;
                }
                break;
            //연결 종료
            case "quit":
                SmtpSessionFactory smtpSessionFactory = (SmtpSessionFactory)BeanUtils.getBean("smtpSessionFactory");
                smtpSessionFactory.close(smtpSession.getChannel());
                bBadSequence = false;
                break;
            default:
                bBadSequence = false;
                sendMessage("500 COMMAND UNRECOGNIZED\r\n");
                break;
        }

        //순서에 맞지 않은 커맨드가 왔을 떄 처리
        if (bBadSequence) {
            sendMessage("503 Bad sequence of commands\r\n");
        }
    }




    private void sendMessage(String s) {
        smtpSession.sendMessage(s);
    }

    private boolean ehlo(){
        String strTemp = smtpSession.getSmtpData();
        if ( strTemp.toLowerCase().startsWith("ehlo ") ){

            String strResponse = "250-"+smtpConfig.getServerName()+" helo ["+smtpSession.getRemoteIP()+ "] pleased to meet you\r\n";
            strResponse = "250-"+smtpConfig.getServerName()+" helo ["+smtpSession.getRemoteIP()+ "] pleased to meet you\r\n";
            strResponse +="250-SIZE " + (Long.parseLong(smtpConfig.getSmtpMaxSize())) + "\r\n";
            strResponse += checkStartTLS();
            strResponse +="250 AUTH LOGIN\r\n";
            smtpSession.setSmtpState(SmtpCommand.EHLO);

            sendMessage(strResponse);
            return true;
        }
        //여기는 실패 코드 이 외에 넣으면 안됨
        sendMessage("503 Bad sequence of commands\r\n");
        return false;
    }

    private boolean mailFrom() {
        Pattern cmdPatternMailFrom = Pattern.compile("MAIL[\\s]FROM[\\s]*?:[\\s]*?<(\\p{Graph}*=?=\\p{Graph}*=?=)?([=_\\-a-zA-Z0-9/+&.'`]*@([_\\-a-zA-Z0-9.]*\\.[a-zA-Z0-9]*))?>([\\s]*SIZE[\\s]*?=[\\s]*?([0-9]*))?[\\s]?(AUTH=<?([_\\-a-zA-Z0-9.]*@([_\\-a-zA-Z0-9.]*\\.[a-zA-Z0-9]*))?>?)?",Pattern.CASE_INSENSITIVE);
        Matcher m = null;
        if ( (m = cmdPatternMailFrom.matcher(smtpSession.getSmtpData())).matches() ) {

            String strMailFrom = m.group(2);
            //인증 후 인증id값과 mail from 값은 일치 해야 한다.
            if (smtpSession.getSmtpState().equals("auth") ){
                if ( !strMailFrom.equals(strAuthID) ){
                    sendMessage("553 5.3.0 Auth failure.\r\n");
                    return false;
                }
            }

            if (null == strMailFrom){
                strMailFrom="";
            }

            sendMessage("250 OK <" +strMailFrom +"> Sender ok\r\n");
            smtpSession.setSmtpState(SmtpCommand.MAIL);
            return true;
        }
        //여기는 실패 코드 이 외에 넣으면 안됨
        sendMessage("503 Bad sequence of commands\r\n");
        return false;
    }

    private boolean rcptTo() {
        Pattern cmdPatternRcptTo = Pattern.compile("RCPT[\\s]+TO[\\s]*?:[\\s]*?<([_\\-a-zA-Z0-9/+&.'`]*@([_\\-a-zA-Z0-9.]*\\.[a-zA-Z0-9]*))(<?[\\u0000-\\uFFFF]*>?)>[\\s]*",Pattern.CASE_INSENSITIVE);
        Matcher m = null;
        if ( (m = cmdPatternRcptTo.matcher(smtpSession.getSmtpData())).matches() ) {

            smtpSession.setSmtpState(SmtpCommand.RCPT);
            return true;
        }
        //여기는 실패 코드 이 외에 넣으면 안됨
        sendMessage("503 Bad sequence of commands\r\n");
        return false;
    }

    private String checkStartTLS() {
        return "";
    }
}

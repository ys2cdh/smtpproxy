package com.funnysalt.smtpproxy;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;


@SpringBootTest()
class SmtpproxyApplicationTests {

	private Socket socket = null;
	private OutputStream out = null;
	private InputStream in = null;

	private static Socket openSocket(String server, int port, int time) throws Exception {
		// socket 생성
		try {

			InetAddress inetAddress = InetAddress.getByName(server);
			SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);

			Socket socket = new Socket();
			socket.connect(socketAddress, 1000 * time);

			return socket;
		} catch (SocketTimeoutException ste) {

			System.err.println("Timed out waiting for the socket");
			ste.printStackTrace();
			throw ste;
		}
	}

	private String recvData(Socket socket, InputStream in) {
		String strReturn = "";
		byte[] byData = new byte[2048];
		boolean bloop = true;
		try {
			while (bloop) {
				int nRecv = in.read(byData);
				if (-1 == nRecv) {
					return "";
				}
				strReturn += new String(byData, 0, nRecv);

				if (false == strReturn.endsWith("\r\n"))
				{
					continue;
				}
				String[] strChecks = strReturn.split("\r\n");
				for (int i = 0; i < strChecks.length; i++) {
					if (3 != strChecks[i].indexOf("-")) {
						bloop = false;
						break;
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assertions.assertThat("0").isEqualTo("1");
		}
		System.out.println("S:" + strReturn);
		return strReturn;

	}

	private void send(OutputStream out,String strTemp){
		try {

			out.write(strTemp.getBytes());
			out.flush();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public void testConnect(Socket socket,InputStream in,OutputStream out)
	{
		try {

			String strTemp = new String("EHLO\r\n");
			out.write(strTemp.getBytes());
			out.flush();

			recvData(socket,in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	@BeforeEach
	void openSocket(){

		try {
			socket = openSocket("127.0.0.1",8025,60);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assertions.assertThat("1").isEqualTo("0");
		}

		try {

			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	@AfterEach
	void closeSocket(){
		try{if (null != out)out.close();}catch (Exception e){}
		try{if (null != in)in.close();}catch (Exception e){}
		try{if (null != socket)socket.close();}catch (Exception e){}
	}

	@Test
	void connectTest(){
		Assertions.assertThat(recvData(socket,in)).isEqualTo("220 test.com ESMTP Ready\r\n");
	}

	void ehloTest() {
		recvData(socket,in);
		String strTemp = new String("EHLO11111\r\n");

		try {
			out.write(strTemp.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assertions.assertThat(recvData(socket,in)).isEqualTo("500 COMMAND UNRECOGNIZED\r\n");

		strTemp = new String("EHLO \r\n");

		try {
			out.write(strTemp.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assertions.assertThat(recvData(socket,in)).isEqualTo("250-test.com helo [127.0.0.1] pleased to meet you\r\n" +
				"250-SIZE 99000000\r\n" +
				"250 AUTH LOGIN\r\n");

	}

	@Test
	void mailFrom() {

		ehloTest();

		String strTemp = new String("mail f\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("503 Bad sequence of commands\r\n");

		strTemp = new String("mail from:<aaa@s>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("503 Bad sequence of commands\r\n");

		strTemp = new String("mail from:<aaa.s.com>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("503 Bad sequence of commands\r\n");
		strTemp = new String("mail from:aaa@s.com\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("503 Bad sequence of commands\r\n");


		strTemp = new String("mail from:<aaa@s.com>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

		strTemp = new String("mail from:<>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <> Sender ok\r\n");

		strTemp = new String("mail from:<0xLHB1E.8B.9+C=EB1D4z/ARH@adrich.co.kr>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <0xLHB1E.8B.9+C=EB1D4z/ARH@adrich.co.kr> Sender ok\r\n");

		strTemp = new String("mail from:<> SIZE = 1234\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <> Sender ok\r\n");

		strTemp = new String("mail from:<aaa@s.com> SIZE = 1234\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

		strTemp = new String("mail from:<aaa@s.com> SIZE = 1234 AUTH=<>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

		strTemp = new String("mail from:<aaa@s.com> SIZE = 1234 AUTH=\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

		strTemp = new String("mail from:<aaa@s.com> SIZE = 1234 AUTH=<aaa@s.com>\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

		strTemp = new String("mail from:<aaa@s.com> SIZE = 1234 AUTH=aaa@s.com\r\n");
		send(out,strTemp);
		Assertions.assertThat(recvData(socket,in)).isEqualTo("250 OK <aaa@s.com> Sender ok\r\n");

	}

	@Test
	void contextLoads() {


		Assertions.assertThat("0").isEqualTo("0");
	}

}

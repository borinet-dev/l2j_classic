package org.l2jmobius.gameserver.util;

import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Player;

public class SendEmailDonate
{
	private static final String SMTP = Config.DONATE_EMAIL_SMTP;
	private static final int PORT = Config.DONATE_EMAIL_PORT;
	private static final boolean TLS = Config.DONATE_EMAIL_START_TLS;
	private static final String MAIL_ID = Config.DONATE_EMAIL_ADDRESS;
	private static final String MAIL_PASS = Config.DONATE_EMAIL_PASSWORD;
	private static final String RECEIVER = Config.DONATE_EMAIL_RECEIVER;
	
	static
	{
		// JavaMail 라이브러리의 로그 레벨 설정
		Logger logger = Logger.getLogger("javax.mail");
		logger.setLevel(Level.WARNING); // 원하는 로깅 레벨로 설정 (예: WARNING, SEVERE, OFF)
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.WARNING); // 원하는 로깅 레벨로 설정 (예: WARNING, SEVERE, OFF)
		logger.addHandler(handler);
	}
	
	public static void sendMail(Player player, String buyerName, String priceMoney, String itemList)
	{
		new Thread(() ->
		{
			Properties props = new Properties();
			props.put("mail.debug", "false");
			props.put("mail.smtp.host", SMTP);
			props.put("mail.smtp.port", PORT);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", TLS);
			
			Authenticator auth = new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(MAIL_ID, MAIL_PASS);
				}
			};
			
			Session session = Session.getInstance(props, auth);
			try
			{
				MimeMessage message = new MimeMessage(session);
				String fromMail = RECEIVER;
				String title = "후원 - " + player.getName();
				String content = String.format("<h2 style='color:blue'>입금자명: %s<br>입금액: %s<br>구매 아이템: %s</h2>", buyerName, priceMoney, itemList);
				
				InternetAddress fromAddress = new InternetAddress(fromMail, MimeUtility.encodeText(Config.SERVER_NAME_KOR, "UTF-8", "B"));
				
				message.setFrom(fromAddress);
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECEIVER));
				message.setSubject(title);
				message.setContent(content, "text/html;charset=UTF-8");
				message.setSentDate(new java.util.Date());
				
				Transport.send(message);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}
}

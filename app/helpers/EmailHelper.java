package helpers;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import play.Logger;
import play.Play;

public class EmailHelper {

	public static void sendSimpleEmail(String subject, String to, String msg) {
		Email email = new SimpleEmail();
		try {
			email.setHostName("smtp.gmail.com");
			email.setAuthenticator(new DefaultAuthenticator(Play.application()
					.configuration().getString("mail.address"), Play.application()
					.configuration().getString("mail.password")));
			email.setSSLOnConnect(true);
			email.setDebug(true);
			email.setSmtpPort(465);
			
			email.setFrom("contact.mymoz@gmail.com");
			email.setSubject(subject);
			email.setMsg(msg);
			email.addTo(to);
			email.send();
		} catch (EmailException e) {
			Logger.error(e.getMessage());
		}
	}

}

/**
 * Created on 2008-5-26 上午12:38:31
 */
package cn.net.openid.jos.service;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import cn.net.openid.jos.domain.EmailConfirmationInfo;

/**
 * @author Sutra Zhou
 * 
 */
public class EmailConfirmationInfoSendTaskExecutor {
	private class EmailConfirmationInfoSendTask implements Runnable {
		private EmailConfirmationInfo emailConfirmationInfo;

		public EmailConfirmationInfoSendTask(
				EmailConfirmationInfo emailConfirmationInfo) {
			this.emailConfirmationInfo = emailConfirmationInfo;
		}

		public void run() {
			log.debug("Sending mail to: "
					+ this.emailConfirmationInfo.getEmail().getAddress());
			SimpleMailMessage simpleMessage = new SimpleMailMessage();
			simpleMessage.setTo(this.emailConfirmationInfo.getEmail()
					.getAddress());
			simpleMessage.setSubject(emailConfirmationProperties
					.getProperty("subject"));
			String text = emailConfirmationProperties.getProperty("text");
			text = StringUtils.replace(text, "${identifier}",
					emailConfirmationInfo.getEmail().getUser().getUsername());
			text = StringUtils.replace(text, "${confirmationCode}",
					emailConfirmationInfo.getConfirmationCode());
			simpleMessage.setText(text);
			mailSender.send(simpleMessage);
		}

	}

	private static final Log log = LogFactory
			.getLog(EmailConfirmationInfoSendTaskExecutor.class);

	private TaskExecutor taskExecutor;
	private MailSender mailSender;
	private Properties emailConfirmationProperties;

	public EmailConfirmationInfoSendTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		emailConfirmationProperties = new Properties();
		try {
			emailConfirmationProperties.loadFromXML(this.getClass()
					.getResourceAsStream("/email-confirmation.xml"));
		} catch (InvalidPropertiesFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param mailSender
	 *            the mailSender to set
	 */
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendEmail(EmailConfirmationInfo emailConfirmationInfo) {
		taskExecutor.execute(new EmailConfirmationInfoSendTask(
				emailConfirmationInfo));
	}

}
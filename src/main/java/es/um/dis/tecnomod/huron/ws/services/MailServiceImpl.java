package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService{
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Override
	public void send(String to, String subject, String body, File... attachments) {
		MimeMessagePreparator messagePreparator = new MimeMessagePreparator() {  
			public void prepare(MimeMessage mimeMessage) throws Exception {  
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setTo(to);
				message.setSubject(subject);
				message.setText(body, true);
				for (File attachment :attachments) {
					message.addAttachment(attachment.getName(), attachment);
				}
			}  
		}; 
		mailSender.send(messagePreparator);
	}

}

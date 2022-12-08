package es.um.dis.ontology_metrics_ws;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class ApplicationConfig {
	@Bean
	public JavaMailSender mailSender() {
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.transport.protocol", "smtp");
		javaMailProperties.put("mail.smtp.auth", "true");
		javaMailProperties.put("mail.smtp.starttls.enable", "true");
		javaMailProperties.put("mail.debug", "true");
		
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(25);
		mailSender.setUsername("human.readable.ontology.metrics@gmail.com");
		mailSender.setPassword("");
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}
}

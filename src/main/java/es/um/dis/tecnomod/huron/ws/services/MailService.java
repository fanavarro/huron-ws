package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;

/**
 * The Interface MailService.
 */
public interface MailService {
	
	/**
	 * Send.
	 *
	 * @param to the to
	 * @param subject the subject
	 * @param body the body
	 * @param attachments the attachments
	 */
	void send(String to, String subject, String body, File... attachments);
}

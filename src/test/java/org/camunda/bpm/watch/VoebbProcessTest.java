package org.camunda.bpm.watch;

import static org.assertj.core.api.Assertions.assertThat;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.extension.mail.MailContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestConfig.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class VoebbProcessTest {

	@Autowired
	private ManagementService managementService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private GreenMail greenMail;
	
	@Test
	public void help() throws Exception {

		sendMessage("voebb help");
		
		greenMail.waitForIncomingEmail(2);
		
		waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(2);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb help");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb help");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Du kannst deine VOEBB Merkliste via Mail mit folgendem Betreff organisieren");
	}
	
	@Test
	public void startWatching() throws Exception {

		sendMessage("voebb watch 978-3-95590-020-5");
		
		// check borrow state > not available
		
		waitForTimerJob();
		
		// check status of print job
		Job timerJob = managementService.createJobQuery().timers().activityId("waitForCheckBorrowState").singleResult();
		managementService.executeJob(timerJob.getId());
				
		// check borrow state > available 
		
		// send mail
		
		// waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(2);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("veobb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Dein Buch 'Essen für Sieger' kann jetzt ausgeliehen werden.");
	}
	
	@Test
	public void stopWatching() throws Exception {

		sendMessage("voebb watch 978-3-95590-020-5");
		
		// check borrow state > not available
		
		waitForTimerJob();
		
		// stop watching
		
		sendMessage("voebb delete 978-3-95590-020-5");
		
		// send mail
		
		greenMail.waitForIncomingEmail(3);
		
		waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(3);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("voebb delete 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[2];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Buch von Merkliste entfernt.");
	}
	
	@Test
	public void startWatchingNoResult() throws Exception {

		sendMessage("voebb watch 978-3-95590-020-6");
		
		greenMail.waitForIncomingEmail(2);
		
		// check borrow state > not result
		
		// send mail
		
		waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(2);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-6");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-6");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Kein Eintrag gefunden. Bitte einen anderen Suchtext verwenden.");
	}
	
	@Test
	public void startWatchingMultipleResult() throws Exception {

		sendMessage("voebb watch Essen für Sieger");
		
		greenMail.waitForIncomingEmail(2);
		
		// check borrow state > multiple results
		
		// send mail
		
		waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(2);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch Essen für Sieger");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch Essen für Sieger");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Bitte eindeutigen Suchtext verwenden.");
	}
	
	private void sendMessage(String subject) throws MessagingException, AddressException {
		Session smtpSession = greenMail.getSmtp().createSession();
		MimeMessage message = createMimeMessage(smtpSession, subject);
		message.setContent("", MailContentType.TEXT_PLAIN.getType());
		
		GreenMailUtil.sendMimeMessage(message);
	}
	
	private MimeMessage createMimeMessage(Session session, String subject) throws MessagingException, AddressException {
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress("from@camunda.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@camunda.com"));
		message.setSubject(subject);

		return message;
	}
	
	private void waitForProcessEnd() throws Exception {
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		while(query.count() > 0) {
			Thread.sleep(500);
		}
		
	}
	
	private void waitForTimerJob() throws Exception {
		JobQuery query = managementService.createJobQuery().timers();
		while(query.count() == 0) {
			Thread.sleep(500);
		}
	}
	
}

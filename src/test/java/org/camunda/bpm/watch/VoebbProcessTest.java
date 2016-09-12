package org.camunda.bpm.watch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.watch.voebb.BorrorState;
import org.camunda.bpm.watch.voebb.MultipleResultsFoundException;
import org.camunda.bpm.watch.voebb.NoResultFoundException;
import org.camunda.bpm.watch.voebb.VoebbService;
import org.junit.After;
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
	
	@Autowired
	private VoebbService voebbService;
	
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

		BorrorState borrorState = new BorrorState("Essen für Sieger", 
				"Spandau: Hauptbibliothek Spandau", 
				"HW 680 Essen", 
				"Ausgeliehen Fällig am 15.09.2016", 
				"Freihand", 
				"bestellbar");
		
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);

		// check borrow state > not available
		
		sendMessage("voebb watch 978-3-95590-020-5");
		
		// check borrow state > available 
		waitForTimerJob();

		borrorState = new BorrorState("Essen für Sieger", 
				"Spandau: Hauptbibliothek Spandau", 
				"HW 680 Essen", 
				"Verfügbar", 
				"Freihand", 
				"bestellbar");
		
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);
		
		Job timerJob = managementService.createJobQuery().timers().activityId("waitForCheckBorrowState").singleResult();
		managementService.executeJob(timerJob.getId());
				
		// send mail
		greenMail.waitForIncomingEmail(3);
		
		waitForTimerJob();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(3);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch wurde der Merkliste");
	    
	    mail = mails[2];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch", "kann jetzt ausgeliehen werden.");
	}
	
	@Test
	public void borrowWhileWaiting() throws Exception {

		BorrorState borrorState = new BorrorState("Essen für Sieger", 
				"Spandau: Hauptbibliothek Spandau", 
				"HW 680 Essen", 
				"Verfügbar", 
				"Freihand", 
				"bestellbar");
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);

		// check borrow state > available
		
		sendMessage("voebb watch 978-3-95590-020-5");
		
		// check borrow state > available 
		waitForTimerJob();
		
		Job timerJob = managementService.createJobQuery().timers().activityId("waitForCheckBorrorStateAgain").singleResult();
		managementService.executeJob(timerJob.getId());
		
		// check borrow state > not available 
		waitForTimerJob();

		borrorState = new BorrorState("Essen für Sieger", 
				"Spandau: Hauptbibliothek Spandau", 
				"HW 680 Essen", 
				"Ausgeliehen Fällig am 15.09.2016", 
				"Freihand", 
				"bestellbar");
		
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);
		
		timerJob = managementService.createJobQuery().timers().activityId("waitForCheckBorrorStateAgain").singleResult();
		managementService.executeJob(timerJob.getId());
				
		// send mail
		greenMail.waitForIncomingEmail(4);
		
		// check borrow state again
		waitForTimerJob();
		
		timerJob = managementService.createJobQuery().timers().activityId("waitForCheckBorrowState").singleResult();
		assertThat(timerJob).isNotNull();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(4);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch wurde der Merkliste");
	    
	    mail = mails[2];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch", "kann jetzt ausgeliehen werden.");
	    
	    mail = mails[3];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch ist leider nicht mehr");
	}
	
	@Test
	public void stopWatching() throws Exception {
		
		BorrorState borrorState = mock(BorrorState.class);
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);

		// check borrow state > not available
		when(borrorState.isAvailableForBorrow()).thenReturn(false);

		sendMessage("voebb watch 978-3-95590-020-5");
		
		waitForTimerJob();
		
		// stop watching
		
		sendMessage("voebb delete 978-3-95590-020-5");
		
		// send mail
		
		greenMail.waitForIncomingEmail(4);
		
		waitForProcessEnd();
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(4);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	   
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("voebb delete 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[2];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch wurde der Merkliste");
	    
	    mail = mails[3];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Buch von Merkliste entfernt.");
	}
	
	@Test
	public void startWatchingNoResult() throws Exception {
		
		// check borrow state > not result
		when(voebbService.checkBorrorState(anyString(), anyString()))
			.thenThrow(new NoResultFoundException("978-3-95590-020-6", "lib"));

		sendMessage("voebb watch 978-3-95590-020-6");
		
		greenMail.waitForIncomingEmail(2);
		
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
		
		// check borrow state > multiple results
		when(voebbService.checkBorrorState(anyString(), anyString()))
			.thenThrow(new MultipleResultsFoundException("978-3-95590-020-6", "lib"));

		sendMessage("voebb watch Essen für Sieger");
		
		greenMail.waitForIncomingEmail(2);
		
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
	
	@Test
	public void watchList() throws Exception {

		BorrorState borrorState = mock(BorrorState.class);
		when(voebbService.checkBorrorState(anyString(), anyString())).thenReturn(borrorState);

		// check borrow state > not available
		when(borrorState.isAvailableForBorrow()).thenReturn(false);

		sendMessage("voebb watch 978-3-95590-020-5");
		
		waitForTimerJob();
		
		sendMessage("voebb list");
		
		greenMail.waitForIncomingEmail(4);
		
		// wait for process end
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("mailDispatching");
		while(query.count() > 1) {
			Thread.sleep(500);
		}
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(4);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("voebb watch 978-3-95590-020-5");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("voebb list");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[2];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb watch 978-3-95590-020-5");
	    assertThat(GreenMailUtil.getBody(mail)).contains("Dein Buch wurde der Merkliste");
	    
	    mail = mails[3];
	    assertThat(mail.getSubject()).isEqualTo("RE: voebb list");
	    assertThat(GreenMailUtil.getBody(mail)).startsWith("Merkliste: [Item [text=978-3-95590-020-5");
	}
	
	@After
	public void cleanUp() {
		for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().processDefinitionKey("mailDispatching").list()) {
			runtimeService.deleteProcessInstance(processInstance.getId(), "test ends");
		}
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

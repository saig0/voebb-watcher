package org.camunda.bpm.watch.task;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.commons.utils.EnsureUtil;
import org.springframework.stereotype.Component;

@Component
public class ExtractSearchTextTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		String subjectPrefix = (String) execution.getVariable("subject");
		Mail mail = (Mail) execution.getVariable("mail");

		EnsureUtil.ensureNotNull("subject prefix", subjectPrefix);
		EnsureUtil.ensureNotNull("mail", mail);
		
		String subject = mail.getSubject();
		
		if (subject.startsWith(subjectPrefix)) {
			String text = subject.substring(subjectPrefix.length()).trim();
			
			execution.setVariable("searchText", text);
			
		} else {
			throw new IllegalStateException("expect mail subject starts with: " + subjectPrefix);
		}
	}

}

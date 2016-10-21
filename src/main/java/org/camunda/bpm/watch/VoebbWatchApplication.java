package org.camunda.bpm.watch;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.spring.application.SpringServletProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("voebb-watch-application")
@ProcessApplication
public class VoebbWatchApplication extends SpringServletProcessApplication {

	@Autowired
	private AppStarter starter;

	@PostDeploy
	public void connectToMailServer() throws Exception {
		starter.connectToMailServer();
	}

	@PreUndeploy
	public void closeConnection() throws Exception {
		starter.closeConnection();
	}

}

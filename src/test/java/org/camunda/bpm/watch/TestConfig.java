package org.camunda.bpm.watch;

import org.camunda.bpm.watch.voebb.VoebbService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@Configuration
@EnableAutoConfiguration
@ComponentScan( excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.camunda.bpm.watch.Application"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.camunda.bpm.watch.voebb.SeleniumVoebbService")})
public class TestConfig {

	@Bean(destroyMethod = "stop")
	public GreenMail greenMail() {
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
				
		greenMail.setUser("test@camunda.com", "bpmn");
		
		greenMail.start();
		
		return greenMail;
	}
	
	@Bean
	@DependsOn("greenMail")
	public Application application() {
		return new Application();
	}
	
	@Bean
	public VoebbService printerService() {
		return Mockito.mock(VoebbService.class);
	}
	
}

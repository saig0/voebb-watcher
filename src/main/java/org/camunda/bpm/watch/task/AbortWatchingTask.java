package org.camunda.bpm.watch.task;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.commons.utils.EnsureUtil;
import org.springframework.stereotype.Component;

@Component
public class AbortWatchingTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String searchText = (String) execution.getVariable("searchText");
		EnsureUtil.ensureNotNull("searchText", searchText);
		
		RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
		
		runtimeService
			.createMessageCorrelation("abort-watching")
			.processInstanceVariableEquals("searchText", searchText)
			.correlateAll();
	}

}

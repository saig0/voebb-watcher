package org.camunda.bpm.watch.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

@Component
public class WatchListTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

		List<WatchListItem> watchList = new ArrayList<>();
		
		List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
				.processDefinitionKey("watchProcess").list();
		
		for (ProcessInstance processInstance : processInstances) {
			
			String searchText = (String) runtimeService.getVariable(processInstance.getId(), "searchText");
			Boolean isAvailableForBorror = (Boolean) runtimeService.getVariable(processInstance.getId(), "isAvailableForBorror");
			Date lastCheckTime = (Date) runtimeService.getVariable(processInstance.getId(), "lastCheckTime");
			
			WatchListItem watchListItem = new WatchListItem(searchText, isAvailableForBorror, lastCheckTime);
			watchList.add(watchListItem);
		}
		
		execution.setVariable("watchList", watchList);
	}

}

package org.camunda.bpm.watch.task;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.watch.voebb.BorrorState;
import org.camunda.bpm.watch.voebb.MultipleResultsFoundException;
import org.camunda.bpm.watch.voebb.NoResultFoundException;
import org.camunda.bpm.watch.voebb.VoebbService;
import org.camunda.commons.utils.EnsureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckBorrorStatusTask implements JavaDelegate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckBorrorStatusTask.class);

	@Value("${libraries}")
	private List<String> libraries;
	
	@Autowired
	private VoebbService voebbService;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {

		String searchText = (String) execution.getVariable("searchText");

		EnsureUtil.ensureNotNull("libraries", libraries);
		EnsureUtil.ensureNotNull("search text", searchText);
		
		List<String> lookupLibraries = libraries;
		
		Boolean previousBorrorStateAvailable = (Boolean) execution.getVariable("isAvailableForBorror");
		if (previousBorrorStateAvailable != null && previousBorrorStateAvailable) {
			BorrorState previousBorrorState = (BorrorState) execution.getVariable("state");
			EnsureUtil.ensureNotNull("previousBorrorState", previousBorrorState);
			
			lookupLibraries = Collections.singletonList(previousBorrorState.getLibrary());
		}
		
		LOGGER.debug("check borror states for '{}' at: {}", searchText, lookupLibraries);
		
		try {		
			Optional<BorrorState> borrorState = lookupLibraries.stream()
				.map(library -> voebbService.checkBorrorState(searchText, library))
				.filter(state -> state.isAvailableForBorrow())
				.findFirst();
			
			if (borrorState.isPresent()) {
				execution.setVariable("isAvailableForBorror", true);
				execution.setVariable("state", borrorState.get());
				
				LOGGER.debug("found available item '{}'", borrorState.get());
			} else {
				execution.setVariable("isAvailableForBorror", false);
				
				LOGGER.debug("found no available item for '{}'", searchText);
			}
		
		} catch (NoResultFoundException e) {
			LOGGER.warn("found no item for text '{}'", searchText);			
			throw new BpmnError("NO_RESULT");
		
		} catch (MultipleResultsFoundException e) {
			LOGGER.warn("found multiple items for text '{}'", searchText);			
			throw new BpmnError("MULTIPLE_RESULTS");
		
		} catch (Exception e) {
			LOGGER.error("failed to check borror state", e);
			throw e;
		}
	}

}

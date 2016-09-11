package org.camunda.bpm.watch.voebb;

import org.springframework.stereotype.Component;

@Component
public class SeleniumVoebbService implements VoebbService {
	
	@Override
	public BorrorState checkBorrorState(String text, String library)
			throws NoResultFoundException, MultipleResultsFoundException {
		return new SeleniumBorrorStateChecker().check(text, library);
	}

}

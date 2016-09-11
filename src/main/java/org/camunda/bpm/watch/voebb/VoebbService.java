package org.camunda.bpm.watch.voebb;

public interface VoebbService {
	
	BorrorState checkBorrorState(String text, String library) throws NoResultFoundException, MultipleResultsFoundException;

}

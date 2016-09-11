package org.camunda.bpm.watch.voebb;

public class MultipleResultsFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MultipleResultsFoundException(String text, String library) {
		super("found multiple results for '" + text + "' at '" + library + "'");
	}
	
}

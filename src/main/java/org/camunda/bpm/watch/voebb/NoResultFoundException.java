package org.camunda.bpm.watch.voebb;

public class NoResultFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoResultFoundException(String text, String library) {
		super("found no result for '" + text + "' at '" + library + "'");
	}
	
}

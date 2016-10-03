package org.camunda.bpm.watch.voebb;

import java.io.Serializable;

public class BorrorState implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String STATE_NO_ITEM_FOUND = "no item found";
	public static String STATE_MULTIPLE_ITEMS_FOUND = "multiple items found";
	
	private final String title;
	private final String library;
	private final String signature;
	private final String state;
	private final String location;
	private final String note;

	private BorrorState(String library, String state) {
		this(null, library, null, state, null, null);
	}
	
	public BorrorState(String title, String library, String signature, String state, String location, String note) {
		this.title = title;
		this.library = library;
		this.signature = signature;
		this.state = state;
		this.location = location;
		this.note = note;
	}

	public String getState() {
		return state;
	}

	public String getLocation() {
		return location;
	}

	public String getNote() {
		return note;
	}

	public boolean isAvailableForBorrow() {
		return state.contains("Verfügbar");
	}

	public String getTitle() {
		return title;
	}

	public String getSignature() {
		return signature;
	}

	public String getLibrary() {
		return library;
	}

	@Override
	public String toString() {
		return "BorrorState [title=" + title + ", library=" + library + ", signature=" + signature + ", state="
				+ state + ", location=" + location + ", note=" + note + "]";
	}

	public boolean isValid() {
		return !state.equals(STATE_NO_ITEM_FOUND) && !state.equals(STATE_MULTIPLE_ITEMS_FOUND);
	}
	
	public static BorrorState notItemFound(String library) {
		return new BorrorState(library, STATE_NO_ITEM_FOUND);
	}
	
	public static BorrorState multipleItemsFound(String library) {
		return new BorrorState(library, STATE_MULTIPLE_ITEMS_FOUND);
	}
	
}

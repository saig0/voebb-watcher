package de.voebb;

import java.io.Serializable;

public class BorrorStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String status;
    private final String location;
    private final String note;
	
    public BorrorStatus(String status, String location, String note) {
		this.status = status;
		this.location = location;
		this.note = note;
	}

	public String getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}

	public String getNote() {
		return note;
	}

	public boolean isAvailableForBorrow() {
		return status.contains("Verfügbar");
	}
	
	@Override
	public String toString() {
		return "BorrorStatus [status=" + status + ", location=" + location + ", note=" + note + "]";
	}
	
}

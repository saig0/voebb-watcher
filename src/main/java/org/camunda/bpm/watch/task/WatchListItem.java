package org.camunda.bpm.watch.task;

import java.io.Serializable;
import java.util.Date;

public class WatchListItem implements Serializable {

	private static final long serialVersionUID = 1L;

	final String searchText;
	final Boolean isAvailableForBorror;
	final Date lastCheckTime;

	public WatchListItem(String searchText, Boolean isAvailableForBorror, Date lastCheckTime) {
		this.searchText = searchText;
		this.isAvailableForBorror = isAvailableForBorror;
		this.lastCheckTime = lastCheckTime;
	}

	public String getSearchText() {
		return searchText;
	}

	public Boolean getIsAvailableForBorror() {
		return isAvailableForBorror;
	}

	public Date getLastCheckTime() {
		return lastCheckTime;
	}

	@Override
	public String toString() {
		return "Item [text=" + searchText + ", available=" + isAvailableForBorror
				+ ", last check=" + lastCheckTime + "]";
	}

}

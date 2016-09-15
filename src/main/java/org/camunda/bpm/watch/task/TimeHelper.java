package org.camunda.bpm.watch.task;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.springframework.stereotype.Component;

@Component
public class TimeHelper {

	public String nextDateAt(String time) {

		LocalTime localTime = LocalTime.parse(time);

		Instant currentInstance = ClockUtil.getCurrentTime().toInstant();
		ZonedDateTime currentDateTime = currentInstance.atZone(ZoneId.systemDefault());

		ZonedDateTime dateTimeAtHour = currentDateTime
				.withHour(localTime.getHour())
				.withMinute(localTime.getMinute());

		if (currentDateTime.isAfter(dateTimeAtHour) || currentDateTime.equals(dateTimeAtHour)) {
			dateTimeAtHour = dateTimeAtHour.plusDays(1);
		}

		return dateTimeAtHour.toLocalDateTime().toString();
	}

}

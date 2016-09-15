package org.camunda.bpm.watch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.watch.task.TimeHelper;
import org.junit.Test;

public class TimeHelperTest {

	@Test
	public void nextDateAt_before() {
		LocalDateTime currentTime = LocalDateTime.parse("2016-09-11T11:00:00");
		Instant currentInstance = currentTime.atZone(ZoneId.systemDefault()).toInstant();
		ClockUtil.setCurrentTime(new Date(currentInstance.toEpochMilli()));

		TimeHelper helper = new TimeHelper();

		assertThat(helper.nextDateAt("14:00")).isEqualTo("2016-09-11T14:00");
	}
	
	@Test
	public void nextDateAt_after() {
		LocalDateTime currentTime = LocalDateTime.parse("2016-09-11T15:00:00");
		Instant currentInstance = currentTime.atZone(ZoneId.systemDefault()).toInstant();
		ClockUtil.setCurrentTime(new Date(currentInstance.toEpochMilli()));

		TimeHelper helper = new TimeHelper();

		assertThat(helper.nextDateAt("14:00")).isEqualTo("2016-09-12T14:00");
	}
	
	@Test
	public void nextDateAt_equal() {
		LocalDateTime currentTime = LocalDateTime.parse("2016-09-11T14:00:00");
		Instant currentInstance = currentTime.atZone(ZoneId.systemDefault()).toInstant();
		ClockUtil.setCurrentTime(new Date(currentInstance.toEpochMilli()));

		TimeHelper helper = new TimeHelper();

		assertThat(helper.nextDateAt("14:00")).isEqualTo("2016-09-12T14:00");
	}

	@Test
	public void nextDateAt_withMinutes() {
		LocalDateTime currentTime = LocalDateTime.parse("2016-09-11T07:00:00");
		Instant currentInstance = currentTime.atZone(ZoneId.systemDefault()).toInstant();
		ClockUtil.setCurrentTime(new Date(currentInstance.toEpochMilli()));

		TimeHelper helper = new TimeHelper();

		assertThat(helper.nextDateAt("07:45")).isEqualTo("2016-09-11T07:45");
	}
	
}

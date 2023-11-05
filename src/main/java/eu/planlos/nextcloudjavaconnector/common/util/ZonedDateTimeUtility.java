package eu.planlos.nextcloudjavaconnector.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeUtility {

	private static final Logger LOG = LoggerFactory.getLogger(ZonedDateTimeUtility.class);
	
	public static final String CET = "Europe/Berlin";
	public static final String UTC = "UTC";
	public static final String FORMAT = "dd.MM.yyyy HH:mm:ss";
	public static final String UI_FORMAT = "dd.MM.yyyy HH:mm";

	public static String niceCET(Instant instant) {
		ZonedDateTime zdt = instant.atZone(ZoneId.of(CET));		
		return nice(zdt);
	}
	
	public static String nice(ZonedDateTime zdt) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT);
		String nice = zdt.format(formatter);
		LOG.debug("Nicing '{}' to '{}'", zdt, nice);
		return nice;
	}
	
	public static ZonedDateTime nowCET() {
		return ZonedDateTime.now(ZoneId.of(CET));
	}
	
	public static ZonedDateTime nowUTC() {
		return ZonedDateTime.now();
	}

	public static ZonedDateTime convertCET(Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.of(CET));
	}
}

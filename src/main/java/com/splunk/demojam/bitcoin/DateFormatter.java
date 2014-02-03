package com.splunk.demojam.bitcoin;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public final class DateFormatter {
	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
	
	public static final String format(Date date) {
		return formatter.print(new DateTime(date));
	}
}

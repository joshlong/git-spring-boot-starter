package generator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Each {@link DateFormat} instance is not thread safe so this class generates ones for
 * reuse.
 */
public abstract class DateUtils {

	public static Calendar getCalendarFor(Date d) {
		var c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}

	public static Calendar getCurrentCalendar() {
		return getCalendarFor(new Date());
	}

	public static DateFormat dateAndTime() {
		return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
	}

	public static DateFormat date() {
		return new SimpleDateFormat("MM/dd/yyyy");
	}

}

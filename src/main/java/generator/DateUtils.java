package generator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Each {@link DateFormat} instance is not thread safe so this class generates ones for
 * reuse.
 */
public abstract class DateUtils {

	public static DateFormat dateAndTime() {
		return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
	}

	public static DateFormat date() {
		return new SimpleDateFormat("MM/dd/yyyy");
	}

}

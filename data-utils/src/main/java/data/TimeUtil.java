package data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil implements Util {
	public enum TimeOfDay {
		MORNING, AFTERNOON, EVENING, NIGHT
	}

	private static final ThreadLocal<TimeUtil> instance = new ThreadLocal<TimeUtil>();
	private SimpleDateFormat formatter;

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static TimeUtil getInstance() {
		TimeUtil result = instance.get();

		if (null == result) {
			result = new TimeUtil();
			instance.set(result);
		}

		return result;
	}

	public long tick() {
		Date d = new Date();

		return d.getTime();
	}

	public long diff(long start) {
		return tick() - start;
	}

	public String difff(long start) {
		Date d = new Date(diff(start));

		SimpleDateFormat fmt = getFormatter();

		return fmt.format(d);
	}

	private SimpleDateFormat getFormatter() {
		if (null == formatter)
			formatter = new SimpleDateFormat("mm:sss:SSS");

		return formatter;

	}

	public String format(Date date, String format) {
		String result;
		SimpleDateFormat formatter = new SimpleDateFormat(format);

		result = formatter.format(date);

		return result;
	}

	public String formatNow(String format) {
		return format(new Date(), format);
	}

	public TimeOfDay getTimeOfDay() {
		TimeOfDay result;

		Calendar cal = Calendar.getInstance();

		int hours = cal.get(Calendar.HOUR_OF_DAY);

		if (hours >= 4 && hours < 12)
			result = TimeOfDay.MORNING;
		else if (hours >= 12 && hours < 18)
			result = TimeOfDay.AFTERNOON;
		else if (hours >= 18 && hours < 20)
			result = TimeOfDay.EVENING;
		else
			result = TimeOfDay.NIGHT;

		return result;
	}
}

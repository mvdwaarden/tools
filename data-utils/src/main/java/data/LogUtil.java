package data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class LogUtil implements Util {
	public static final String SETTING_LOG_DIR = "log.dir";
	
	public enum Option {
		NOP, OPTION_ECHO, FORMAT_LINE;
	}

	public enum LogAppend {
		YES, NO
	}

	private List<LogCallback> callbacks = new ArrayList<>();
	private static final ThreadLocal<LogUtil> instance = new ThreadLocal<LogUtil>();
	private OutputStream os;
	private Exception lastError;
	public void registerCallback(LogCallback cb) {
		if (!callbacks.contains(cb))
			callbacks.add(cb);

	}

	/**
	 * Clone the LogUtil to another thread, typically called before the thread
	 * is actually run.
	 * 
	 * @param util
	 */
	public void clone(LogUtil instance) {
		LogUtil.instance.set(instance);
	}

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static LogUtil getInstance() {
		LogUtil result = instance.get();

		if (null == result) {
			result = new LogUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * If there is problem with logging output to stderr!
	 * 
	 * @param text
	 * @param ex
	 */
	public void stderr(String text, Exception ex) {
		System.err.println(text + formatException(ex));
	}

	/**
	 * Handle errors
	 * 
	 * @param text
	 */
	public void error(String text) {
		error(text, null);
	}

	/**
	 * Handle errors
	 * 
	 * @param text
	 */
	public void error(String text, Exception e) {
		log("error", text, e);
	}

	/**
	 * Handle debug messages
	 * 
	 * @param text
	 */
	public void debug(String str, Exception e) {
		if (!callbacks.isEmpty()) {
			String text = createText("", "", str, e);
			for (LogCallback cb : callbacks)
				cb.handle("", "debug", text);
		}
		// if (null != os) {
		// info(text, e);
		// } else
		// System.err.println(text + formatException(e));
	}

	/**
	 * Handle debug messages
	 * 
	 * @param text
	 */
	public void debug(String text) {
		if (!callbacks.isEmpty()) {
			for (LogCallback cb : callbacks)
				cb.handle("", "debug", text.toString());
		} else
			System.err.println(text);
	}

	/* Cache */
	private SimpleDateFormat cacheDateFormat;

	private String getTimestamp(long currTime) {
		if (null == cacheDateFormat) {
			cacheDateFormat = new SimpleDateFormat("EEE, dd MMM HH:mm:ss-SSS");
		}
		return cacheDateFormat.format(new Date(currTime));
	}

	private String getThreadId() {
		long id = Thread.currentThread().getId();
		// so only 'prefix' with a '<' if the id > 10000 (id %10000) generated
		// max 4 characters => pad length = 5
		char padCharacter = (id < 10000) ? ' ' : '<';
		return pad(id % 10000, 3, padCharacter, true);
	}

	private String getLevel(String level) {
		return pad(level, 1, ' ', false).toUpperCase();
	}

	public void info(String str, Exception e) {
		log("info", str, e, Option.NOP);
	}
	
	public Exception getLastError(){		
		return this.lastError;
	}

	protected String createText(String context, String level, String text, Exception e) {
		StringBuilder out = new StringBuilder();
		Date date = new Date();
		long currTime = date.getTime();
		out.append("[" + getLevel(level) + "][" + getTimestamp(currTime) + "][" + getThreadId() + "] " + text
				+ formatException(e));
		date = null;
		return out.toString();
	}

	/**
	 * Format the exception
	 * 
	 * @param e
	 * @param options
	 *            [FORMAT_LINE]
	 * @return
	 */
	public String formatException(Exception e, Option... options) {
		boolean inline = EnumUtil.getInstance().contains(options, Option.FORMAT_LINE);
		String result = "";

		if (null != e) {
			StringBuilder err = new StringBuilder();
			if (!inline) {
				err.append("\nException in thread \"");
				err.append(Thread.currentThread().getName());
				err.append("\" " + e.getClass().getName() + ": ");
				err.append(e.getMessage());
			}
			for (StackTraceElement ste : e.getStackTrace()) {
				err.append(
						((inline) ? "" : "\n") + " in (" + ((null != ste.getFileName()) ? ste.getFileName() : "unknown")
								+ ":" + ste.getLineNumber() + ") " + ste.getClassName() + "." + ste.getMethodName());
			}
			result = err.toString();
		}

		return result;

	}

	/**
	 * Format the exception in one line (no carriage return and newlines)
	 * 
	 * @param e
	 * @return
	 */
	public String formatExceptionInline(Exception e) {
		return formatException(e, Option.FORMAT_LINE);
	}

	/**
	 * Info method : Print messages on screen
	 * 
	 * @param info
	 */
	public void info(String text) {
		log("info", text, null);
	}

	/**
	 * Define an output stream
	 * 
	 * @param filename
	 */
	public void setOutputStream(String filename) {
		setOutputStream(filename, LogAppend.NO);
	}

	/**
	 * Define an output stream
	 * 
	 * @param filename
	 */
	public void setOutputStream(String filename, LogAppend append) {
		try {
			DataUtil.getInstance().makeDirectories(filename);
			this.os = new FileOutputStream(filename, (append == LogAppend.YES));
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to set outputstream", e);
		}
	}

	/**
	 * Close the output stream
	 */
	public void closeOutputStream() {
		if (null != this.os) {
			try {
				os.close();
			} catch (IOException e) {
				LogUtil.getInstance().error(formatException(e));
			}
		}
	}

	public void warning(String msg) {
		log("warning", msg, null);
	}

	public void warning(String msg, Exception e) {
		log("warning", msg, e);
	}

	/**
	 * Log a message
	 * 
	 * @param name
	 * @param level
	 * @param text
	 * @param e
	 */
	public void log(String name, Level level, String text, Exception e) {
		log(level.getName(), text, e);
	}

	/**
	 * Info method : Print messages on screen
	 * 
	 * @param str
	 * @param e
	 * @param options
	 */
	private void log(String level, String str, Exception e, Option... options) {
		String text = createText("", level, str, e);
		this.lastError = e;
		if (!callbacks.isEmpty()) {
			for (LogCallback cb : callbacks)
				cb.handle("", "level", text);
		} else if (null != os) {
			try {
				os.write(text.getBytes());
			} catch (IOException ex) {
				stderr("problem writing to file", ex);
			}
			if (EnumUtil.getInstance().contains(options, Option.OPTION_ECHO))
				System.out.println(text);
		} else
			System.out.println(text);
	}

	public void echo(String str) {
		log("echo", str, null, Option.OPTION_ECHO);
	}

	public void severe(String text) {
		severe(text, null);
	}

	public void severe(String text, Exception e) {
		log("severe", text, e);
	}

	public void ignore(String string, Exception e) {
	}

	public String pad(long number, int length, char padCharacter, boolean before) {
		return pad("" + number, length, padCharacter, before);
	}

	public String pad(String str, int length, char padCharacter, boolean before) {
		String result = (str.length() > length) ? str.substring(0, length) : str;
		int pad = length - result.length();
		while (pad-- > 0)
			if (before)
				result = "" + padCharacter + result;
			else
				result += padCharacter;
		return result;
	}

}

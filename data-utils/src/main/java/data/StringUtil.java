package data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

public class StringUtil implements Util {
	private static final ThreadLocal<StringUtil> instance = new ThreadLocal<StringUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static StringUtil getInstance() {
		StringUtil result = instance.get();

		if (null == result) {
			result = new StringUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Extended matcher with option
	 * 
	 * @param str
	 * @param pattern
	 * @param flags
	 * @return
	 */
	public boolean matches(String str, String pattern, int... flags) {
		boolean result = false;
		int opts = 0;
		for (int flag : flags) {
			opts = opts | flag;
		}
		Pattern p = Pattern.compile(pattern, opts);

		Matcher m = p.matcher(str);

		result = m.matches();

		return result;
	}

	/**
	 * Splits a delimited line into parts taking nested separators into account
	 * 
	 * @param line
	 * @param separator
	 * @param begin
	 * @param end
	 * @return
	 */
	public String[] splitNested(String line, String separator, String begin, String end) {
		String[] result = new String[0];
		List<String> parts = new ArrayList<>();
		String part = "";
		int STATE_INSIDE = 1;
		int STATE_OUTSIDE = 2;
		int state = STATE_OUTSIDE;
		int depth = 0;
		int i = 0;
		int lineLength = line.length();
		while (i < lineLength) {
			// get token
			if (i + separator.length() < lineLength && line.substring(i, i + separator.length()).equals(separator)) {
				if (state == STATE_OUTSIDE) {
					parts.add(part);
					part = "";
					++i;
				} else {
					part += separator;
					i += separator.length();
				}
			} else if (i + begin.length() < lineLength && line.substring(i, i + begin.length()).equals(begin)) {
				part += begin;
				++depth;
				if (depth > 0)
					state = STATE_INSIDE;
				i += begin.length();
			} else if (i + end.length() < lineLength && line.substring(i, i + end.length()).equals(end)) {
				part += end;
				--depth;
				if (depth <= 0) {
					depth = 0;
					state = STATE_OUTSIDE;
				}

				i += end.length();
			} else {
				part += line.substring(i, i + 1);
				++i;
			}
		}
		if (part.length() > 0)
			parts.add(part);

		if (!parts.isEmpty()) {
			result = new String[parts.size()];
			System.arraycopy(parts.toArray(), 0, result, 0, result.length);
		}

		return result;
	}

	/**
	 * Splits a delimited line into parts
	 * 
	 * @param line
	 * @param separator
	 * @param begin
	 * @param end
	 * @return
	 */
	public String[] split(String line, String separator, String begin, String end) {
		String[] result = new String[0];

		if (begin.equals(end)) {
			List<String> parts = new ArrayList<>();
			int idx = 0;
			int lastIdx = idx;

			while (idx >= 0) {
				idx = indexOf(line, separator, begin, end, lastIdx);

				if (idx >= 0) {
					parts.add(line.substring(lastIdx, idx));
					lastIdx = idx + separator.length();
				}
			}
			if (lastIdx > 0)
				parts.add(line.substring(lastIdx));
			if (parts.isEmpty())
				parts.add(line);
			if (!parts.isEmpty()) {
				result = new String[parts.size()];
				System.arraycopy(parts.toArray(), 0, result, 0, result.length);
			}
		} else {
			result = splitNested(line, separator, begin, end);
		}

		return result;
	}

	/**
	 * Splits a string twice
	 * 
	 * @param str
	 * @return
	 */
	public List<String[]> split(String str, String[] separators, String begin, String end) {
		List<String[]> result = new ArrayList<>();
		if (separators.length > 0 && null != separators[0]) {
			String[] split1 = split(str, separators[0], begin, end);
			if (separators.length > 1 && null != separators[1]) {
				for (String s : split1) {
					result.add(split(s, separators[1], begin, end));
				}
			} else {
				for (String s : split1)
					result.add(new String[] { s });
			}
		}
		if (result.isEmpty())
			result.add(new String[] { str });

		return result;
	}

	/**
	 * Special indexOf which honors delimited texts, delimited tekst is NOT
	 * considered
	 * 
	 * @param str
	 * @param search
	 * @param begin
	 * @param end
	 * @param indexFrom
	 * @return
	 */
	public int indexOf(String str, String search, String begin, String end, int indexFrom) {
		if (search.length() <= 0)
			throw new IllegalArgumentException("search string is empty");
		int result = -1;
		int idx = indexFrom;

		result = str.indexOf(search, idx);
		if (result > 0) {
			while (indexInsideDelimitedText(str, begin, end, indexFrom, result)) {
				result = str.indexOf(search, result + search.length());
				if (result < 0)
					break;

			}
		}

		return result;
	}

	/**
	 * Checks whether or not an index is inside a delimited string.
	 * 
	 * Important : idxFrom is OUTSIDE the delimited string
	 * 
	 * @param str
	 * @param begin
	 * @param end
	 * @param idxPrevious
	 *            , assumed to be OUTSIDE when calling
	 * @param idxCurrent
	 * @return
	 */
	protected boolean indexInsideDelimitedText(String str, String begin, String end, int idxPrevious, int idxCurrent) {
		boolean result = false;
		boolean findBegin = true;
		int beginIdx = -1;
		int endIdx = -1;
		int tmp = str.indexOf(begin, idxPrevious);
		while (tmp >= 0 && tmp < idxCurrent) {
			if (findBegin) {
				tmp = str.indexOf(begin, tmp);
				if (tmp >= 0 && tmp < idxCurrent) {
					beginIdx = tmp;
					tmp += begin.length();
				}
			} else {
				tmp = str.indexOf(end, tmp);
				if (tmp >= 0) {
					endIdx = tmp;
					tmp += end.length();
				} else
					tmp = idxCurrent + 1;
			}
			findBegin = !findBegin;
		}

		if ((beginIdx >= 0) && (idxCurrent > beginIdx) && ((idxCurrent < endIdx) || endIdx < 0))
			result = true;

		return result;
	}

	/**
	 * Creates a name value map from a string.
	 * 
	 * <pre>
	 * Example: 
	 * - line (url='http://myurl.nl'&ip='12.12.12.12'&name='janssen''s shop'&adres='hoofdweg 1')
	 * - separator (&),
	 * - nvSeparator (=)
	 * - begin (')
	 * - end(') 
	 * Would produce following name value pairs: 
	 * - <url,http://myurl.nl'>
	 * - <ip,12.12.12.12>
	 * - <name,janssen's shop>
	 * - <adres,hoofdweg 1>
	 * </pre>
	 * 
	 * @param line
	 * @param separator
	 * @param nvSeparator
	 * @param begin
	 * @param end
	 * @return
	 */
	public Map<String, String> map(String line, String separator, String nvSeparator, String begin, String end) {
		String[] pairs = split(line, separator, begin, end);
		Map<String, String> result = new HashMap<>();

		for (String pair : pairs) {
			String[] nv = pair.split(nvSeparator);

			if (2 <= nv.length && null != nv[0] && null != nv[1]) {
				String tmp = nv[1].trim();
				if (tmp.startsWith(begin))
					tmp = tmp.substring(begin.length());
				if (tmp.endsWith(end))
					tmp = tmp.substring(0, tmp.length() - end.length());
				result.put(nv[0].trim(), tmp);
			}
		}

		return result;
	}

	/**
	 * Get an command line argument value
	 * 
	 * @param args
	 * @param arg
	 * @return
	 */
	public String getArgument(String[] args, String arg) {
		return getArgument(args, arg, null);
	}

	/**
	 * Get an command line argument value
	 * 
	 * @param args
	 * @param arg
	 * @param std
	 * @return
	 */
	public String getArgument(String[] args, String arg, String std) {
		String result = std;
		int idx;

		for (idx = 0; idx < args.length; ++idx) {
			String tmp = "-" + arg;
			if (args[idx].equals(tmp)) {
				++idx;
				break;
			}
		}

		if (idx < args.length)
			result = args[idx];

		return result;
	}

	/**
	 * Get an HTTP URL parameter
	 * 
	 * @param params
	 *            (HTTP parameter object)
	 * @param arg
	 * @param std
	 * @return
	 */
	public String getURLParameter(Map<?, ?> params, String arg, String std) {
		String result = std;
		Object param = params.get(arg);

		if (param instanceof String[] && ((String[]) param).length > 0)
			result = ((String[]) param)[0];

		return result;
	}

	/**
	 * Get an command line argument value
	 * 
	 * @param args
	 * @param arg
	 * @param std
	 * @return
	 */
	public Boolean getBooleanArgument(String[] args, String arg, Boolean std) {
		Boolean result = std;

		try {
			result = Boolean.parseBoolean(getArgument(args, arg));
		} catch (Exception e) {
			LogUtil.getInstance().warning("problem parsing boolean", e);
		}

		return result;
	}

	/**
	 * Get an command line argument value
	 * 
	 * @param args
	 * @param arg
	 * @param std
	 * @return
	 */
	public int getIntegerArgument(String[] args, String arg, int std) {
		int result = std;

		try {
			result = Integer.parseInt(getArgument(args, arg));
		} catch (Exception e) {
			LogUtil.getInstance().warning("problem parsing integer", e);
		}

		return result;
	}

	/**
	 * Strip the end from a StringBuilder
	 * 
	 * @param result
	 * @param ending
	 */
	public void stripEnd(StringBuilder result, String ending) {
		if (null != ending && !ending.isEmpty() && result.toString().endsWith(ending))
			result.delete(result.length() - ending.length(), result.length());
	}

	/**
	 * Get the first not null string
	 * 
	 * @param strs
	 */
	public String getFirstNotNullString(String... strs) {
		String result = null;
		for (String str : strs) {
			if (null != str) {
				result = str;
				break;
			}
		}

		return result;
	}

	/**
	 * Get the first not null and not empty string
	 * 
	 * @param strs
	 */
	public String getFirstNotNullAndNotEmpyString(String... strs) {
		String result = null;
		for (String str : strs) {
			if (null != str && !str.isEmpty()) {
				result = str;
				break;
			}
		}

		return result;
	}

	/**
	 * @see StringUtil#camelCaseFormat(String, String, String, String, String)
	 * @param str
	 * @param validChars
	 * @param camelCasingTriggerChars
	 * @return
	 */
	public String camelCaseFormat(String str, String validChars, String camelCasingTriggerChars) {
		return camelCaseFormat(str, "", "", validChars, camelCasingTriggerChars, false);
	}

	private char[] str2CharArray(String str) {
		char[] result;
		if (null != str && !str.isEmpty()) {
			result = new char[str.length()];
			str.getChars(0, result.length, result, 0);
		} else {
			result = new char[0];
		}
		return result;
	}

	/**
	 * Create camelcase version of string.
	 * 
	 * <pre>
	 * - Non valid characters (not defined by validChars) are skipped.
	 * - Camelcasing is trigged by specific characters.
	 * Examples:
	 * - validChars "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	 * - camelCasingTriggerChars " +-"
	 * - "aaa bbb ccc" => "aaaBbbCcc"
	 * - " aaa bbb ccc" => "AaaBbbCcc"
	 * - "a+b+c+aa+bb+cc" => "aBCAaBbCc"
	 * - "a%b+%c%aa% bb-cc" => "abCaaBbCc"
	 * </pre>
	 * 
	 * @param str
	 * @param substitute
	 * @param substitutes
	 * @param validChars
	 * @param camelCasingTriggerChars
	 * @return
	 */
	public String camelCaseFormat(String str, String substitute, String substitutes, String validChars,
			String camelCasingTriggerChars, boolean uppercaseFirstChar) {
		char[] tmp = str2CharArray(str);
		char[] camel = str2CharArray(camelCasingTriggerChars);
		char[] valid = str2CharArray(validChars);
		char[] subst = str2CharArray(substitute);
		char[] substs = str2CharArray(substitutes);
		char[] target = new char[str.length()];
		int targetIdx = 0;
		boolean doCamel = false;
		int caseOffset = 'A' - 'a';
		String result = "";
		for (int i = 0; i < tmp.length; ++i) {
			boolean isCamel = false;
			boolean isValid = false;
			char c = tmp[i];

			for (int n = 0; n < subst.length; ++n) {
				if (c == subst[n] && n < substs.length) {
					c = substs[n];
					break;
				}
			}
			for (int n = 0; n < valid.length; ++n) {
				if (c == valid[n]) {
					isValid = true;
					break;
				}
			}
			for (int n = 0; n < camel.length; ++n) {
				if (c == camel[n]) {
					isCamel = true;
					doCamel = true;
					break;
				}
			}
			if (!isCamel && isValid) {
				if ((doCamel || (targetIdx == 0 && uppercaseFirstChar)) && c >= 'a' && c <= 'z')
					target[targetIdx++] = (char) (c + caseOffset);
				else
					target[targetIdx++] = c;
				doCamel = false;
			}
		}
		if (targetIdx > 0) {
			result = new String(target, 0, targetIdx);
		}
		return result;
	}

	public String rtf2String(String str) {
		String result = str;
		if (null != str && str.startsWith("{\\rtf1\\ansi\\")) {
			RTFEditorKit rtfParser = new RTFEditorKit();
			Document document = rtfParser.createDefaultDocument();
			try {
				rtfParser.read(new ByteArrayInputStream(str.getBytes()), document, 0);
				result = document.getText(0, document.getLength());
			} catch (BadLocationException | IOException e) {
				LogUtil.getInstance().info("error converting RTF", e);
			}
		}
		return result;
	}

	/**
	 * Replaces strings inside an inner block in a string. The block is defined by a
	 * blockStart AND a blockEnd. Only replacements in the inner block are
	 * performed.
	 * 
	 * <pre>
	 * Example:
	 *   str = "111 ( 222 (444 333 ( 444 555) ( 444) ))"
	 *   blockStart = '('
	 *   blockEnd = ')'
	 *   search = "444"
	 *   replace = "aha"
	 *   
	 *   The result = "111 ( 222 ( 333 444( aha 555) ( aha) ))"
	 *   
	 *   Note that the 444 that is NOT replaced is not contained in an inner block
	 * 
	 * </pre>
	 * 
	 * @param str
	 * @param blockStart
	 * @param blockEnd
	 * @param search
	 * @param replace
	 * @return
	 */
	public String replaceInInnerblock(String str, char blockStart, char blockEnd, String wholeWordCharacters,
			String search, String replace) {
		String result = str;
		int idxCurr = 0;
		final int MAX_REPLACE = 1000;
		int[] replaceIdxs = new int[MAX_REPLACE];
		int replaceCount = 0;

		while (idxCurr >= 0 && replaceCount < MAX_REPLACE && idxCurr < str.length()) {
			++idxCurr;
			int idxStart = str.indexOf(blockStart, idxCurr);
			int idxEnd = str.indexOf(blockEnd, idxCurr);
			int idxMax;
			if (idxStart != -1 && idxStart < idxEnd) {
				idxCurr = idxStart;
			} else {
				// Inside inner block!
				idxMax = idxEnd;
				if (idxMax > idxCurr) {
					// Try to find search string
					boolean found;
					do {
						found = false;
						int idxReplace = str.indexOf(search, idxCurr);
						if (idxReplace >= idxCurr && idxReplace < idxMax) {
							// Check for whole word
							if ((null == wholeWordCharacters || wholeWordCharacters.length() == 0)
									|| (wholeWordCharacters.contains("" + str.charAt(idxReplace - 1))
											&& wholeWordCharacters
													.contains("" + str.charAt(idxReplace + search.length())))) {
								replaceIdxs[replaceCount++] = idxReplace;
							}
							found = true;
							idxCurr = idxReplace + 1;
						}
					} while (found && replaceCount < MAX_REPLACE);

					if (idxStart != -1)
						idxCurr = idxEnd;
					else
						idxCurr = -1; // Stop!
				}
			}
		}
		if (replaceCount > 0) {
			result = "";
			int idxStart = 0;

			for (int i = 0; i < replaceCount; ++i) {
				result += str.substring(idxStart, replaceIdxs[i]);
				result += replace;
				idxStart = replaceIdxs[i] + search.length();
			}
			if (idxStart < str.length())
				result += str.substring(idxStart);
		}
		return result;

	}

	/**
	 * Checks whether an index is inside a block
	 * 
	 * @param line
	 * @param begin
	 * @param end
	 * @param idx
	 * @return
	 */
	public boolean isInsideBlock(String str, String begin, String end, int idx) {
		boolean result = false;

		int startIdx = 0;
		int endIdx = 0;
		while (startIdx >= 0 && endIdx >= 0) {
			startIdx = str.indexOf(begin, endIdx);
			if (startIdx >= 0) {
				startIdx += begin.length();
				endIdx = str.indexOf(end, startIdx);
				if (idx >= startIdx && (idx < endIdx || endIdx < 0)) {
					result = true;
					break;
				}
				if (startIdx > idx)
					break;
				if (endIdx >= 0)
					endIdx += end.length();
			}

		}

		return result;

	}

	public String stripQuotes(String str, String quote) {
		String result = str;

		if (null != quote && str.startsWith(quote) && str.endsWith(quote)) {
			result = str.substring(quote.length(), str.length() - quote.length());
		}

		return result;
	}

	public boolean isNumber(String str) {
		boolean result = false;
		try {
			Integer.parseInt(str);
			result = true;
		} catch (NumberFormatException e) {
			LogUtil.getInstance().ignore("string is not a number", e);
			result = false;
		}
		return result;
	}

	/**
	 * Verifies if a string is contained within a comma separated include list
	 * 
	 * @param filter
	 * @param str
	 * @return
	 */
	public boolean filterInclude(String filter, String str) {
		boolean result = false;
		String[] includes = filter.split(",");

		for (String include : includes) {
			if (include.equals(str)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Concattenate a list of strings
	 * 
	 * @param strs
	 * @return
	 */
	public String concat(List<String> strs) {

		String result = "";

		for (String str : strs) {
			if (result.length() > 0)
				result += " ";
			result += str;
		}

		return result;
	}

	/**
	 * Verifies if a string is null or empty
	 * 
	 * @param attribuut
	 * @return
	 */
	public boolean isEmpty(String str) {
		return null == str || str.isEmpty();
	}

	/**
	 * Converts a string to a byte array
	 */
	public byte[] string2ByteArray(String str) {

		byte[] result = new byte[0];
		if (null != str && !str.isEmpty()) {
			result = new byte[str.length() / 2 + ((str.length() % 2 != 0) ? 1 : 0)];
			boolean combine = true;
			int n = result.length - 1;
			int tmp = 0;
			for (int i = str.length() - 1; i >= 0; --i) {
				tmp = 0;
				char c = str.charAt(i);
				if (c >= '0' && c <= '9')
					tmp = c - '0';
				else if (c >= 'a' && c <= 'f')
					tmp = c - 'a' + 10;
				else if (c >= 'A' && c <= 'F')
					tmp = c - 'A' + 10;

				if (combine)
					result[n] = (byte) tmp;
				else
					result[n--] |= ((byte) (tmp << 4));
				combine = !combine;
			}
			if (result.length % 2 != 0)
				result[n] = (byte) tmp;
		}

		return result;
	}

	/**
	 * Convert a byte array to a string
	 * 
	 * @param bytes
	 * @return
	 */
	public String byteArray2String(byte[] bytes, boolean includeTrailingZero) {
		StringBuilder result = new StringBuilder(bytes.length / 2 + ((bytes.length % 2 != 0) ? 1 : 0));
		for (int i = bytes.length - 1; i >= 0; --i) {
			int tmp = bytes[i] & 0x0f;
			result.append((char) ((tmp < 10) ? '0' + tmp : 'a' + tmp - 10));
			tmp = ((bytes[i] & 0xf0) >> 4) & 0x0f;
			if (tmp > 0 || i > 0 || includeTrailingZero)
				result.append((char) ((tmp < 10) ? '0' + tmp : 'a' + tmp - 10));
		}

		return result.reverse().toString();
	}

	/**
	 * printf
	 */
	public String printf(String format, Object... args) {
		String result = format;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(bos)) {
			ps.printf(format, args);
			result = bos.toString();
		}
		return result;
	}

	/**
	 * padding
	 */
	public String pad(String str, char c, int length) {
		String result = str;

		if (null != str && length > str.length()) {
			char[] padding = new char[length - str.length()];
			for (int i = 0; i < length - str.length(); ++i)
				padding[i] = c;
			result = new String(padding) + result;

		}

		return result;
	}

	/*
	 * Get the last part of a string, using the split
	 */
	public String getLastPart(String str, String split) {
		String result = "";

		int idx = str.lastIndexOf(split);

		if (idx >= 0 && str.length() > idx)
			result = str.substring(idx + 1);

		return result;
	}

	/**
	 * Perform a replacement on a string using regular expressions. The replacement
	 * stops when the first replacement pair changes the string.
	 * 
	 * Example: replace("([^\\.]*)\\.([^\\.]*)$,$2") returns the filename
	 * 
	 * @param str
	 * @param replaceConfig
	 *            (<match>,<replacement>)(,<match>,<replacement>)*
	 * @return
	 */
	public String replace(String str, String replaceConfig) {
		String result = str;
		if (null != replaceConfig && !replaceConfig.isEmpty()) {
			String[] replace = replaceConfig.split(",");

			if (replace.length % 2 == 0) {
				String old = result;
				for (int i = 0; i < replace.length; i += 2) {
					result = result.replaceAll(replace[i], replace[i + 1]);
					// stop when first replacement changes the result
					if (replace.length > 2 && !old.equals(result))
						break;
				}
			} else if (replace.length == 1)
				result = result.replace(replace[0], "");

		}

		return result;
	}

	/**
	 * Replace variables in a string. A variable has the format ${variable}
	 * 
	 * @param str
	 * @param mapper
	 * @return
	 */
	public String replace(String str, Function<String, String> mapper) {
		Pattern pat = Pattern.compile("[$][{][^}]*[}]");
		Matcher matcher = pat.matcher(str);
		String result = str;
		while (matcher.find()) {
			String variable = str.substring(matcher.start(), matcher.end());
			String value = mapper.apply(variable.substring(2, variable.length() - 1));
			if (null != value)
				result = result.replace(variable, value);
		}

		return result;
	}

	/**
	 * Slices a string with a start and end string. The result is everything in
	 * between
	 * 
	 * @param str
	 * @param start
	 * @param end
	 * @return
	 */
	public String slice(String str, String start, String end) {
		String result = str;
		int tmpIdx = result.indexOf(start);

		if (tmpIdx >= 0)
			result = result.substring(tmpIdx + start.length());

		tmpIdx = result.lastIndexOf(end);

		if (tmpIdx >= 0)
			result = result.substring(0, tmpIdx);

		return result;
	}

}

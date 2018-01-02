package conversion;

import csv.CSVUtil;
import csv.CSVWriterCallback;
import data.EnumUtil;
import data.StringUtil;
import data.Util;
import xml.XMLUtil;

public class ConversionUtil implements Util {
	private static final ThreadLocal<ConversionUtil> instance = new ThreadLocal<ConversionUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ConversionUtil getInstance() {
		ConversionUtil result = instance.get();

		if (null == result) {
			result = new ConversionUtil();
			instance.set(result);
		}

		return result;
	}

	private String makeValidName(String str) {
		return StringUtil.getInstance().camelCaseFormat(str, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
				" ");
	}

	public void CSV2XML(String csvFile, char commaToken, String xmlFile, String replaceConfig,
			CSVUtil.Option... options) {
		boolean trimValues = EnumUtil.getInstance().contains(options, CSVUtil.Option.TRIM_VALUES);
		CSVUtil.getInstance().copy(csvFile, commaToken, xmlFile, new CSVWriterCallback() {
			String[] header;

			@Override
			public String getLine(long idx, String[] line, CSVUtil.Option... options) {
				String result = "    <row nr=\"" + idx + "\">\n";
				if (null == header && idx == 0)
					result = "<csv>" + "\n" + result;
				int valueIdx = 0;
				for (String value : line) {	
					if (trimValues)
						value = (null != value) ? value.trim() : value;
					if (null != replaceConfig && !replaceConfig.isEmpty())
						result = StringUtil.getInstance().replace(result, replaceConfig);
					result += "        ";
					result += (null != header) ? "<" + makeValidName(header[valueIdx]) + ">"
							: "<col nr=\"" + valueIdx + "\">";
					if (null != value)
						result += (null != value) ? XMLUtil.getInstance().escapeXML(value) : "";

					result += (null != header) ? "</" + makeValidName(header[valueIdx]) + ">" : "</col>";
					result += "\n";
					++valueIdx;
				}
				result += "    </row>\n";
				return result;
			}

			@Override
			public String getHeader(String[] header, CSVUtil.Option... options) {
				this.header = header;
				return "<csv>" + "\n";
			}

			@Override
			public String getFooter() {
				return "</csv>" + "\n";
			}
		}, options);
	}
}

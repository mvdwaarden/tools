package json;

import java.text.SimpleDateFormat;
import java.util.Date;

import object.ObjectUtil;

public class DataConverter {
	private static final ThreadLocal<DataConverter> instance = new ThreadLocal<DataConverter>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static DataConverter getInstance() {
		DataConverter result = instance.get();

		if (null == result) {
			result = new DataConverter();
			instance.set(result);
		}

		return result;
	}

	public JSONValue<?> makeJSONValue(Object obj) {
		JSONValue<?> result = null;
		if (ObjectUtil.getInstance().isString(obj)) {
			result = new JSONValue<String>((String) obj);
		} else if (ObjectUtil.getInstance().isDate(obj)) {
			if (obj instanceof Date) {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				result = new JSONValue<String>(fmt.format((Date) obj));
			} else
				result = new JSONValue<String>(obj.toString());
		} else if (ObjectUtil.getInstance().isNumber(obj, ObjectUtil.Option.INTEGER)) {
			result = new JSONValue<Long>(Long.parseLong(obj.toString()));
		} else if (ObjectUtil.getInstance().isNumber(obj, ObjectUtil.Option.FLOATING_POINT)) {
			result = new JSONValue<Double>(Double.parseDouble(obj.toString()));
		} else if (ObjectUtil.getInstance().isBoolean(obj)) {
			result = new JSONValue<Boolean>((Boolean) obj);
		}

		return result;
	}
}

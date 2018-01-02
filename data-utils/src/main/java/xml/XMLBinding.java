package xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import data.DataUtil;
import data.LogUtil;

public class XMLBinding {
	public enum BindingValueType {
		STRING, STREAM;
	}

	private String prefix;
	private String name;
	private String uri;
	private BindingValueType valueType;
	private String strValue;
	private InputStream isValue;

	public XMLBinding(String name, InputStream is) {
		this("xs", null, name, is);
	}

	public XMLBinding(String name, String value) {
		this("xs", null, name, value);
	}

	public XMLBinding(String prefix, String uri, String name, InputStream is) {
		this.prefix = prefix;
		this.name = name;
		this.uri = uri;
		setValue(is);
	}

	public XMLBinding(String prefix, String uri, String name, String value) {
		this.prefix = prefix;
		this.name = name;
		this.uri = uri;
		setValue(value);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public String getURI() {
		return uri;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public BindingValueType getValueType() {
		return valueType;
	}

	public void setValueType(BindingValueType valueType) {
		this.valueType = valueType;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		Object result = null;
		switch (valueType) {
		case STREAM:
			result = isValue;
			break;
		case STRING:
			result = strValue;
			break;
		}
		return (T) result;
	}

	public String getString() {
		String result = null;
		switch (valueType) {
		case STREAM:
			result = DataUtil.getInstance().readFromInputStream(isValue, null);
			break;
		case STRING:
			result = strValue;
			break;
		}
		return result;
	}

	public InputStream getInputStream() {
		InputStream result = null;
		switch (valueType) {
		case STREAM:
			result = isValue;
			break;
		case STRING:
			result = new ByteArrayInputStream(strValue.getBytes());
			break;
		}
		return result;
	}

	public void setValue(String value) {
		this.strValue = value;
		this.valueType = BindingValueType.STRING;
	}

	public void setValue(InputStream isValue) {
		this.isValue = isValue;
		this.valueType = BindingValueType.STREAM;
	}

	public boolean close() {
		boolean result = true;
		switch (valueType) {
		case STREAM:
			try {
				isValue.close();
			} catch (IOException e) {
				LogUtil.getInstance().warning("problem closing binding [" + name + "]", e);
				result = false;
				break;
			}
			break;
		case STRING:
			break;
		}

		return result;
	}
}

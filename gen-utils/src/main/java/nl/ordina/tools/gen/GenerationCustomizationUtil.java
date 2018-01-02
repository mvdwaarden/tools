package nl.ordina.tools.gen;

import java.util.List;

import data.StringUtil;
import metadata.MetaAtom;
import metadata.MetaAtom.BaseType;
import metadata.MetaElement;
import metadata.MetaType;

public class GenerationCustomizationUtil {
	private static final ThreadLocal<GenerationCustomizationUtil> instance = new ThreadLocal<GenerationCustomizationUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static GenerationCustomizationUtil getInstance() {
		GenerationCustomizationUtil result = instance.get();

		if (null == result) {
			result = new GenerationCustomizationUtil();
			instance.set(result);
		}

		return result;
	}

	protected static final String DATA_TYPE_PREFIX = "_DT_";

	public String makeImplementationName(String str) {
		return makeImplementationName(str, false);
	}

	public String makePublicName(MetaAtom attr) {
		return attr.getName();
	}

	public String makePublicName(MetaElement element) {
		return element.getName();
	}

	private String cleanupString(String str) {
		return cleanupString(str, false);
	}

	protected String cleanupString(String str, boolean uppercaseFirstChar) {
		String result = StringUtil.getInstance().camelCaseFormat(str, "àáâãäåçèéêëìíîïòóôõö", "aaaaaaceeeeiiiiooooo",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -_", "", uppercaseFirstChar);

		return result;
	}

	public String makeFilename(String filename) {
		return cleanupString(filename);
	}

	public GenerationCustomizationUtil() {
		super();
	}

	public String getXsdAttributeType(MetaElement child) {
		String result = null;
		if (child.getType() instanceof BaseType) {
			String[][] lookup = new String[][] { { BaseType.BOOLEAN.name(), "boolean" },
					{ BaseType.DATE.name(), "date" }, { BaseType.FLOAT.name(), "float" },
					{ BaseType.INT.name(), "int" }, { BaseType.STRING.name(), "string" } };
			for (String[] e : lookup) {
				if (child.getType().getName().equals(e[0])) {
					result = e[1];
					break;
				}
			}
		} else
			result = child.getType().getName();

		return result;
	}

	public List<String> getXsdDocumentation(MetaElement child) {
		return child.getDocumentation();
	}

	public String makeEntityName(MetaElement element) {
		return makeImplementationName(element);
	}

	public String makeImplementationName(MetaElement element) {
		return makeImplementationName(stripName(element.getName()), true);
	}

	public String makeImplementationName(MetaType type) {
		return makeImplementationName(stripName(type.getName()), true);
	}

	public String stripName(String str) {
		String result = "";
		String[] toStrip = new String[] { "volgnummer", "volgnr", "indicator" };
		for (String strip : toStrip) {
			if (str.toLowerCase().startsWith(strip)) {
				result = str.substring(strip.length()).trim();
			}
		}
		if (result.length() == 0) {
			result = str;
		}

		return result;
	}

	public String makeImplementationName(String str, boolean upperCaseFirstChar) {
		String result = "";
		StringBuilder tmp = new StringBuilder(
				StringUtil.getInstance().camelCaseFormat(str, "àáâãäåçèéêëìíîïòóôõö", "aaaaaaceeeeiiiiooooo",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", " ", upperCaseFirstChar));
		StringUtil.getInstance().stripEnd(tmp, "_");

		if ("0123456789".contains("" + tmp.charAt(0))) {
			result = "_" + tmp.toString();
		} else {
			result = tmp.toString();
		}

		return result.toString();
	}

}
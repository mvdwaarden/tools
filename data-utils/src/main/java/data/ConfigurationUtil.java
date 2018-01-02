package data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

/**
 * Usage: Java VM parameter -Dconf.mode
 * 
 * -Dconf.mode = [mode.thread|mode.global|mode.map:<key>]
 * 
 * Default = mode.thread
 * 
 * @author mwa17610
 * 
 */
public class ConfigurationUtil implements Util {
	public static final String CONFIGURATION_MODE = "conf.mode";
	public static final String CLASS_PATH_PREFIX = "classpath://";
	public static final String FILENAME_PREFIX = "file://";
	public static final String TEST_RESOURCES_DIR = "test.resources.dir";
	public static final String REPORT_DIR = "report.dir";
	public static final String TEST_OUTPUT_DIR = "test.output.dir";
	public static final String PROJECT_NAME = "project.name";
	public static final String IMPORT_DECLARATION = "#import";

	public enum ConfigurationMode {
		MODE_THREAD("mode.thread"), MODE_GLOBAL("mode.global"), MODE_MAP("mode.map");
		private String code;

		ConfigurationMode(String code) {
			this.code = code;
		}

		public String getCode() {
			return this.code;
		}

		public static ConfigurationMode getModeByCode(String code) {
			ConfigurationMode result = MODE_THREAD;
			if (null != code) {
				for (ConfigurationMode mode : ConfigurationMode.values()) {
					if (code.startsWith(mode.getCode())) {
						result = mode;
						break;
					}
				}
			}

			return result;
		}
	}

	private static Map<String, ConfigurationUtil> instanceMap = new HashMap<>();
	private static ConfigurationMode mode;
	private static ConfigurationUtil globalInstance;
	private static final ThreadLocal<ConfigurationUtil> instance = new ThreadLocal<ConfigurationUtil>();
	public Properties properties;
	public String configuration;

	public ConfigurationUtil() {
		super();
	}

	/**
	 * Clone the thread local to another thread, typically called before the
	 * thread is actually run.
	 * 
	 * @param util
	 */
	public void clone(ConfigurationUtil instance) {
		switch (ConfigurationUtil.mode) {
		case MODE_THREAD:
			ConfigurationUtil.instance.set(instance);
			break;
		default:
			break;
		}
	}

	/**
	 * Get the thread local 'singleton'
	 * 
	 * Singleton is either: - thread scope - global scope - map scope
	 * 
	 * @return
	 */
	public static ConfigurationUtil getInstance() {
		ConfigurationUtil result = null;
		String value = System.getProperty(CONFIGURATION_MODE);
		if (null == ConfigurationUtil.mode)
			ConfigurationUtil.mode = ConfigurationMode.getModeByCode(value);

		switch (ConfigurationUtil.mode) {
		case MODE_THREAD:
			result = instance.get();
			if (null == result) {
				result = new ConfigurationUtil();
				instance.set(result);
				LogUtil.getInstance()
						.info("current working directory [" + DataUtil.getInstance().getCurrentWorkingdir() + "]");
			}
			break;
		case MODE_GLOBAL:
			if (null == globalInstance) {
				globalInstance = result = new ConfigurationUtil();
				LogUtil.getInstance()
						.info("current working directory [" + DataUtil.getInstance().getCurrentWorkingdir() + "]");
			}
			break;
		case MODE_MAP:
			String key = value.substring(value.indexOf(":") + 1);
			result = instanceMap.get(key);
			if (null == result) {
				result = new ConfigurationUtil();
				instanceMap.put(key, result);
				LogUtil.getInstance()
						.info("current working directory [" + DataUtil.getInstance().getCurrentWorkingdir() + "]");
			}
			break;
		default:
			break;
		}

		return result;
	}

	public ConfigurationUtil init(String resourcename) {
		if (null == properties) {
			initWithImports(null, resourcename, new ArrayList<>());
		}
		return this;
	}

	/**
	 * Initialize from a resource. The resourcename prefix determines place to
	 * look for the resource. The prefix can be:
	 * 
	 * <pre>
	 * - 'classpath://' : This means a resource in the classpath (typically JAR)
	 * - 'file://' : This means a resource on the file system
	 * - other : a resource on the filesystem.
	 * </pre>
	 * 
	 * @param resourcename
	 * @return
	 */
	public ConfigurationUtil initWithImports(Class<?> cls, String resourcename, List<String> importList) {
		importList.add(resourcename);
		this.configuration = resourcename;
		// create properties object
		if (null == this.properties)
			this.properties = new Properties();
		InputStream ips = null;
		try {
			// determine import specification
			ips = openResourceStream(cls, resourcename);
			LogUtil.getInstance().info("looking for imports in [" + resourcename + "]");
			DataUtil.getInstance().readLinesFromFile(ips, resourcename, null, null, (nr, line) -> {
				if (line.startsWith(IMPORT_DECLARATION)) {
					String importResource = line.substring(IMPORT_DECLARATION.length()).trim();
					if (!importList.contains(importResource)) {
						importList.add(importResource);
						LogUtil.getInstance().warning("imported [" + importResource + "] in [" + resourcename + "]");
						initWithImports(cls, importResource, importList);
					} else {
						StringBuilder cycleInfo = new StringBuilder();
						for (String tmp : importList)
							cycleInfo.append(tmp + "->");
						StringUtil.getInstance().stripEnd(cycleInfo, "->");
						LogUtil.getInstance().warning("inhibited import for [" + importResource + "] in ["
								+ resourcename + "] cyclic! [" + cycleInfo.toString() + "]");
					}
				}
			});
			DataUtil.getInstance().close(ips);
			ips = openResourceStream(cls, resourcename);
			LogUtil.getInstance().info("loading properties for [" + resourcename + "]");
			// load properties and append to 'this' properties
			Properties tmpProperties = new Properties();
			tmpProperties.load(ips);
			for (Entry<Object, Object> e : tmpProperties.entrySet())
				this.properties.put(e.getKey(), e.getValue());
			LogUtil.getInstance().info("loaded configuration from [" + resourcename + "]");
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to load properties [" + resourcename + "]", e);
			DataUtil.getInstance().close(ips);
		}

		return this;
	}

	private InputStream openResourceStream(Class<?> cls, String configuration) throws FileNotFoundException {
		InputStream ips;
		// Check for indirection
		String resourcename = System.getProperty(configuration);
		// Otherwise asume load from class path
		if (null == resourcename && null != cls && !configuration.startsWith(CLASS_PATH_PREFIX))
			resourcename = CLASS_PATH_PREFIX + DataUtil.PATH_SEPARATOR + configuration + ".properties";
		else if (null == resourcename)
			resourcename = configuration;
		LogUtil.getInstance().warning("used resource [" + resourcename + "] for configuration [" + configuration + "]");
		if (resourcename.startsWith(CLASS_PATH_PREFIX)) {
			ips = getClass().getResourceAsStream(resourcename.substring(CLASS_PATH_PREFIX.length()));
		} else if (resourcename.startsWith(FILENAME_PREFIX)) {
			ips = new FileInputStream(resourcename.substring(FILENAME_PREFIX.length()));
		} else {
			ips = new FileInputStream(resourcename);
		}

		return ips;
	}

	/**
	 * <pre>
	 * Initialize from :
	 * [a] <configuration> system property is defined =>  the system property value defines the resource,
	 *                                                    this can still be classpath:// or file:// 
	 *                                                    or something else.
	 * [b] the cls class is defined => find by using <class>.getResourceAsStream('<configuration>.properties')
	 * [c] cls class is NULL => this.class.getResourceAsStream('/<configuration>.properties')
	 * </pre>
	 * 
	 * @param cls
	 *            (null => absolute location, Class => resource at class file
	 *            location)
	 * @param configuration
	 * @return
	 */
	public ConfigurationUtil init(Class<?> cls, String configuration) {
		if (null == properties) {
			this.configuration = configuration;

			initWithImports(cls, configuration, new ArrayList<>());
		}

		return this;
	}

	/**
	 * Get a setting by its name
	 * 
	 * @param name
	 * @return
	 */
	public String getSetting(String name) {
		return getSetting(name, "");
	}

	/**
	 * Get a boolean setting by its name. If the settings is not found the
	 * default value is used.
	 * 
	 * @see #getSetting(String)
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public boolean getBooleanSetting(String name, boolean defaultValue) {
		boolean result = defaultValue;
		String tmp = getSetting(name);

		if (null != tmp && !tmp.isEmpty())
			result = Boolean.parseBoolean(tmp);

		return result;
	}

	/**
	 * Get a integer setting by its name. If the settings is not found the
	 * default value is used.
	 * 
	 * @see #getSetting(String)
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public int getIntegerSetting(String name, int defaultValue) {
		int result = defaultValue;
		String tmp = getSetting(name);

		if (null != tmp && !tmp.isEmpty())
			result = Integer.parseInt(tmp);

		return result;
	}

	/**
	 * Get a setting by its name. If the setting value is expanded recursively,
	 * if it contains ${} variable parts.
	 * 
	 * @param name
	 * @param defaultValue
	 * @param recursionControl
	 * @return
	 */
	protected String getSettingRecursive(String name, Stack<String> recursionControl) {
		String result = (null != properties) ? properties.getProperty(name) : null;

		if (null != result && result.contains("${")) {
			class Locals {
				Stack<String> recursionControl;
			}
			;
			final Locals _locals = new Locals();
			_locals.recursionControl = recursionControl;
			if (null == _locals.recursionControl)
				_locals.recursionControl = new Stack<>();
			if (!_locals.recursionControl.contains(name)) {
				_locals.recursionControl.push(name);
				// replace for all ${setting} occurrences
				String tmp = StringUtil.getInstance().replace(result,
						var -> getSettingRecursive(var, _locals.recursionControl));
				// for sake of speed, make a backup and replace the property
				properties.setProperty("_" + name + "_original", result);
				properties.setProperty(name, tmp);
				result = tmp;
				_locals.recursionControl.pop();
			} else {
				String tmp = "";
				if (_locals.recursionControl.size() > 0)
					tmp = _locals.recursionControl.get(0);
				for (int i = 1; i < _locals.recursionControl.size(); ++i)
					tmp += "->" + _locals.recursionControl.get(i);
				LogUtil.getInstance().error("setting recursion detected, check configuration [" + tmp + "]");
				result = null;
			}
		}
		return result;
	}

	/**
	 * Get a setting by its name. If the setting is not found (i.e. null) the
	 * default value is used.
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getSetting(String name, String defaultValue) {
		String result = getSettingRecursive(name, null);
		if (null == result)
			result = defaultValue;

		return result;
	}

	/**
	 * Resource path for unit tests
	 * 
	 * @return
	 */
	public String getTestResourcesPath() {
		return getSetting(TEST_RESOURCES_DIR, DataUtil.getInstance().simplifyFolder(
				DataUtil.getInstance().getCurrentWorkingdir() + DataUtil.PATH_SEPARATOR + "./src/test/resources")
				+ DataUtil.PATH_SEPARATOR);
	}

	/**
	 * Resource path for unit tests
	 * 
	 * @return
	 */
	public String getTestOutputPath() {
		String result = getSetting(TEST_OUTPUT_DIR,
				DataUtil.getInstance().simplifyFolder(
						DataUtil.getInstance().getCurrentWorkingdir() + DataUtil.PATH_SEPARATOR + "../target/test-out")
						+ DataUtil.PATH_SEPARATOR);

		return result;
	}

	/**
	 * Resource path for unit tests
	 * 
	 * @return
	 */
	public String getReportPath() {
		return getSetting(REPORT_DIR,
				DataUtil.getInstance().simplifyFolder(
						DataUtil.getInstance().getCurrentWorkingdir() + DataUtil.PATH_SEPARATOR + "./src/test/report")
						+ DataUtil.PATH_SEPARATOR);
	}

	/**
	 * Clear the configuration
	 */
	public void clear() {
		this.properties = null;
	}

	/**
	 * Return the project name
	 */
	public String getProjectName() {
		return getSetting(PROJECT_NAME, "");
	}

	public String getTmpDir() {
		return getSetting(DataUtil.TMP_DIR, ".");
	}
}

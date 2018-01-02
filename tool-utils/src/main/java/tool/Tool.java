package tool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import data.Sourcedir;
import data.StringUtil;
import data.Targetdir;
import data.TimeUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.fs.FileNode;
import graph.ext.dm.fs.FileSystemNode;
import graph.ext.mod.fs.FileSystemReader;
import json.JSONObject;
import json.JSONUtil;

public abstract class Tool implements Sourcedir, Targetdir {
	public static final String ARG_FUNCTION = "func";
	public static final String ARG_MODE = "mode";
	public static final String ARG_FILE = "file";
	public static final String ARG_SRC_DIR = "sourcedir";
	public static final String ARG_TMP_DIR = "tempdir";
	public static final String ARG_MODE_SERVER = "server";
	public static final String ARG_MODE_CLIENT = "client";
	public static final String ARG_CONFIG = "cfg";
	public static final String ARG_TOOL_CLASS = "tool";
	public static final String CONFIGURATION_PROFILE = "configuration.profile";
	public static final String CONFIGURATION_PROFILES = "configuration.profiles";
	public static final String DEST_DIR_FORMAT_URL = "dest.dir.format.url";
	public static final String SRC_DIR = "src.dir";
	public static final String FILE_ROOT_DIR = "file.root.dir";
	public static final String SRC_FILE = "src.file";
	public static final String DEST_DIR = "dest.dir";
	public static final String DEST_MERGE = "dest.merge";
	public static final String MODE = "mode";
	private String targetdir;
	private String sourcedir;

	public enum Option {
		CLIENT, ORACLE_ANALYSE_ONLY
	}

	public Tool() {
		super();
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> getFileGraph(String dir) {
		return getFileGraph(dir, null);
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> getFileGraph(String dir, String regex) {
		return getFileGraph(dir, regex, true);
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> getFileGraph(String dir, String regex, boolean recursive) {
		FileSystemReader reader = new FileSystemReader();

		Graph<FileSystemNode, Edge<FileSystemNode>> result = reader.read(dir, regex,
				(recursive) ? FileSystemReader.Option.OPTION_RECURSIVE : FileSystemReader.Option.NOP);
		if (result.filterNodes(fn -> fn instanceof FileNode).isEmpty())
			LogUtil.getInstance().info("found no files in [" + dir + "] using regex [" + regex + "]");

		result.setName(dir);

		return result;
	}

	public String getSetting(String[] args, String option, String setting, String function) {
		// order of retrieval:
		// 1) try the command line argument first
		// 2) try the function specific configuration setting
		// 3) try the global configuration setting
		String result = StringUtil.getInstance().getArgument(args, option,
				ConfigurationUtil.getInstance().getSetting(setting + "." + function));
		if (null == result || result.isEmpty())
			result = ConfigurationUtil.getInstance().getSetting(setting);

		return result;
	}

	public String getSetting(Map<?, ?> params, String option, String setting, String function) {
		// order of retrieval:
		// 1) try the url parameter first
		// 2) try the function specific configuration setting
		// 3) try the global configuration setting
		String result = StringUtil.getInstance().getURLParameter(params, option,
				ConfigurationUtil.getInstance().getSetting(setting + "." + function));
		if (null == result || result.isEmpty())
			result = ConfigurationUtil.getInstance().getSetting(setting);

		return result;
	}

	public String getSetting(String setting, String function) {
		return getSetting(new String[] {}, "", setting, function);
	}

	public String[] getConfigurations() {
		String[] result = new String[] {};
		readRootConfiguration();
		String profiles = ConfigurationUtil.getInstance().getSetting(CONFIGURATION_PROFILES);

		if (null != profiles)
			result = profiles.split(",");

		return result;
	}

	public void run(String[] args) {
		String functions = StringUtil.getInstance().getArgument(args, ARG_FUNCTION);
		String configuration = StringUtil.getInstance().getArgument(args, ARG_CONFIG);
		LogUtil.getInstance().info("good " + TimeUtil.getInstance().getTimeOfDay().name().toLowerCase());
		LogUtil.getInstance().info("using default characterset [" + Charset.defaultCharset().name() + "]");
		readConfiguration(configuration);
		long start = TimeUtil.getInstance().tick();
		String targetBasedir = StringUtil.getInstance().getArgument(args, "targetdir",
				ConfigurationUtil.getInstance().getSetting(DEST_DIR) + DataUtil.PATH_SEPARATOR);
		LogUtil.getInstance().info("using base targetdir [" + targetBasedir + "]");
		List<String[]> outputdirs = new ArrayList<>();
		for (String function : functions.split(",")) {
			long funcstart = TimeUtil.getInstance().tick();
			/**
			 * Target directory worden geappend met de functie.
			 */

			if (ConfigurationUtil.getInstance().getBooleanSetting(DEST_MERGE, false))
				setTargetdir(targetBasedir);
			else
				setTargetdir(targetBasedir + function);
			setSourcedir(getSetting(args, ARG_SRC_DIR, SRC_DIR, function));
			String sourcefile = getSetting(args, ARG_FILE, SRC_FILE, function);
			DataUtil.getInstance().makeDirectories(getTargetdir() + DataUtil.PATH_SEPARATOR);
			String tmpdir = ConfigurationUtil.getInstance().getTmpDir();
			if (null != tmpdir && !tmpdir.isEmpty()) {
				DataUtil.getInstance().makeDirectories(tmpdir + DataUtil.PATH_SEPARATOR);
				LogUtil.getInstance()
						.info("using temporary directory [" + tmpdir + "] for function [" + function + "]");
			}
			LogUtil.getInstance().info("using sourcedir [" + getSourcedir() + "] for function [" + function + "]");
			if (null != sourcefile && !sourcefile.isEmpty())
				LogUtil.getInstance().info("using sourcefile [" + sourcefile + "] for function [" + function + "]");
			if (!getSetting(args, ARG_MODE, MODE, function).equals(ARG_MODE_SERVER))
				dispatch(function, configuration, getSourcedir(), getTargetdir(), sourcefile, args, Option.CLIENT);
			else
				dispatch(function, configuration, getSourcedir(), getTargetdir(), sourcefile, args);
			outputdirs.add(new String[] { function, getTargetdir(), TimeUtil.getInstance().difff(funcstart) });
		}
		logOutput(outputdirs);
		LogUtil.getInstance().info("done, took " + TimeUtil.getInstance().difff(start) + " seconds. Enjoy your "
				+ TimeUtil.getInstance().getTimeOfDay().name().toLowerCase() + "!");

	}

	private void logOutput(List<String[]> outputdirs) {
		LogUtil.getInstance().info("Created the following output directories : ");
		for (String[] output : outputdirs) {
			LogUtil.getInstance().info("    " + output[0]
					+ "() took [" + output[2] + "] seconds -> [" + ((output[1].toLowerCase().startsWith("file:"))
							? output[1] : "<a href=\"" + convertToURL(output[1], output[0]))
					+ "\">" + output[1] + "</a>]");
		}
	}

	public String convertToURL(String filename, String function) {
		return StringUtil.getInstance().replace(filename, getSetting(DEST_DIR_FORMAT_URL, function));
	}

	public static void readRootConfiguration() {
		ConfigurationUtil.getInstance().clear();
		ConfigurationUtil.getInstance().init(Tool.class, "tool");
	}

	/**
	 * <pre>
	 * Configuration is either
	 * [a] defined => used for the configuration as in : 'tool.<configuration>'
	 * [b] null    => configuration is read from the 'configuration.profile' setting in the root-configuration (tool)
	 * </pre>
	 * 
	 * @param configuration
	 */
	public static void readConfiguration(String configuration) {
		String profile = null;
		if (null != configuration && !configuration.isEmpty() && !"undefined".equals(configuration)) {
			profile = configuration;
			LogUtil.getInstance().info("configuration provided [" + configuration + "]");
		} else {
			readRootConfiguration();
			profile = ConfigurationUtil.getInstance().getSetting(CONFIGURATION_PROFILE);
		}
		if (null != profile && !profile.isEmpty()) {
			ConfigurationUtil.getInstance().clear();
			ConfigurationUtil.getInstance().init(Tool.class, "tool." + profile);

			Properties props = new Properties();
			String jsonFile = ConfigurationUtil.getInstance().getTmpDir() + DataUtil.PATH_SEPARATOR + profile + ".json";
			try {
				props.load(Tool.class.getResourceAsStream(DataUtil.PATH_SEPARATOR + "tool." + profile + ".properties"));
				JSONObject jsonObject = JSONUtil.getInstance().properties2JSON(props);
				DataUtil.getInstance().writeToFile(jsonFile, JSONUtil.getInstance().writeJSON(jsonObject));
				LogUtil.getInstance().info("written JSON configuration to [" + jsonFile + "]");
			} catch (IOException e) {
				LogUtil.getInstance().info("could not write JSON configuration to [" + jsonFile + "]", e);
			}

		}
	}

	public void setTargetdir(String targetdir) {
		this.targetdir = targetdir;
	}

	public String getTargetdir() {
		return targetdir;
	}

	public void setSourcedir(String sourcedir) {
		this.sourcedir = sourcedir;
	}

	public String getSourcedir() {
		return sourcedir;
	}

	public abstract void dispatch(String function, String configuration, String sourcedir, String targetdir,
			String sourcefile, String[] args, Option... options);

	public void forEachDir(String sourcedir, Predicate<Graph<FileSystemNode, Edge<FileSystemNode>>> handlerDir) {
		forEachDir(sourcedir, null, handlerDir);
	}

	public void forEachDir(String sourcedir, String regexFilefilter,
			Predicate<Graph<FileSystemNode, Edge<FileSystemNode>>> handlerDir) {
		String[] dirs = sourcedir.split(",");
		for (String dir : dirs) {
			LogUtil.getInstance().info("Start looking for files in  folder [" + dir + "]"
					+ ((null != regexFilefilter) ? " using " + regexFilefilter : ""));
			Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(dir, regexFilefilter);

			handlerDir.test(fsg);

			LogUtil.getInstance().info("Stop looking for files in  folder [" + dir + "]");
		}
	}
}
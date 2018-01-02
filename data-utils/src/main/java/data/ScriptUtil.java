package data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Utility for external scripts
 * 
 * @author mwa17610
 * 
 */
public class ScriptUtil implements Util {
	public static final String PLACEHOLDER_FILENAME = "%%FILENAME%%";

	private static final ThreadLocal<ScriptUtil> instance = new ThreadLocal<ScriptUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ScriptUtil getInstance() {
		ScriptUtil result = instance.get();

		if (null == result) {
			result = new ScriptUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Execute a script. Use PLACEHOLDER_FILENAME to specify the location of the
	 * script file.
	 * 
	 * The commandline is specified by parameters.
	 * 
	 * @param antScriptContent
	 */
	public int executeScript(String script, String prefix, String suffix, String[] parameters, final int timeout) {
		File f = new File("unknown");
		String concattedParameters = "";
		int result = 0;
		try {
			f = File.createTempFile(prefix, suffix,
					new File(ConfigurationUtil.getInstance().getTmpDir()));
			DataUtil.getInstance().writeToFile(f.getPath(), script);
			List<String> params = new ArrayList<>();
			for (String param : parameters) {
				if (PLACEHOLDER_FILENAME.equals(param))
					params.add(f.getPath());
				else
					params.add(param);
				concattedParameters += param;
				concattedParameters += ":";
			}

			Process proc = null;
			Thread kt = null;
			try {
				proc = Runtime.getRuntime().exec(params.toArray(new String[] {}));
				if (timeout > 0) {
					// create a kill thread
					class KillThread implements Runnable {
						private int timeout;
						private Process proc;
						private File file;

						public KillThread(int timeout, Process proc, File file) {
							this.proc = proc;
							this.timeout = timeout;
							this.file = file;
						}

						@Override
						public void run() {
							try {
								Thread.sleep(this.timeout);
							} catch (InterruptedException e) {
							} catch (Exception e) {
								LogUtil.getInstance().log(getClass().getName(), Level.WARNING,
										"killed process [" + file.getName() + "] timeout [" + timeout + "]", e);
								proc.destroy();
							}
						}
					}
					kt = new Thread(new KillThread(timeout, proc, f));
					kt.start();
				}
				proc.waitFor();
				result = proc.exitValue();
			} finally {
				if (null != proc)
					proc.destroy();
				if (null != kt)
					kt.interrupt();
			}
		} catch (Exception ex) {
			LogUtil.getInstance().log(getClass().getName(), Level.WARNING,
					"error executing script on  [" + f.getName() + "] with parameters [" + concattedParameters + "]",
					ex);
		}
		return result;
	}

	/**
	 * Execute an ANT script. Assumes that ant.bat can be found in the PATH
	 * 
	 * @param antScriptContent
	 */
	public int executeAntScript(String antScriptContent) {
		return executeAntScript(antScriptContent, false);
	}

	/**
	 * Execute an ANT script. Assumes that ant.bat can be found in the PATH
	 * 
	 * @param antScriptContent
	 */
	public int executeAntScript(String antScriptContent, boolean wait) {
		return executeScript(antScriptContent, "build", ".xml",
				new String[] { "cmd.exe", "/c", "ant.bat", "-buildfile", PLACEHOLDER_FILENAME }, -1);
	}

	/**
	 * Execute script. Assumes that ant.bat can be found in the PATH
	 * 
	 * @param antScriptContent
	 */
	public int executeBatchScript(String script, String[] parameters) {
		return executeBatchScript(script, parameters, -1);
	}

	/**
	 * Execute script. Assumes that ant.bat can be found in the PATH
	 * 
	 * @param antScriptContent
	 */
	public int executeBatchScript(String script, String[] parameters, int timeout) {
		List<String> params = new ArrayList<>();

		params.add("cmd.exe");
		params.add("/c");
		params.add(PLACEHOLDER_FILENAME);
		if (null != parameters)
			for (String param : parameters)
				params.add(param);

		return executeScript(script, "batch", ".cmd", params.toArray(new String[] {}), timeout);
	}
}

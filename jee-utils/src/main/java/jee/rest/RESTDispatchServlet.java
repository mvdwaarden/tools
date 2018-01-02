package jee.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import csv.CSVData;
import csv.CSVUtil;
import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import data.StringUtil;
import jee.thread.ManagedThread;
import jee.thread.ThreadUtil;
import jee.thread.ThreadUtil.Option;
import json.JSONList;
import json.JSONObject;
import json.JSONUtil;
import object.ObjectIterator;
import object.ObjectUtil;

public abstract class RESTDispatchServlet extends RESTServlet {
	public static final String REST_SRV_CLEANUP_TIMEOUT = "rest.server.cleanup.timeout";
	public static final String REST_SRV_LOG_DIR = "rest.server.logdir";
	public static final String REST_SRV_MAX_WORKERS = "rest.server.maxworkers";
	public static final String REST_MANAGED_THREAD_FACTORY = "rest.server.managed.thread.factory";
	private static final long serialVersionUID = 4831364817779863550L;
	private static final String DEFAULT_MANAGED_THREAD_FACTORY = "DefaultManagedThreadFactory";
	public static final String REST_ARG_HANDLE = "handle";
	public static final String REST_ARG_CONFIG = "config";
	private static long handle;

	/**
	 * TODO make EJB out of workers ...
	 */
	private static final Map<String, Worker> workers = new ConcurrentHashMap<String, Worker>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handle(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handle(req, res);
	}

	protected DispatchContext getDispatchContext(String name) {
		DispatchContext result = new DispatchContext();
		result.setConfiguration(name);
		result.setMaxWorkers(
				ConfigurationUtil.getInstance().getIntegerSetting(REST_SRV_MAX_WORKERS + "." + name, 60000));
		result.setServerlogDir(ConfigurationUtil.getInstance().getSetting(REST_SRV_LOG_DIR + "." + name));
		result.setCleanupTimeout(
				ConfigurationUtil.getInstance().getIntegerSetting(REST_SRV_CLEANUP_TIMEOUT + "." + name, 60000));
		result.setMaxWorkers(
				ConfigurationUtil.getInstance().getIntegerSetting(REST_SRV_MAX_WORKERS + "." + name, 60000));

		return result;
	}

	protected List<DispatchEntry> getDispatchTable() {
		List<DispatchEntry> result = new ArrayList<>();

		for (Method m : ObjectIterator.lookupMethods(RESTDispatchServlet.class, getClass()).values()) {
			for (Annotation a : m.getAnnotationsByType(RESTMethod.class)) {
				RESTMethod ma = (RESTMethod) a;
				DispatchEntry de = new DispatchEntry(this, ma.URLPattern(), m.getName(), ma.dispatchType());
				result.add(de);
			}
		}
		return result;
	}

	private void handle(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String configuration = StringUtil.getInstance().getURLParameter(req.getParameterMap(), REST_ARG_CONFIG, null);
		DispatchContext ctxDispatch = getDispatchContext(configuration);
		ctxDispatch.setContextPath(req.getContextPath());

		DataUtil.getInstance().makeDirectories(
				ConfigurationUtil.getInstance().getSetting(REST_SRV_LOG_DIR + "." + ctxDispatch.getConfiguration())
						+ DataUtil.PATH_SEPARATOR);
		String tmpdir = ConfigurationUtil.getInstance().getTmpDir();
		if (null != tmpdir && !tmpdir.isEmpty()) {
			DataUtil.getInstance().makeDirectories(tmpdir + DataUtil.PATH_SEPARATOR);
			LogUtil.getInstance().info("using temporary directory [" + tmpdir + "]");
		}
		String contextPath = req.getPathInfo();
		logHeaders(req);

		for (DispatchEntry de : getDispatchTable()) {
			if (contextPath.matches(de.getPath())) {
				Object result = null;
				switch (de.getType()) {
				case ASYNCH:
					result = invokeAsynch(de.getDispatchTarget(), de.method, ctxDispatch, req, res);
					break;
				case SYNCH:
					result = invokeSynch(de.getDispatchTarget(), de.method, ctxDispatch, req, res);
					break;
				}
				// afhandelen van verschillende return types
				if (null != result) {
					// een input stream (document)
					if (result instanceof InputStream) {
						res.setContentType("application/octet-stream");
						DataUtil.getInstance().copy((InputStream) result, res.getOutputStream());
					} else if (result instanceof CSVData) {
						// CSV gegevens
						res.setContentType("text/csv");
						CSVUtil.getInstance().writeToCSVStream(res.getOutputStream(), (CSVData) result, ';');
					} else {
						// een object
						boolean isXML = false;
						// automatic content type determination, json/xml wordt
						// in URL ondersteund
						if (null == res.getContentType()) {
							if (ctxDispatch.getContextPath().endsWith("xml")) {
								res.setContentType("application/xml");
								isXML = true;
							} else
								res.setContentType("application/json");
						}
						String resultString = "";
						if (res.getContentType().equals("application/json")) {
							// is het return type een string
							if (result instanceof String) {
								resultString = "{\"list\" : " + (String) result + "}";
							} else {
								// anders is het return type een object
								JSONObject jsonObject = JSONUtil.getInstance().java2JSON(result);
								if (isXML)
									resultString = JSONUtil.getInstance().writeXML(jsonObject, "result");
								else {
									resultString = JSONUtil.getInstance().writeJSON(jsonObject);
									if (jsonObject instanceof JSONList) {
										resultString = "{\"list\" : " + resultString + "}";
									}
								}
							}
						} else if (result instanceof String) {
							resultString = (String) result;
						}
						DataUtil.getInstance().writeToOutputStream(res.getOutputStream(), resultString, getCharset());
					}
				}
				break;
			} else if (contextPath.equals("/status")) {
				status(ctxDispatch, req, res);
				break;
			}
		}
	}

	protected <T> T invokeSynch(Object object, String method, DispatchContext ctxDispatch, HttpServletRequest req,
			HttpServletResponse res) {

		T result = ObjectUtil.getInstance().<T>invoke((null == object) ? this : object, method,
				new Class[] { DispatchContext.class, HttpServletRequest.class, HttpServletResponse.class },
				new Object[] { ctxDispatch, req, res });

		return result;
	}

	protected String getParameter(HttpServletRequest req, String paramArg) {
		String result = StringUtil.getInstance().getURLParameter(req.getParameterMap(), paramArg, null);

		return result;
	}

	protected Parameters cloneParameterMap(HttpServletRequest req) {
		Parameters result = new Parameters();

		for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
			result.put(e.getKey(), e.getValue());
		}

		return result;
	}

	protected Handle invokeAsynch(Object object, String method, DispatchContext ctxDispatch, HttpServletRequest req,
			HttpServletResponse res) {
		Handle result;

		if (workers.size() <= ctxDispatch.getMaxWorkers()) {
			// haal alle URL parameters op, die zijn in de verschillende
			// threads n.l. niet meer geldig
			long handle = ++RESTDispatchServlet.handle;
			final Worker worker = new Worker();
			Parameters parameters = cloneParameterMap(req);
			worker.worker = ThreadUtil.getInstance().createManagedThread(ConfigurationUtil.getInstance()
					.getSetting(REST_MANAGED_THREAD_FACTORY, DEFAULT_MANAGED_THREAD_FACTORY), () -> {
						// dit zorgt ervoor dat de logging die in deze thread
						// gebeurt, naar de worker gaat. Mede omdat
						// LogUtil.getInstance() wordt gebruikt!
						LogUtil.getInstance().registerCallback(
								(String context, String level, String text) -> worker.logging.log.add(text));
						ObjectUtil.getInstance().invoke((null == object) ? this : object, method,
								new Class[] { DispatchContext.class, Parameters.class },
								new Object[] { ctxDispatch, parameters });
					}, Option.INHERIT_UTILS);
			// zorg ervoor dat de worker via de handle kan worden gevonden in
			// workers
			workers.put(Long.toString(handle), worker);
			// zorg dat de worker wordt verwijdert uit de lijst indien hij klaar
			// is.
			worker.worker.registerFinishedCallback(thr -> {
				// ruim deze worker 'lazy' op (m.a.w. in een andere thread)!
				ThreadUtil.getInstance().createManagedThread(ConfigurationUtil.getInstance()
						.getSetting(REST_MANAGED_THREAD_FACTORY, DEFAULT_MANAGED_THREAD_FACTORY), () -> {
							try {
								ThreadUtil.getInstance().sleep(ctxDispatch.getCleanupTimeout());
							} catch (InterruptedException e) {
								LogUtil.getInstance().warning("thread interrupted");
							}
							cleanupWorker(ctxDispatch, String.valueOf(handle));
						}).start();
			});
			worker.worker.start();
			result = new Handle(handle);
		} else {
			result = new Handle(-1);
			result.message = "unable to create worker, maxworkers[" + ctxDispatch.getMaxWorkers() + "] reached";
		}
		return result;
	}

	protected void status(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) throws IOException {
		String handle = StringUtil.getInstance().getURLParameter(req.getParameterMap(), REST_ARG_HANDLE, null);
		if (null != handle) {
			Worker worker = workers.get(handle);
			if (null != worker) {
				res.setContentType("application/json");
				DataUtil.getInstance().writeToOutputStream(res.getOutputStream(), getWorkerLog(worker.logging),
						getCharset());
			} else {
				res.setContentType("application/json");
				String jsonStr = getLogForHandle(cfg, handle);
				DataUtil.getInstance().writeToOutputStream(res.getOutputStream(), jsonStr, getCharset());
			}
		}
	}

	private void logHeaders(HttpServletRequest req) {
		Enumeration<String> headers = req.getHeaderNames();

		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			LogUtil.getInstance().debug("req-header[" + header + "] = " + req.getHeader(header));
		}
	}

	private void cleanupWorker(DispatchContext cfg, String handle) {
		Worker worker = workers.get(handle);
		persistWorkerLog(cfg, handle, worker.logging);
		workers.remove(handle);
		LogUtil.getInstance().info("cleanup information for worker with handle [" + handle + "]");
	}

	class Worker {
		ManagedThread worker;
		Object result;
		Logging logging = new Logging();

	}

	public void persistWorkerLog(DispatchContext cfg, String handle, Logging logging) {
		String jsonStr = getWorkerLog(logging);
		DataUtil.getInstance().writeToFile(getWorkerLogFilename(cfg, handle), jsonStr);
	}

	public String getWorkerLog(Logging logging) {
		JSONObject jsonObject = JSONUtil.getInstance().java2JSON(logging);
		String jsonStr = JSONUtil.getInstance().writeJSON(jsonObject);

		return jsonStr;
	}

	private String getWorkerLogFilename(DispatchContext cfg, String handle) {
		return cfg.getServerlogDir() + DataUtil.PATH_SEPARATOR + handle + ".log.json";
	}

	public String getLogForHandle(DispatchContext cfg, String handle) {
		return DataUtil.getInstance().readFromFile(getWorkerLogFilename(cfg, handle));
	}

	public class Handle {
		public Handle(long handle) {
			this.handle = handle;
		}

		long handle;
		String message;
	}

	public class Logging {
		List<String> log = new ArrayList<>();
	}

	public class DispatchContext {
		private String serverlogDir;
		private int cleanupTimeout;
		private int maxWorkers;
		private String configuration;
		private String contextPath;
		private String dispatchEntry;

		public String getServerlogDir() {
			return serverlogDir;
		}

		public void setConfiguration(String configuration) {
			this.configuration = configuration;

		}

		public String getConfiguration() {
			return configuration;
		}

		public void setServerlogDir(String serverlogDir) {
			this.serverlogDir = serverlogDir;
		}

		public int getCleanupTimeout() {
			return cleanupTimeout;
		}

		public void setCleanupTimeout(int cleanupTimeout) {
			this.cleanupTimeout = cleanupTimeout;
		}

		public int getMaxWorkers() {
			return maxWorkers;
		}

		public void setMaxWorkers(int maxWorkers) {
			this.maxWorkers = maxWorkers;
		}

		public String getContextPath() {
			return contextPath;
		}

		public void setContextPath(String contextPath) {
			this.contextPath = contextPath;
		}

		public String getDispatchEntry() {
			return dispatchEntry;
		}

		public void setDispatchEntry(String dispatchEntry) {
			this.dispatchEntry = dispatchEntry;
		}
	}

	public enum DispatchType {
		SYNCH, ASYNCH;
	}

	public class DispatchEntry {
		private String path;
		private Object dispatchTarget;
		private String method;
		private DispatchType dispatchType;

		public DispatchEntry(String path, String method, DispatchType dispatchType) {
			this(null, path, method, dispatchType);
		}

		public DispatchEntry(Object dispatchTarget, String path, String method, DispatchType dispatchType) {
			this.path = path;
			this.dispatchTarget = dispatchTarget;
			this.method = method;
			this.dispatchType = dispatchType;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public Object getDispatchTarget() {
			return dispatchTarget;
		}

		public void setDispatchTarget(Object dispatchTarget) {
			this.dispatchTarget = dispatchTarget;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public DispatchType getType() {
			return dispatchType;
		}

		public void setType(DispatchType type) {
			this.dispatchType = type;
		}
	}

	public class Parameters {
		private Map<String, String[]> parameters;

		public void put(String key, String[] value) {
			init();
			parameters.put(key, value);
		}

		public String[] get(String key) {
			init();
			return parameters.get(key);
		}

		protected void init() {
			if (null == this.parameters)
				this.parameters = new HashMap<>();
		}
	}

}

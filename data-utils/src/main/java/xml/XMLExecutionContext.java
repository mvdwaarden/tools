package xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ConfigurationUtil;
import data.DataUtil;
import data.Filter;
import data.LogUtil;
import data.StringUtil;
import xml.XMLTransformer.ScriptType;

/**
 * <pre>
 * Purpose : XMLExecutionContext
 *         
 * Version history: 
 * 30/03/2015 MWA creation
 * </pre>
 * 
 * @author mwa17610
 * @since 30/03/2015
 */
public class XMLExecutionContext implements ExecutionContext {
	/* data : input stream for the current step */
	private InputStream input;
	/* filename : filename which relates the the current step. */
	private String filename;
	/* step : the current step name. Also the last executed correct step */
	private String step;
	/* the result of the execution context chain */
	private boolean ok;
	/* nfoListener : report listener */
	private ReportListener lsnrReport;
	/*
	 * namespaces : namespace map to be used by assertion methods (e.g.
	 * assertTrue())
	 */
	private Map<String, String> namespaces;
	/* failedAssertions : failed assertions */
	private List<String> failedAssertions;

	public XMLExecutionContext() {
		this(new ReportListener() {

			@Override
			public void report(Class<?> cls, String step, String msg) {
				LogUtil.getInstance().info(step + ":" + msg);
			}
		});
	}

	public XMLExecutionContext(ReportListener lsnrReport) {
		init(lsnrReport);

	}

	private void init(ReportListener lsnrReport) {
		this.lsnrReport = lsnrReport;
		this.ok = true;
		this.namespaces = new HashMap<>();
		this.failedAssertions = new ArrayList<>();
	}

	@Override
	public ExecutionContext reset() {
		init(this.lsnrReport);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext transform(String step, String script, ScriptType type) {
		if (proceed(step)) {
			XMLTransformer transformer = createTransformer(type);

			ok = false;
			try {
				File tmp = File.createTempFile("tmp", "_xslt_result",
						new File(ConfigurationUtil.getInstance().getTmpDir()));
				try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
						InputStream xslt = new BufferedInputStream(new FileInputStream(script));) {
					transformer.transform(new XMLBinder(input), os, xslt);
					os.close();
					input.close();
					ok = true;
					load(tmp.getPath());
				} catch (Exception e) {
					LogUtil.getInstance().warning("problem transforming ", e);
				}
			} catch (Exception e) {
				LogUtil.getInstance().warning("problem transforming ", e);
			}
		}

		return this;
	}

	private XMLTransformer createTransformer(ScriptType type) {
		XMLTransformer result = null;
		;
		switch (type) {
		case XQUERY:
			result = new XQueryTransformer();
			break;
		case XSLT:
			result = new XSLTTransformer();
			break;

		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext transform(String step, XMLBinder binder, String script, XMLTransformer.ScriptType type) {
		if (proceed(step)) {
			XMLTransformer transformer = createTransformer(type);
			ok = false;
			try {
				File tmp = File.createTempFile("tmp", "_xslt_result",
						new File(ConfigurationUtil.getInstance().getTmpDir()));

				OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
				InputStream xslt = new BufferedInputStream(new FileInputStream(script));
				/*
				 * set the input to the binding which has the last result, don't
				 * break if found
				 */
				for (XMLBinding bind : binder.getBindings()) {
					if (bind.getValueType() == XMLBinding.BindingValueType.STRING
							&& bind.getValue().equals(LAST_RESULT))
						bind.setValue(input);
				}
				ok = transformer.transform(binder, os, xslt);
				os.close();
				input.close();
				load(tmp.getPath());
			} catch (Exception e) {
				LogUtil.getInstance().warning("problem transforming ", e);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext validate(String step, String xsdFilename) {
		if (proceed(step)) {
			List<Exception> errors = XMLUtil.getInstance().validate(xsdFilename, filename);

			for (Exception e : errors)
				report("[" + filename + "] -> " + e.toString());

			ok = errors.isEmpty();
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext load(String step, String sourceFilename) {
		if (proceed(step)) {
			ok = load(sourceFilename);

		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext load(String step, String dirname, Filter<File> filter) {
		if (proceed(step)) {
			File dir = new File(dirname);
			File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return filter.include(file);
				}
			});
			try {
				File tmp = File.createTempFile("tmp", "_dir_context",
						new File(ConfigurationUtil.getInstance().getTmpDir()));
				try (FileOutputStream fos = new FileOutputStream(tmp)) {
					// @TODO opruimen, old skool xml output ziet niet uit
					// natuurlijk
					StringBuilder out = new StringBuilder();
					out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					out.append("    <context xmlns=\"http://xml/ExecutionContext\">");
					if (null != files) {
						for (File file : files) {
							out.append("        <file>");
							out.append("            <name>" + DataUtil.getInstance().getFilename(file.getPath())
									+ "</name>");
							out.append("            <extension>"
									+ DataUtil.getInstance().getFileExtension(file.getPath()) + "</extension>");
							out.append("            <folder>" + DataUtil.getInstance().getFoldername(file.getPath())
									+ "</folder>");
							out.append("            <absoluteName>"
									+ DataUtil.getInstance().simplifyFolder(file.getPath()) + "</absoluteName>");
							out.append("        </file>");
						}
					}
					out.append("    </context>");
					fos.write(out.toString().getBytes());
					fos.close();
				}
				load(tmp.getPath());
			} catch (IOException e) {
				LogUtil.getInstance().error("problem writing directory context", e);
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext copy(String step, String targetFilename) {
		if (proceed(step)) {
			ok = false;
			try (FileInputStream from = new FileInputStream(filename);
					FileOutputStream to = new FileOutputStream(targetFilename);) {
				DataUtil.getInstance().copy(from, to);
				ok = load(targetFilename);
			} catch (IOException e) {
				LogUtil.getInstance().error("could copy [" + filename + "] to [" + targetFilename + "]", e);
			}
			ok = true;
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOk() {
		return ok;
	}

	@Override
	public ExecutionContext subst(String step, String replaceConfig) {
		if (proceed(step)) {
			ok = false;
			try {
				File tmp = File.createTempFile("tmp", "_subst_result",
						new File(ConfigurationUtil.getInstance().getTmpDir()));
				try (OutputStream os = new FileOutputStream(tmp)) {
					XMLUtil.getInstance().parse(input, new XMLSAXEchoHandler(os) {
						@Override
						public void writeCharacters(String data) {
							String replacedData = StringUtil.getInstance().replace(data, replaceConfig);
							super.writeCharacters(replacedData);
						}
					});
					os.close();
					input.close();
					ok = true;
					load(tmp.getPath());
				} catch (Exception ex) {
					LogUtil.getInstance().error("problem substituting file");
				}
			} catch (Exception e) {
				LogUtil.getInstance().warning("problem substituting ", e);

			}
		}
		return this;
	}

	@Override
	public ExecutionContext combine(String step, String sourcedir, String regexFileFilter, String targetfile,
			String namespace, String prefix, String roottag) {
		if (proceed(step)) {
			String[] dirs = sourcedir.split(",");

			try (OutputStream os = new FileOutputStream(targetfile)) {
				os.write(("<" + prefix + ":" + roottag + " xmlns:" + prefix + "=\"" + namespace + "\">").getBytes());
				for (String dir : dirs) {
					File filedir = new File(dir);

					for (File file : filedir
							.listFiles((pathname) -> (pathname.getAbsolutePath().matches(regexFileFilter)))) {
						try (FileInputStream fis = new FileInputStream(file.getAbsolutePath())) {
							XMLUtil.getInstance().parse(fis, new XMLSAXEchoHandler(os) {
								@Override
								public void startDocument() {
									superStartDocument();
								};
							});
						} catch (Exception ex) {
							LogUtil.getInstance().error("problem reading output file [" + file.getAbsolutePath() + "]");
						}
					}
				}
				os.write(("</" + prefix + ":" + roottag + ">").getBytes());
			} catch (Exception ex) {
				ok = false;
				LogUtil.getInstance().error("problem creating output file");
			}
		}
		return this;
	}

	/**
	 * Handle a report call
	 * 
	 * @param msg
	 *            message to report
	 */
	public void report(String msg) {
		lsnrReport.report(getClass(), getStep(), msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFilename() {
		return filename;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStep() {
		return step;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setStep(String step) {
		this.step = step;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext assertTrue(String step, String xpath, String value) {
		if (proceed(step)) {
			ok = assertTrue(filename, step, xpath, value);
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext assertTrue(String step, String[][] assertions) {
		if (proceed(step)) {
			ok = true;
			// perform ALL assertions
			for (String[] assertion : assertions) {
				boolean result = assertTrue(filename, step, assertion[0], assertion[1]);

				if (!result)
					ok = false;
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext registerNamespace(String step, String prefix, String uri) {
		namespaces.put(prefix, uri);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext registerNamespaces(String step, String[][] namespaces) {
		for (String[] ns : namespaces)
			registerNamespace(step, ns[0], ns[1]);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext unregisterNamespace(String step, String prefix) {
		namespaces.remove(prefix);

		return this;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionContext custom(String step, CustomMethod custom) {
		if (proceed(step)) {
			custom.execute(this);
			ok = custom.isOk();
		}

		return custom.getExecutionContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getFailedAssertions() {
		return failedAssertions;
	}

	/**
	 * Initializes the input stream to be used by the next step.
	 * 
	 * @param sourceFilename
	 * @return
	 */
	private boolean load(String sourceFilename) {
		boolean result = false;

		try {
			input = new BufferedInputStream(new FileInputStream(sourceFilename));
			filename = sourceFilename;
			result = true;
		} catch (FileNotFoundException e) {
			LogUtil.getInstance().error("could not load [" + sourceFilename + "]", e);
		}

		return result;
	}

	/**
	 * Checks if the current operation may be executed.
	 * 
	 * @param step
	 * @return
	 */
	private boolean proceed(String step) {
		if (ok)
			setStep(step);

		return ok;

	}

	/**
	 * assert
	 */
	private boolean assertTrue(String step, String filename, String xpath, String value) {
		boolean assertResult = false;

		String content = "";
		try (InputStream is = new FileInputStream(filename)) {
			content = XMLUtil.getInstance().evaluate(is, xpath, namespaces);
			if (value.equals(content))
				assertResult = true;
		} catch (Exception e) {
			LogUtil.getInstance().ignore("assertion failed", e);
			assertResult = false;
		}
		if (!assertResult) {
			String nfoAssert = "assertFailed on [" + getFilename() + "] -> [" + xpath + ": " + value + " <> " + content
					+ "] (possibly check xpath)";
			lsnrReport.report(getClass(), step, nfoAssert);
			failedAssertions.add("[" + step + "] " + nfoAssert);
		}

		return assertResult;
	}
}

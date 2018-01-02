package nl.ordina.tools.soa.osb.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comm.StreamHandler;
import comm.TCPIPServer;
import csv.CSVData;
import csv.CSVUtil;
import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;

public class OSBTestService extends OSBTestBase implements OSBTestConst {
	public static final String MIME_TYPES_FILENAME = "mime-types.csv";
	public static final String MIME_TYPES_PROPERTY = "mime.types.filename";

	CSVData mimetypes;

	/**
	 * Get a mime-type map
	 */
	public List<String[]> getMimeTypes() {
		if (null == mimetypes)
			mimetypes = CSVUtil.getInstance().readFromFile(MIME_TYPES_FILENAME, ';');
		return mimetypes.getLines();

	}

	/**
	 * Get the mimetype basesd on the mimetype CSV
	 * 
	 * @param filename
	 * @return
	 */
	public String getMimeType(String filename) {
		String result = "text/plain";

		for (String[] mimetype : getMimeTypes()) {
			if (mimetype[2].equalsIgnoreCase("." + DataUtil.getInstance().getFileExtension(filename))) {
				result = mimetype[1];
				break;
			}
		}

		return result;
	}

	private TCPIPServer service;

	public void init() {
		readConfig();
		service = new TCPIPServer(ConfigurationUtil.getInstance().getSetting(OSBTestConst.OSB_TEST_BIND, "localhost"),
				ConfigurationUtil.getInstance().getIntegerSetting(OSBTestConst.OSB_TEST_PORT, 12345)) {
			@Override
			public StreamHandler createStreamHandler() {
				return new StreamHandler() {
					@Override
					public void handle(InputStream is, OutputStream os) {
						readConfig();
						try {
							Map<String, String> headers = getHeaders(is);
							slow(is, os, headers);
						} finally {
							// deferred close
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep(50);
									} catch (InterruptedException e) {
										// ignore
									}
									DataUtil.getInstance().close(is);
									DataUtil.getInstance().close(os);
								}
							}).start();
						}
					}
				};
			}
		};
	}

	public Map<String, String> getHeaders(InputStream is) {
		Map<String, String> headers = new HashMap<>();
		try {
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String line = ir.readLine();
			String resource = null;
			while (null != line && !line.isEmpty()) {
				if ((line.startsWith(OSBTestConst.HTTP_METHOD_GET) || line.startsWith(OSBTestConst.HTTP_METHOD_POST))
						&& line.endsWith("HTTP/1.1")) {
					String method = (line.startsWith(OSBTestConst.HTTP_METHOD_GET)) ? OSBTestConst.HTTP_METHOD_GET
							: OSBTestConst.HTTP_METHOD_POST;
					resource = line.substring(method.length(), line.length() - "HTTP/1.1".length()).trim();
					headers.put(OSBTestConst.HTTP_HEADER_URL, resource);
				} else {
					int idx = line.indexOf(":");
					if (idx > 0) {
						String key = line.substring(0, idx).trim();
						String value;
						if (idx < line.length())
							value = line.substring(idx + 1, line.length()).trim();
						else
							value = "";

						headers.put(key, value);
					}
				}
				LogUtil.getInstance().info(line);
				line = ir.readLine();
			}
			String function = "";
			String item = "";
			if (null != resource) {
				String[] el = resource.split(HTTP_PATH_SEPARATOR);
				function = el[el.length - 2];
				item = el[el.length - 1];
				headers.put(OSBTestConst.HTTP_HEADER_FUNCTION, function);
				int idx = item.indexOf("?");
				if (idx > 0)
					item = item.substring(0, idx);
				headers.put(HTTP_HEADER_ITEM, item);
				LogUtil.getInstance().info(resource + "[" + item + "]");
			} else {
				LogUtil.getInstance().info("invalid token");
				function = "invalid";
			}
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to process headers", e);
		}

		return headers;
	}

	public void slow(InputStream is, OutputStream os, Map<String, String> headers) {
		String function = headers.get(OSBTestConst.HTTP_HEADER_FUNCTION);
		String item = headers.get(OSBTestConst.HTTP_HEADER_ITEM);
		// int length = 0;
		//
		// try {
		// length =
		// Integer.parseInt(headers.get(OSBTestConst.HTTP_HEADER_CONTENT_LENGTH));
		// } catch (NumberFormatException | NullPointerException e) {
		// LogUtil.getInstance().warning(
		// "problem reading the length from HTTP header[" +
		// OSBTestConst.HTTP_HEADER_CONTENT_LENGTH + "]");
		// }
		int delay = ConfigurationUtil.getInstance().getIntegerSetting(
				OSBTestConst.OSB_RESPONSE_DELAY.replace(OSBTestConst.FUNCTION_PLACEHOLDER, function), 1);
		int readTimeout = ConfigurationUtil.getInstance().getIntegerSetting(
				OSBTestConst.OSB_RESPONSE_READ_TIME_OUT.replace(OSBTestConst.FUNCTION_PLACEHOLDER, function), 1);
		int blocks = 1;
		if (readTimeout > 0) {
			blocks = delay / readTimeout;
			if (blocks <= 0)
				blocks = 1;
		}
		try {
			String path = getResponsePath();
			String filename = path + item;
			String response = DataUtil.getInstance().readFromFile(path + item + ".xml");
			// write the HTTP headers
			String header = "HTTP/1.1 200 OK\n";
			os.write(header.getBytes());
			header = OSBTestConst.HTTP_HEADER_CONTENT_TYPE + ": " + getMimeType(filename) + "; charset=utf-8\n";
			os.write(header.getBytes());
			header = OSBTestConst.HTTP_HEADER_CONTENT_LENGTH + ": " + response.length() + "\n";
			os.write(header.getBytes());
			os.write("\n".getBytes());

			byte[] buf = response.getBytes();
			int blocksize = buf.length / blocks;
			for (int i = 0; i < blocks; ++i) {
				os.write(buf, i * blocksize, blocksize);
				if (blocks > 1)
					try {
						Thread.sleep(delay * 1000 / blocks);
					} catch (InterruptedException e) {
						LogUtil.getInstance().warning("interrupted delay", e);
					}
			}
			if (buf.length % blocks != 0)
				os.write(buf, blocks * blocksize, buf.length - blocks * blocksize);
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to write outputstream", e);
		}
	}

	public void start() {
		LogUtil.getInstance().info("Using response path [" + getResponsePath() + "]");
		service.start();
	}

	public static void main(String[] args) {
		OSBTestService svc = new OSBTestService();

		svc.init();
		svc.start();
	}

	public String getResponsePath() {
		return ConfigurationUtil.getInstance().getSetting(OSBTestConst.OSB_RESPONSE_PATH, "d:/tmp/");
	}

}

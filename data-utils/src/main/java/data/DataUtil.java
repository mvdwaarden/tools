package data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Purpose: Utility class for graph package
 * 
 * @author mwa17610
 * 
 */
public class DataUtil implements Util {
	public static final String URL_PROTOCOL_REPLACE = "url.protocol.replace";
	public static final String PATH_SEPARATOR = "/";
	public static final String FILE_ENCODING = "file.encoding";
	public static final String TMP_DIR = "tmp.dir";
	private static final ThreadLocal<DataUtil> instance = new ThreadLocal<DataUtil>();
	private int uuid;

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static DataUtil getInstance() {
		DataUtil result = instance.get();

		if (null == result) {
			result = new DataUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * <pre>
	 * Converts a filename to a file URL 
	 * Examples: 
	 * - c:/myfile.txt => file:///c:/myfile.txt 
	 * - c:\myfile.txt => file:///c:/myfile.txt 
	 * - /myfile.txt   => file:///myfile.txt
	 * </pre>
	 * 
	 * @param filename
	 * @return
	 */
	public String convertToFileURL(String filename) {
		String result = filename;
		if (!filename.toLowerCase().startsWith("file://")) {
			String path = new File(filename).getAbsolutePath();
			if (File.separatorChar != DataUtil.PATH_SEPARATOR.charAt(0)) {
				path = path.replace(File.separatorChar, DataUtil.PATH_SEPARATOR.charAt(0));
			}

			if (!path.startsWith(PATH_SEPARATOR)) {
				path = PATH_SEPARATOR + path;
			}
			result = "file://" + path;
		}
		return result;
	}

	/**
	 * Remove the file extension from the filename
	 * 
	 * @param filename
	 * @return
	 */
	public String removeExtension(String filename) {
		String result = filename;

		int idx = result.lastIndexOf(".");

		if (idx > 0)
			result = result.substring(0, idx);

		return result;
	}

	/**
	 * Returns the file extension
	 * 
	 * @param filename
	 * @return
	 */
	public String getFileExtension(String filename) {
		return StringUtil.getInstance().getLastPart(filename, ".");
	}

	/**
	 * Get the filename part of an absolute filename without the extension
	 * 
	 * @return
	 */
	public String getFilenameWithoutExtension(String filename) {
		return removeExtension(getFilename(filename));
	}

	/**
	 * Merge two character arrays
	 * 
	 * @param sizes
	 * @param arrays
	 * @return
	 */
	public byte[] mergeArrays(int[] sizes, byte[][] arrays) {
		byte[] result;

		int totalsize = 0;
		for (int i = 0; i < sizes.length; ++i) {
			totalsize += sizes[i];
		}
		result = new byte[totalsize];
		int idx = 0;
		for (int i = 0; i < arrays.length; ++i) {
			System.arraycopy(arrays[i], 0, result, idx, sizes[i]);
			idx += sizes[i];
		}

		return result;
	}

	/**
	 * Read the contents of a file
	 * 
	 * @param filename
	 */
	public String readFromFile(String filename) {
		return readFromFile(filename, null);
	}

	/**
	 * Read the contents of a file
	 * 
	 * @param filename
	 * @param charset
	 */
	public String readFromFile(String filename, String charset) {
		StringBuilder result = new StringBuilder();

		readBlocksFromFile(filename, 10000,
				(blocknr, blocksize, currentReadBytes, currentBlock, readbytes, blockswindow) -> {
					boolean ok = true;
					if (null == charset)
						result.append(new String(currentBlock, 0, currentReadBytes));
					else
						try {
							result.append(new String(currentBlock, 0, currentReadBytes, charset));
						} catch (UnsupportedEncodingException e) {
							ok = false;
							LogUtil.getInstance().error("unsupported encoding [" + charset + "]", e);
						}
					return ok;
				});

		return result.toString();
	}

	/**
	 * Read blocks from file
	 * 
	 * @param filename
	 * @param blocksize
	 * @param cb
	 */
	public void readBlocksFromFile(String filename, int blocksize, BlockReaderCallback cb) {
		FileInputStream fis = null;
		int blocknr = 0;

		try {
			fis = new FileInputStream(filename);
			byte[] buf1 = new byte[blocksize];
			byte[] buf2 = new byte[blocksize];
			byte[][] bufs = new byte[2][];
			bufs[0] = buf1;
			bufs[1] = buf2;
			int[] readbytesInfo = new int[2];
			boolean ok = false;

			while (0 < (readbytesInfo[0] = fis.read(bufs[0]))) {
				byte[] ctmp;
				ctmp = bufs[0];
				bufs[0] = bufs[1];
				bufs[1] = ctmp;
				int itmp = readbytesInfo[0];
				readbytesInfo[0] = readbytesInfo[1];
				readbytesInfo[1] = itmp;

				ok = cb.readBlock(blocknr++, blocksize, readbytesInfo[1], bufs[1], readbytesInfo, bufs);
				if (!ok)
					break;
			}
		} catch (Exception e) {
			LogUtil.getInstance().error("problem reading file [" + filename + "]", e);
		} finally {
			if (null != fis)
				try {
					fis.close();
				} catch (IOException e) {
					LogUtil.getInstance().warning("unable to close file [" + filename + "]", e);
				}
		}
	}

	/**
	 * Read the contents of an input stream
	 * 
	 * @param filename
	 */
	public String readFromInputStream(InputStream is, Charset charset) {
		StringBuilder result = new StringBuilder();

		try {
			byte[] cbuf = new byte[100000];
			int readbytes;

			while (0 < (readbytes = is.read(cbuf)))
				if (null != charset)
					result.append(new String(cbuf, 0, readbytes, charset));
				else
					result.append(new String(cbuf, 0, readbytes));

		} catch (IOException e) {
			LogUtil.getInstance().error("problem reading from stream", e);
		}

		return result.toString();
	}

	/**
	 * Write the contents of a string to a file
	 * 
	 * @param filename
	 * @param data
	 */
	public void writeToOutputStream(OutputStream os, String data, Charset charset) {
		try {
			if (null == charset)
				os.write(data.getBytes());
			else
				os.write(data.getBytes(charset));
		} catch (IOException e) {
			LogUtil.getInstance().error("problem writing to output stream", e);
		}
	}

	/**
	 * Write the contents of a string to a file
	 * 
	 * @param filename
	 * @param data
	 * @param charset
	 */
	public void writeToFile(String filename, String data, String charset) {

		try (BufferedOutputStream of = new BufferedOutputStream(new FileOutputStream(filename))) {
			if (null == charset)
				of.write(data.getBytes());
			else
				of.write(data.getBytes(charset));
		} catch (IOException e) {
			LogUtil.getInstance().error("problem writing to file[" + filename + "]", e);
		}
	}

	/**
	 * Write the contents of a string to a file
	 * 
	 * @param filename
	 * @param data
	 */
	public void writeToFile(String filename, String data) {
		writeToFile(filename, data, (String) null);
	}

	/**
	 * Returns the foldername part of a path
	 * 
	 * @param path
	 * @return
	 */
	public String getFoldername(String path) {
		int idx = path.lastIndexOf(DataUtil.PATH_SEPARATOR);
		if (idx <= 0)
			idx = path.lastIndexOf("/");
		if (idx <= 0)
			idx = path.lastIndexOf("\\");
		String result;
		if (idx >= 0)
			result = path.substring(0, idx);
		else
			result = path;

		return result;
	}

	public String stripProtocol(String url) {
		String result = url;
		String[] protocols = new String[] { "http:", "file:" };

		for (String protocol : protocols) {
			if (result.startsWith(protocol)) {
				result = result.substring(protocol.length());
				result = result.replace("\\", "/");
				while (result.startsWith("/"))
					result = result.substring(1);
				break;
			}
		}

		return result;
	}

	/**
	 * Simplifies a folder name
	 * 
	 * <pre>
	 * 	Examples
	 * 	d://a/b/../../c => d:/c
	 *  file://a/b/c/../d => file://a/b/d
	 *  file://c:\a\b\c => file://c:/a/b/c
	 *  http://whatever.com/a/c/../q/r => http://whatever.com/a/q/r
	 *  file://////a///b/c/..\\d/e => file://a/b/d/e
	 *  file://////a/././b/./c/..\\\\d => file://a/b/d
	 * </pre>
	 */
	public String simplifyFolder(String folder) {
		String result = "";
		String protocol = "";
		String drive = "";
		boolean absolute = false;

		String tmpFolder = folder.replace('\\', '/');
		// check if this is an absolute path
		if (folder.length() > 0 && folder.charAt(0) == '/') 
			absolute = true;
		// check if the path contains a protocol specification
		int idx = tmpFolder.indexOf("//");
		if (idx >= 1 && tmpFolder.charAt(idx - 1) == ':') {
			protocol = tmpFolder.substring(0, idx - 1);
			if (tmpFolder.length() > idx + 2)
				tmpFolder = tmpFolder.substring(idx + 2);
			else
				tmpFolder = "";
		}
		// check if the remaining path contains a drive specification
		idx = tmpFolder.indexOf(":");
		if (idx > 0) {
			drive = tmpFolder.substring(0, idx);
			if (tmpFolder.length() > idx)
				tmpFolder = tmpFolder.substring(idx + 1);
			else
				tmpFolder = "";
		} else
			drive = "";
		// check if we have something left, after path specification
		if (!tmpFolder.isEmpty()) {
			String[] path = tmpFolder.split("/");
			Stack<String> corrected = new Stack<>();
			for (String el : path) {
				if (el.equals("..")) {
					if (!corrected.isEmpty())
						corrected.pop();
				} else if (el.equals(".")) {
				} else if (!el.isEmpty())
					corrected.push(el);

			}
			if (!corrected.isEmpty()) {
				//rebuild path
				result = makePath(protocol, drive,absolute);

				for (String el : corrected) {
					result += el + "/";
				}
				if (result.endsWith("/"))
					result = result.substring(0, result.length() - 1);
			}
		} else {
			result = makePath(protocol, drive,absolute);
		}

		return result;
	}

	private String makePath(String protocol, String drive,boolean absolute) {
		String result = "";
		if (!protocol.isEmpty()) {
			result += protocol;
			result += "://";
		}
		if (!drive.isEmpty()) {
			result += drive;
			result += ":";
			result += "/";
		} 
		if (result.isEmpty() && absolute)
			result += "/";
		return result;
	}

	/**
	 * Returns the filename part of a path
	 * 
	 * @param path
	 * @return
	 */
	public String getFilename(String path) {
		int idx = path.lastIndexOf(DataUtil.PATH_SEPARATOR);
		if (idx <= 0)
			idx = path.lastIndexOf("/");
		if (idx <= 0)
			idx = path.lastIndexOf("\\");
		String result;
		if (idx >= 0)
			result = path.substring(idx + 1);
		else
			result = path;

		return result;
	}

	/**
	 * Returns the size of a file
	 * 
	 * @param path
	 * @return
	 */
	public long getFilesize(String path) {
		File file = new File(path);

		return file.length();
	}

	/**
	 * Returns the relative name of a file
	 * 
	 * @param path
	 * @return
	 */
	public String getRelativename(String path, String filename) {
		String result = filename;
		// simplify folders
		String simplifiedPath = simplifyFolder(path);
		String simplifiedFilename = simplifyFolder(filename);

		if (null != simplifiedPath && !simplifiedPath.isEmpty()) {
			simplifiedPath = stripProtocol(simplifiedPath);
			simplifiedFilename = stripProtocol(simplifiedFilename);
			if (simplifiedFilename.toLowerCase().startsWith(simplifiedPath.toLowerCase())) {
				result = simplifiedFilename.substring(simplifiedPath.length());
				while (result.startsWith("/"))
					result = result.substring(1);
			}
		}

		return result;
	}

	/**
	 * Read the content of a file into a list of string (each line has its own
	 * entry)
	 * 
	 * @param filename
	 * @return
	 */
	public List<String> readLinesFromFile(String filename) {
		return readLinesFromFile(filename, null, null);
	}

	public String getDefaultEncoding() {
		return System.getProperties().getProperty(FILE_ENCODING);
	}

	/**
	 * Pas the content of an input stream to a callback, line by line
	 * 
	 * @param is       input stream
	 * @param stream   name
	 * @param begin    //comment begin
	 * @param end      //comment end
	 * @param callback
	 */
	public void readLinesFromFile(InputStream is, String streamname, String begin, String end,
			LineReaderCallback callback) {
		readLinesFromFile(is, streamname, begin, end, getDefaultEncoding(), callback);
	}

	/**
	 * Wrapper with default encoding
	 * 
	 * @param filename
	 * @param begin    //comment begin
	 * @param end      //comment end
	 * 
	 * @param callback
	 */
	public void readLinesFromFile(String filename, String begin, String end, LineReaderCallback callback) {
		readLinesFromFile(filename, begin, end, getDefaultEncoding(), callback);
	}

	/**
	 * Pas the content of a file to a callback, line by line
	 * 
	 * @param filename
	 * @param begin    //comment begin
	 * @param end      //comment end
	 * @encoding
	 * 
	 * @param callback
	 */
	public void readLinesFromFile(String filename, String begin, String end, String encoding,
			LineReaderCallback callback) {
		try (FileInputStream fis = new FileInputStream(filename)) {
			readLinesFromFile(fis, filename, begin, end, encoding, callback);
		} catch (IOException e) {
			LogUtil.getInstance().log(getClass().getName(), Level.WARNING, "unable to open [" + filename + "]", e);
		}
	}

	/**
	 * Pas the content of an input stream to a callback, line by line
	 * 
	 * @param is     input stream
	 * @param stream name
	 * @param begin  //comment begin
	 * @param end    //comment end
	 * @encoding
	 * 
	 * @param callback
	 */
	public void readLinesFromFile(InputStream is, String streamname, String begin, String end, String encoding,
			LineReaderCallback callback) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(is, encoding));
			String line = "";
			String tmp = null;
			boolean add;
			boolean considerBlocks = null != begin && null != end;
			int i = 0;
			do {
				tmp = br.readLine();
				if (null != tmp && !tmp.isEmpty()) {
					line += tmp;
					add = true;
					if (considerBlocks)
						add = !StringUtil.getInstance().isInsideBlock(line, begin, end, line.length() - 1);
					else
						add = true;
					if (add && null != line && !line.isEmpty()) {
						callback.readLine(i, line);
						line = "";
					} else {
						line += "\n";
					}
				}
				++i;
			} while (null != tmp);
			if (null != line && !line.isEmpty())
				callback.readLine(i, line);
		} catch (Exception ex) {
			LogUtil.getInstance().log(getClass().getName(), Level.WARNING,
					"problem reading lines from file [" + streamname + "]", ex);
		} finally {
			br = null;
			// try {
			// br.close();
			// } catch (Exception ex) {
			// LogUtil.getInstance().log(getClass().getName(), Level.WARNING,
			// "error closing file [" + streamname + "]", ex);
			// } finally {
			// }

		}
	}

	/**
	 * Read the content of a file into a list of string (each line has its own
	 * entry)
	 * 
	 * @param filename
	 * @param begin    //comment begin
	 * @param end      //comment end
	 * @return
	 */
	public List<String> readLinesFromFile(String filename, String begin, String end) {
		List<String> result = new ArrayList<>();

		readLinesFromFile(filename, begin, end, (i, line) -> result.add(line));

		return result;
	}

	/**
	 * Read contents from resource into a StringBuilder
	 * 
	 * @param cls
	 * @param resource
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public StringBuilder readContentFromResource(Class cls, String resource) {
		StringBuilder result = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(resource)));
			String line;
			do {
				line = br.readLine();
				if (null != line && !line.isEmpty()) {
					result.append(line);
				}
			} while (null != line && !line.isEmpty());
		} catch (Exception ex) {
			LogUtil.getInstance().log(getClass().getName(), Level.WARNING,
					"problem reading content from resource [" + cls.getName() + ":" + resource + "]", ex);
		} finally {
			close(br, cls.getName() + ":" + resource);
		}
		return result;
	}

	public void copy(InputStream is, OutputStream os) {
		byte[] buf = new byte[100000];

		int readbytes;
		try {
			readbytes = is.read(buf);
			while (readbytes > 0) {
				os.write(buf, 0, readbytes);
				readbytes = is.read(buf);
			}
		} catch (IndexOutOfBoundsException | IOException e) {
			LogUtil.getInstance().error("unable copy streams", e);
		}

	}

	public void close(InputStream ips, String filename) {
		if (null != ips) {
			try {
				ips.close();
			} catch (IOException e) {
				LogUtil.getInstance().error("problem closing file [" + filename + "]", e);
			}
		}
	}

	/**
	 * Close a writer
	 * 
	 * @param wri
	 * @param filename
	 */
	public void close(java.io.Writer wri, String filename) {
		if (null != wri) {
			try {
				wri.close();
			} catch (IOException e) {
				LogUtil.getInstance().warning("problem closing file[" + filename + "]", e);
			}
		}
	}

	/**
	 * Close a reader
	 * 
	 * @param wri
	 * @param filename
	 */
	public void close(java.io.Reader rdr, String filename) {
		if (null != rdr) {
			try {
				rdr.close();
			} catch (IOException e) {
				LogUtil.getInstance().warning("problem closing file[" + filename + "]", e);
			}
		}
	}

	public void close(InputStream ips) {
		close(ips, "unknown");
	}

	public void close(OutputStream os) {
		close(os, "unknown");
	}

	public void close(OutputStream os, String filename) {
		if (null != os) {
			try {
				os.close();
			} catch (IOException e) {
				LogUtil.getInstance().error("problem closing file [" + filename + "]", e);
			}
		}
	}

	/**
	 * Convenience method for printf
	 * 
	 * @param fmt
	 * @param values
	 * @return
	 */
	public String printf(String fmt, Object... values) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		ps.printf(fmt, values);

		return bos.toString();
	}

	/**
	 * Remove null values from a list, this method is NOT thread safe
	 * 
	 * @param list
	 * @return
	 */
	public int removeNulls(List<?> list) {
		List<Integer> idxsRemove = new ArrayList<>();
		int idx = 0;

		for (Object el : list) {
			if (null == el) {
				idxsRemove.add(idx);
			}
			++idx;
		}
		int removeCount = 0;

		for (Integer idxToRemove : idxsRemove) {
			list.remove(idxToRemove - removeCount++);
		}

		return removeCount;
	}

	/**
	 * Return the current working dir
	 * 
	 * @return
	 */
	public String getCurrentWorkingdir() {
		String result = null;

		result = new File(".").getAbsolutePath();

		return result;
	}

	/**
	 * Make directories based on a filename
	 * 
	 * @param filename
	 */
	public void makeDirectories(String filename) {
		String folder = DataUtil.getInstance().getFoldername(filename);
		File file = new File(folder);
		boolean ok = true;

		if (!file.exists())
			ok = file.mkdirs();

		if (!ok)
			LogUtil.getInstance()
					.warning("problem creating directories for [" + filename + "] : folder [" + folder + "]");

	}

	public <V> List<V> getMapValuesAsList(Map<?, V> roots) {
		return roots.values().stream().collect(Collectors.toList());
	}

	public int getUuid() {
		return ++uuid;
	}

	/**
	 * Write an object to a file
	 * 
	 * @param file
	 * @param object
	 * @param wri
	 */
	public <T> void writeToFile(String file, T object, Writer<T> wri) {
		writeToFile(file, wri.write(object).toString());
	}

	/**
	 * Create a StringBuilder from an InputStream
	 * 
	 * @param is
	 * @return
	 */
	public StringBuilder writeToStringBuilder(InputStream is) {
		StringBuilder result = new StringBuilder();
		byte[] buf = new byte[100000];
		int readbytes;

		try {
			readbytes = is.read(buf);
			while (readbytes > 0) {
				result.append(new String(buf, 0, readbytes));
				++readbytes;
				readbytes = is.read(buf);
			}
		} catch (IOException e) {
			LogUtil.getInstance().error("problem streaming into StringBuilder", e);
		}

		return result;
	}

	/**
	 * Determine if the filename is an absolute path
	 * 
	 * @param filename
	 * @return
	 */
	public boolean isAbsolutePath(String filename) {
		boolean result;
		if (null != filename && !filename.isEmpty() && filename.matches("(?i)(.*://?.*)|([a-z]:(/|\\\\).*)"))
			result = true;
		else
			result = false;
		return result;
	}

	/**
	 * Returns the alternative for a null value
	 * 
	 * @param value
	 * @param nullValue
	 * @return
	 */
	public static <T> T nvl(T value, T nullValue) {
		return (null == value) ? nullValue : value;
	}

	/**
	 * Replace the protocol according to substitution rules
	 * 
	 * @param filename
	 * @return
	 */
	public String protocolReplace(String filename) {
		String result = StringUtil.getInstance().replace(filename,
				ConfigurationUtil.getInstance().getSetting(URL_PROTOCOL_REPLACE));

		return result;

	}
}

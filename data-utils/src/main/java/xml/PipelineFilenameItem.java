package xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import data.LogUtil;

/**
 * XSLT is te vinden op de file locatie.
 */
public class PipelineFilenameItem implements PipelineItem {
	private String filename;
	private InputStream is;

	public PipelineFilenameItem(String filename) {
		this.filename = filename;
	}

	@Override
	public InputStream getStream() {
		if (null == is) {
			try {
				is = new BufferedInputStream(new FileInputStream(filename));

			} catch (FileNotFoundException e) {
				LogUtil.getInstance().warning("unable to get stream from [" + filename + "]", e);
			} finally {

			}
		}
		return is;

	}
}

package xml;

import java.io.InputStream;

/**
 * XSLT document 'zit' in een stream.
 */
public class PipelineStreamItem implements PipelineItem {
	private InputStream is;

	public PipelineStreamItem(InputStream is) {
		this.is = is;
	}

	@Override
	public InputStream getStream() {
		return is;
	}

}

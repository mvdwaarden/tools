package xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import data.LogUtil;

/**
 * Purpose: Convenience class voor XSLT transformaties.
 */
public abstract class XMLTransformer {
	/**
	 * Transformer script type
	 */
	public enum ScriptType {
		XSLT, XQUERY;
	}
	/**
	 * Transformeer XMLBinder naar een output stream
	 * 
	 * @param binder
	 * @param os
	 * @param transformer
	 */
	public abstract boolean transform(XMLBinder binder, OutputStream os, InputStream transformer);

	/**
	 * Pas alle transformaties in de pipeline to op het ingaande document ('is'
	 * variabele)
	 * 
	 * @param is
	 * @param os
	 * @param pipeline
	 * @return
	 */
	public boolean transform(XMLBinder binder, OutputStream os, XMLTransformPipeline pipeline) {
		XMLBinder lbinder = binder;
		OutputStream los;

		boolean result = true;
		int i = 0;

		for (PipelineItem item : pipeline.getItems()) {
			++i;
			if (i == pipeline.getItems().size())
				los = os;
			else
				los = new ByteArrayOutputStream();

			transform(lbinder, los, item.getStream());
			if (null != lbinder && !lbinder.close()) {
				LogUtil.getInstance().warning("problem transforming on item [" + i + "]");
				result = false;
				break;
			}
			if (i < pipeline.items.size())
				lbinder = new XMLBinder(new ByteArrayInputStream(los.toString().getBytes()));
		}

		return result;
	}
}

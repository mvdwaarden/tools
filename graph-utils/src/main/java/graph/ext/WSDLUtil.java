package graph.ext;

import org.xml.sax.Attributes;

import data.DataUtil;
import data.LogUtil;
import xml.XMLSAXHandler;
import xml.XMLUtil;

public class WSDLUtil {
	private static ThreadLocal<WSDLUtil> instance = new ThreadLocal<WSDLUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static WSDLUtil getInstance() {
		WSDLUtil result = instance.get();

		if (null == result) {
			result = new WSDLUtil();
			instance.set(result);
		}

		return result;
	}

	public String createWSDLName(String path, String wsdl) {
		String result = "";
		class Locals {
			String targetNamespace;
			String name;
		}
		;
		String wsdlFile;
		if (wsdl.matches("[^:]*:/.*")) {
			wsdlFile = DataUtil.getInstance().protocolReplace(wsdl);
		} else
			wsdlFile = DataUtil.getInstance().simplifyFolder(path + DataUtil.PATH_SEPARATOR + wsdl);
		final Locals _locals = new Locals();
		try {
			XMLUtil.getInstance().parse(wsdlFile, new XMLSAXHandler() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes atts) {
					if (nodeTest(qName, null, WSDLConst.EL_DEFINITIONS, null, localName)) {
						_locals.name = atts.getValue(WSDLConst.ATTR_NAME);
						_locals.targetNamespace = atts.getValue(WSDLConst.ATTR_TARGET_NAMESPACE);
					}
				}
			});

			if (null == _locals.name || _locals.name.isEmpty())
				_locals.name = DataUtil.getInstance().getFilenameWithoutExtension(wsdlFile);
			if (null != _locals.name && null != _locals.targetNamespace && !_locals.name.isEmpty()
					&& !_locals.targetNamespace.isEmpty())
				result = _locals.targetNamespace + DataUtil.PATH_SEPARATOR + _locals.name;
		} catch (Exception ex) {
			LogUtil.getInstance().error("Unable to create WSDL name for [ " + wsdlFile + "]", ex);
			result = wsdlFile;
		}
		return result;
	}
}

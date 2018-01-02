package xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import data.ConfigurationUtil;
import data.LogUtil;
import xml.XMLBinding.BindingValueType;

/**
 * Purpose: Convenience class voor XSLT transformaties.
 */
public class XSLTTransformer extends XMLTransformer {
	@Override
	public boolean transform(XMLBinder binder, OutputStream os, InputStream xslt) {
		boolean result = false;
		try {
			String transformerFactoryClass = ConfigurationUtil.getInstance()
					.getSetting(XMLUtil.TRANSFORMER_FACTORY_CLASS);
			TransformerFactory tf = null;
			if (null != transformerFactoryClass && !transformerFactoryClass.isEmpty())
				tf = TransformerFactory.newInstance(transformerFactoryClass, null);
			else
				tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer(new StreamSource(xslt));
			// set the parameters based on NON stream bindings
			for (XMLBinding binding : binder.getBindings().stream()
					.filter(b -> b.getValueType() != BindingValueType.STREAM).collect(Collectors.toList()))
				transformer.setParameter(
						(null != binding.getURI() && binding.getURI().length() > 0)
								? "{" + binding.getURI() + "}" + binding.getName() : binding.getName(),
						binding.getString());
			transformer.transform(new StreamSource(binder.<InputStream>getFirstBindingByType(BindingValueType.STREAM)),
					new StreamResult(os));
			result = true;
		} catch (Exception e) {
			LogUtil.getInstance().warning("problem transforming ", e);
			throw new RuntimeException(e);
		}
		return result;
	}
}

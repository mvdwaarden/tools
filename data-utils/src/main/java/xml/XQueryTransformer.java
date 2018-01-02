package xml;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import data.DataUtil;
import data.LogUtil;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.value.StringValue;

public class XQueryTransformer extends XMLTransformer {
	@Override
	public boolean transform(XMLBinder binder, OutputStream os, InputStream xquery) {
		boolean result = false;
		Configuration cfg = new Configuration();
		StaticQueryContext ctx = cfg.newStaticQueryContext();

		try {
			XQueryExpression ex = ctx.compileQuery(xquery, "UTF-8");
			DynamicQueryContext dctx = new DynamicQueryContext(cfg);
			dctx.setSchemaValidationMode(Validation.SKIP);
			for (XMLBinding bind : binder.getBindings())
				dctx.setParameter(new StructuredQName(bind.getPrefix(), bind.getURI(), bind.getName()),
						getValue(cfg, bind));
			List<Object> output = ex.evaluate(dctx);
			// is there any output, yes => write the transformation result to a file
			if (output.get(0) instanceof TinyElementImpl) {
				TinyElementImpl el = (TinyElementImpl) output.get(0);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				writeToOutputStream(el, bos);
				String str = bos.toString();
				DataUtil.getInstance().writeToOutputStream(os, str, null);
				result = true;
			}			
		} catch (TransformerException | IOException e) {
			LogUtil.getInstance().error("problem during XQuery transformation", e);
		}
		return result;
	}

	public Sequence getValue(Configuration cfg, XMLBinding binding) {
		Sequence result = null;
		switch (binding.getValueType()) {
		case STRING:
			result = new StringValue(binding.getString());
			break;
		case STREAM:
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				Document doc = db.parse(binding.getInputStream());
				DocumentWrapper wrapper = new DocumentWrapper(doc, binding.getURI(), cfg);

				class Maker extends DOMNodeWrapper {
					protected Maker(Node node, DocumentWrapper docWrapper, DOMNodeWrapper parent, int index) {
						super(node, docWrapper, parent, index);
					}

					public DOMNodeWrapper make(Node node, DocumentWrapper docWrapper,
							/* @Nullable */ DOMNodeWrapper parent, int index) {
						return makeWrapper(node, docWrapper, parent, index);
					}
				}
				Maker maker = new Maker(doc.getDocumentElement(), wrapper, null, 0);

				result = maker.make(doc.getDocumentElement(), wrapper, null, 0);
			} catch (SAXException | IOException | ParserConfigurationException e) {
				LogUtil.getInstance().error("problem getting value", e);
			}
			break;
		}
		return result;
	}

	private void writeToOutputStream(Source source, OutputStream os) throws TransformerException {
		// Use a Transformer for output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;

		transformer = tFactory.newTransformer();
		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);
	}

	public static void main(String[] args) {

		XQueryTransformer tf = new XQueryTransformer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			tf.transform(
					new XMLBinder(
							new XMLBinding[] {
									new XMLBinding("", null, "covog_model_grAanvraag",
											new FileInputStream(
													"d:/projects/tools/soa/soatool/src/main/xml/testxq.xml")),
									new XMLBinding("xs", null, "relatieCode", "1235") }),
					bos, new FileInputStream("d:/projects/tools/soa/soatool/src/main/xquery/transform.xq"));
		} catch (FileNotFoundException e) {
			LogUtil.getInstance().error("unable to find files for XQuery transformation", e);
		}
	}
}

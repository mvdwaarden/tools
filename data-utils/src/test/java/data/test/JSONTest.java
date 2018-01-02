package data.test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import csv.CSVData;
import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import data.TimeUtil;
import json.JSONObject;
import json.JSONParser;
import json.JSONRecord;
import json.JSONUtil;
import json.JSONValue;
import json.JSONWriter;
import json.JavaWalker;
import json.JavaWalkerCallbackWriter;
import json.Path;
import json.Path.Option;
import json.XMLWriter;

public class JSONTest {

	@Test
	public void testLoop() {
		String[] files = new String[] { "looptest.json", "googlehangout.json", "nonquotedfields.json.error" };

		for (String file : files) {

			String json = DataUtil.getInstance().readFromFile(
					ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + file);

			JSONObject jsonObject = JSONUtil.getInstance().parseJSON(json);
			XMLWriter xmlWri = new XMLWriter();
			String result = xmlWri.write(jsonObject, "root");

			DataUtil.getInstance().writeToFile(
					"d://tmp/" + DataUtil.getInstance().getFilenameWithoutExtension(file) + ".xml", result);
			JSONWriter jsonWri = new JSONWriter();
			result = jsonWri.write(jsonObject);

			DataUtil.getInstance().writeToFile(
					"d://tmp/" + DataUtil.getInstance().getFilenameWithoutExtension(file) + ".json", result);
		}

		return;
	}

	@Test
	public void testJSON2Java2() {
		B b1 = createTestB();

		JSONObject jsonObject = JSONUtil.getInstance().java2JSON(b1);

		String json1 = JSONUtil.getInstance().writeJSON(jsonObject);

		B b2 = new B();

		JSONUtil.getInstance().json2Java(jsonObject, b2);
		b1.doAssertions(b2);
		Assert.assertEquals(b1.getAs().size(), b2.getAs().size());

		jsonObject = JSONUtil.getInstance().java2JSON(b2);

		String json2 = JSONUtil.getInstance().writeJSON(jsonObject);
		Assert.assertTrue("conversion succceeded", json1.length() > 0);
		Assert.assertEquals(json1.replace("\n", "").replace("\t", "").replace(" ", ""),
				json2.replace("\n", "").replace("\t", "").replace(" ", ""));

		return;
	}

	@Test
	public void testJavaWalker() {
		JavaWalker walker = new JavaWalker();

		walker.walk(createTestB(), null, new JavaWalkerCallbackWriter());

	}

	@Test
	public void testJSON22PropertiesStream() {
		String json = "{\"nameId\":\"124992857\",\"userAttributes\":[{\"name\":\"bsn\",\"values\":[{\"value\":\"987654321\"},{\"value\":\"123456789\"}]},{\"name\":\"test\",\"values\":[{\"value\":\"test987654321\"},{\"value\":\"test123456789\"}]}]}";
		JSONObject jsonObject = JSONUtil.getInstance().parseJSON(new ByteArrayInputStream(json.getBytes()));
		Properties props = JSONUtil.getInstance().json2Properties(jsonObject);

		Assert.assertEquals("bsn", props.getProperty("userAttributes[0].name"));
		Assert.assertEquals("987654321", props.getProperty("userAttributes[0].values[0].value"));
		Assert.assertEquals("123456789", props.getProperty("userAttributes[0].values[1].value"));
		Assert.assertEquals("test", props.getProperty("userAttributes[1].name"));
		Assert.assertEquals("test987654321", props.getProperty("userAttributes[1].values[0].value"));
		Assert.assertEquals("test123456789", props.getProperty("userAttributes[1].values[1].value"));

		JSONParser parser = new JSONParser();

		jsonObject = parser.parse(new ByteArrayInputStream(new byte[] { 0 }));

		Assert.assertTrue("empty JSON object", null == jsonObject || jsonObject.isEmpty());

		return;
	}

	@Test
	public void testJSON22Properties() {
		String json = "{\"nameId\":\"124992857\",\"userAttributes\":[{\"name\":\"bsn\",\"values\":[{\"value\":\"987654321\"},{\"value\":\"123456789\"}]},{\"name\":\"test\",\"values\":[{\"value\":\"test987654321\"},{\"value\":\"test123456789\"}]}]}";
		JSONObject jsonObject = JSONUtil.getInstance().parseJSON(json);
		Properties props = JSONUtil.getInstance().json2Properties(jsonObject);

		Assert.assertEquals("bsn", props.getProperty("userAttributes[0].name"));
		Assert.assertEquals("987654321", props.getProperty("userAttributes[0].values[0].value"));
		Assert.assertEquals("123456789", props.getProperty("userAttributes[0].values[1].value"));
		Assert.assertEquals("test", props.getProperty("userAttributes[1].name"));
		Assert.assertEquals("test987654321", props.getProperty("userAttributes[1].values[0].value"));
		Assert.assertEquals("test123456789", props.getProperty("userAttributes[1].values[1].value"));

		return;
	}

	@Test
	public void testJSON2XML() {
		String json = "{\"nameId\":\"124992857\",\"userAttributes\":[{\"name\":\"bsn\",\"values\":[{\"value\":\"124992857\"}]}]}";
		JSONObject jsonObject = JSONUtil.getInstance().parseJSON(json);
		String xml = "<etc><nameId>124992857</nameId><userAttributes><name>bsn</name><values><value>124992857</value></values></userAttributes></etc>";
		XMLWriter xmlWriter = new XMLWriter();
		String xmlResult = xmlWriter.write(jsonObject, "etc");
		Assert.assertEquals(xmlResult.replace("\n", "").replace("\t", "").replace(" ", ""), xml);
	}

	@Test
	public void testXML2JSON() {
		String xml = "<a><b>b1</b><b>b2</b><b>b3</b><c><d>d1</d><d>d2</d><d>d3</d></c><c><d>d4</d><d>d5</d><d>d6</d></c></a>";
		JSONObject jsonObject = JSONUtil.getInstance().parseXML(xml);
		XMLWriter xmlWriter = new XMLWriter();
		String xmlResult = xmlWriter.write(jsonObject, "etc");
		Assert.assertEquals(xmlResult.replace("\n", "").replace("\t", "").replace(" ", ""), "<etc>" + xml + "</etc>");
	}

	public static class A {
		private B b;
		private int intValue;
		private long longValue;
		private double doubleValue;
		private Date dateValue;
		private Boolean boolValue;
		private String stringValue;

		public A() {

		}

		public A(int intValue, long longValue, double doubleValue, Date dateValue, String stringValue,
				Boolean boolValue) {
			this.intValue = intValue;
			this.longValue = longValue;
			this.doubleValue = doubleValue;
			this.dateValue = dateValue;
			this.stringValue = stringValue;
			this.boolValue = boolValue;
		}

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}

		public int getIntValue() {
			return intValue;
		}

		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}

		public long getLongValue() {
			return longValue;
		}

		public void setLongValue(long longValue) {
			this.longValue = longValue;
		}

		public double getDoubleValue() {
			return doubleValue;
		}

		public void setDoubleValue(double doubleValue) {
			this.doubleValue = doubleValue;
		}

		public Date getDateValue() {
			return dateValue;
		}

		public void setDateValue(Date dateValue) {
			this.dateValue = dateValue;
		}

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		public Boolean getBoolValue() {
			return boolValue;
		}

		public void setBoolValue(Boolean boolValue) {
			this.boolValue = boolValue;
		}

		public void doAssertions(A a) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			Assert.assertEquals("double value  mismatch", this.getDoubleValue(), a.getDoubleValue(), 0.001);
			Assert.assertEquals("long value  mismatch", this.getLongValue(), a.getLongValue());
			Assert.assertEquals("boolean value  mismatch", this.getBoolValue(), a.getBoolValue());
			Assert.assertEquals("string value  mismatch", this.getStringValue(), a.getStringValue());
			Assert.assertEquals("date value  mismatch", fmt.format(this.getDateValue()), fmt.format(a.getDateValue()));
			if (null != getB())
				getB().doAssertions(a.getB());
			else
				Assert.assertEquals("arrays not empty", getB(), a.getB());
		}
	}

	public static class B {
		private List<A> as = new ArrayList<>();

		public List<A> getAs() {
			if (null == as)
				as = new ArrayList<>();
			return as;
		}

		public void doAssertions(B b) {
			Assert.assertEquals(this.getAs().size(), b.getAs().size());
			for (int i = 0; i < this.getAs().size(); ++i)
				this.getAs().get(i).doAssertions(b.getAs().get(i));
		}

		public void setAs(List<A> as) {
			this.as = as;
		}
	}

	private B createTestB() {
		A a1 = new A(1, 2, 3.0, new Date(), "str1", true);
		A a2 = new A(4, 5, 6.0, new Date(), "str2", false);
		A a3 = new A(7, 8, 9.0, new Date(), "str3", true);
		B b1 = new B();
		b1.getAs().add(a1);
		b1.getAs().add(a2);
		b1.getAs().add(a3);
		A a4 = new A(10, 11, 12.0, new Date(), "str4", false);
		B b2 = new B();
		b2.getAs().add(a4);
		a2.setB(b2);

		return b1;
	}

	public static final String HEADER_PARSER = "PARSER";
	public static final String HEADER_ENABLED = "ENABLED";
	public static final String HEADER_DESCRIPTION = "DESCRIPTION";

	public static final String PARSER_JACKSON = "JACKSON";
	public static final String PARSER_THIS = "THIS";
	public static final String PARSER_FAST = "FAST";
	public static final String PARSER_SMART = "SMART";
	public static final String PARSER_THIS_TO_PROPERTIES = "PROPERTIES";

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		class BenchMarkTest {
			void doBenchmark(String file, String json) throws IOException {
			};
		}
		;
		class BenchMark {
			BenchMarkTest test;
			String name;
			String description;
			boolean enabled;

			public BenchMark(String name, String description, boolean enabled, BenchMarkTest test) {
				this.name = name;
				this.description = description;
				this.enabled = enabled;
				this.test = test;
			}
		}
		;
		class Input {
			String file;
			int loops;

			public Input(String file, int loops) {
				this.file = file;
				this.loops = loops;
			}
		}

		Input[] inputs = new Input[] { new Input("looptest.json", 0), new Input("googlehangout.json", 0),
				new Input("nonquotedfields.json", 0), new Input("large.json", 1) };
		BenchMark[] cfg = new BenchMark[] { new BenchMark(PARSER_THIS, "this", true, new BenchMarkTest() {
			void doBenchmark(String file, String json) {
				JSONParser parser = new JSONParser();
				Object o1 = null;
				o1 = parser.parse(json);
				o1 = null;
			}

		}), new BenchMark(PARSER_THIS_TO_PROPERTIES, "to_props", false, new BenchMarkTest() {
			@Override
			void doBenchmark(String file, String json) throws IOException {
				JSONParser parser = new JSONParser();
				JSONObject o1 = parser.parse(json);
				Properties props = JSONUtil.getInstance().json2Properties(o1);
				props.store(new FileOutputStream("d:/tmp/test.props"), "tada");
				o1 = null;
			}
		}), /*new BenchMark(PARSER_SMART, "smart parser", false, new BenchMarkTest() {
			void doBenchmark(String file, String json) {
				@SuppressWarnings("deprecation")
				net.minidev.json.parser.JSONParser smartParser = new net.minidev.json.parser.JSONParser();
				Object o1 = null;
				try {
					o1 = smartParser.parse(json);
				} catch (ParseException e) {
					LogUtil.getInstance().info("error smart parsing [" + file + "]", e);
				}
				o1 = null;
			};
		}), new BenchMark(PARSER_FAST, "fast parser", false, new BenchMarkTest() {
			@Override
			void doBenchmark(String file, String json) {
				Object o1 = null;
				o1 = JSON.parseObject(json);
				o1 = null;
			}
		}) , new BenchMark(PARSER_JACKSON, "this", false, new BenchMarkTest() {
			@Override
			void doBenchmark(String file, String json) throws IOException {
				ObjectReader jacksonParser = new ObjectMapper().reader(Map.class);
				Object o1;
				o1 = jacksonParser.readValue(json);
				o1 = null;
			}
		}),*/ };

		CSVData testresults = new CSVData();
		List<String> cols = new ArrayList<>();
		cols.add("FILE");
		for (BenchMark bm : cfg)
			cols.add(bm.name);
		testresults.add(cols.toArray(new String[] {}));
		for (Input input : inputs) {
			LogUtil.getInstance().info("running benchmarks for file [" + input + "]");
			String[] result = new String[cfg.length + 1];
			int resultidx = 0;
			result[resultidx++] = input.file;
			String json = DataUtil.getInstance().readFromFile(
					ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + input.file);
			int loops = input.loops;
			for (int n = 0; n < cfg.length; ++n) {
				BenchMark bm = cfg[n];
				if (bm.enabled) {
					long start = TimeUtil.getInstance().tick();
					LogUtil.getInstance().info("running:" + bm.description);
					for (int i = 0; i < loops; ++i) {
						try {
							bm.test.doBenchmark(input.file, json);
						} catch (Exception e) {
							LogUtil.getInstance().info("error " + bm.description + " with file [" + input + "]", e);
							break;
						}
					}
					result[resultidx++] = TimeUtil.getInstance().difff(start);
				} else {
					result[resultidx++] = "n.a.";
				}
			}
			testresults.add(result);
		}
		for (String[] line : testresults.getLines()) {
			for (String cell : line)
				System.out.print("\t" + cell);
			System.out.println();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPath() {
		Path path = new Path();

		path.pushName("test");
		path.pushIndex(1);
		path.pushName("name");

		Assert.assertEquals("path build problem", path.getPathString(), "test[1].name");

		String json = DataUtil.getInstance().readFromFile(
				ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "looptest.json");

		JSONObject jsonObject = JSONUtil.getInstance().parseJSON(json);
		path = Path.newPath("geocoded_waypoints[1].types[2]");
		JSONObject value = JSONUtil.getInstance().getPath(jsonObject, path);

		Assert.assertEquals("path 1 test", "political", (String) value.getData());

		jsonObject = new JSONRecord();
		value = JSONUtil.getInstance().getPath(jsonObject, path, Option.MAKE_PATH);
		((JSONValue<String>) value).setData("political");
		json = JSONUtil.getInstance().writeJSON(jsonObject);
		jsonObject = JSONUtil.getInstance().parseJSON(json);
		value = JSONUtil.getInstance().getPath(jsonObject, path);
		Assert.assertEquals("path 1 test", "political", (String) value.getData());
	}

	@Test
	public void testXmlToJSON() {
//		JSONObject jsonObj = JSONUtil.getInstance()
//				.parseXML(DataUtil.getInstance().readFromFile(ConfigurationUtil.getInstance().getTestResourcesPath()
//						+ DataUtil.PATH_SEPARATOR + "xml" + DataUtil.PATH_SEPARATOR + "test2.xml"));
//
//		String jsonStr = JSONUtil.getInstance().writeJSON(jsonObj);
//
//		DataUtil.getInstance().writeToFile("d:/tmp/void/file/test2.json", jsonStr);
	}
}

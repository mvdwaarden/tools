package data.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import conversion.ConversionUtil;
import csv.CSVData;
import csv.CSVUtil;
import data.ConfigurationUtil;
import data.DataUtil;
import data.EnumUtil;
import data.SequenceUtil;
import data.StringUtil;
import metadata.MetaData;
import metadata.SimpleXSDReader;
import object.ObjectIterator;
import object.ObjectUtil;
import xml.XMLBinder;
import xml.XMLSAXEchoHandler;
import xml.XMLUtil;
import xml.XQueryTransformer;

public class DataUtilTest {
	public enum EnumTest {
		E1, E2, E3, E4, E5
	}

	@Before
	public void setUp() {
		DataUtil.getInstance().makeDirectories(ConfigurationUtil.getInstance().getTestOutputPath());
	}

	@Test
	public void testReadCsv() {
		CSVData content = CSVUtil.getInstance().readFromFile(
				ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "trivialtest.csv");
		String[][] verify = new String[][] { { "head1", "head2", "head3", "head4" },
				{ "\"r1,1\"", "\"r1,2\"", "\"r1,3\"", "\"r1,4\"" }, { "\"r2,1\"", "\"r2,2\"", "\"r2,3\"", "\"r2,4\"" },
				{ "\"r3,1\"", "\"r3,2\"", "\"r3,3\"", "\"r3,4\"" }, { "\"r4,1\"", "\"r4,2\"", "\"r4,3\"", "\"r4,4\"" },
				{ "c1", "c2", "c3", "c4" }, { "\"r6,1\"", "c2", "\"r6,3\"", "\"c4\"" },
				{ "\"r7,1,1,1,\"", "\"r7,2,2,2\"", "\"r7,3,3,3\"", "r7" },
				{ "\"r8,1,1,1,\"", "\"r8,2,2,2\"", "\"r8,3,3,3\"", "" }, { "", "", "2", "" } };

		int i = 0;
		Assert.assertTrue("fail length check verify.length = '" + verify.length + "'== content.size() '"
				+ content.getLines().size() + "'", verify.length == content.getLines().size());
		for (String[] line : content.getLines()) {
			Assert.assertTrue("fail length check verify[" + i + "] = '" + verify[i].length + "'==  line.length = '"
					+ line.length + "' on line [" + i + "]", verify[i].length == line.length);
			for (int n = 0; n < verify[i].length; ++n) {
				Assert.assertTrue("fail verify [" + i + "," + n + "] = '" + verify[i][n] + "' != line [" + n + "] = '"
						+ line[n] + "'", verify[i][n].equals(line[n]));
			}
			++i;
		}
	}

	@Test
	public void testCSVShiftUp() {
		String tmpInFile = ConfigurationUtil.getInstance().getTmpDir() + DataUtil.PATH_SEPARATOR
				+ "csvshifttest_in.csv";
		String tmpOutFile = ConfigurationUtil.getInstance().getTmpDir() + DataUtil.PATH_SEPARATOR
				+ "csvshifttest_out.csv";
		String[][] sheet = new String[][] { { "a", "", "", "" }, { "b", "b", "", "" }, { "c", "c", "c", "" },
				{ "d", "d", "d", "d" }, { "f", "", "", "" }, { "g", "g", "", "" }, { "h", "h", "h", "" },
				{ "i", "i", "i", "i" } };
		CSVData csv = new CSVData();

		for (String[] row : sheet)
			csv.add(row);
		CSVUtil.getInstance().writeToFile(tmpInFile, csv, ',', CSVUtil.Option.TRIM_VALUES);
		CSVUtil.getInstance().concatShiftUp(tmpInFile, tmpOutFile, ',', 3, CSVUtil.Option.TRIM_VALUES);
		csv = CSVUtil.getInstance().readFromFile(tmpOutFile, ',', CSVUtil.Option.TRIM_VALUES);
		Assert.assertEquals(csv.getLines().size(), 2);
		
		Assert.assertEquals(csv.getLines().get(0)[0], "abcd");
		Assert.assertEquals(csv.getLines().get(1)[0], "fghi");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testObjectIterator() {
		TrivialData data = new TrivialData();

		data.setName("the name");
		data.setCreationUser("c_user");
		data.setCreationDate(new Date(2004, 2, 4));

		ObjectIterator itObj = new ObjectIterator(data);
		Map<String, Object> objNv = itObj.map();

		Assert.assertTrue("name = '" + objNv.get("name") + "' != 'the name'", objNv.get("name").equals("the name"));
		Assert.assertTrue("creationUser = '" + objNv.get("creationUser") + "' != 'c_user'",
				objNv.get("creationUser").equals("c_user"));
		Assert.assertTrue("creationDate = '" + objNv.get("creationDate") + "' != '2004-02-04'",
				objNv.get("creationDate").equals(new Date(2004, 2, 4)));

	}

	@Test
	public void testStringSplit() {
		String test = "\"r1,1\",\"r1,2\",\"r1,3\",\"r1,4\"";

		String[] split1 = StringUtil.getInstance().split(test, ",", "\"", "\"");

		Assert.assertEquals("expected 4 entries", split1.length, 4);

		test = "ab->bc;\";bc\"->cd;cd->ab";

		List<String[]> split2 = StringUtil.getInstance().split(test, new String[] { ";", "->" }, "\"", "\"");

		Assert.assertEquals("expected 3 entries", split2.size(), 3);
		for (String[] s : split2)
			Assert.assertEquals("expected 2 entries", s.length, 2);

		test = "[2016-11-28T15:05:27.479+01:00] [zms_server1] [NOTIFICATION] [] [nl.minvenj.justis.edn.EventService] [tid: [ACTIVE].ExecuteThread: '6' for queue: 'weblogic.kernel.Default (self-tuning)'] [userId: t_om2] [ecid: 005G_nqiNrEFw0aLpMWByY0003Mh0003jW,0:1] [APP: ZMSApplication-1.78.506#1.78.5] [DSID: 0000LYfGG42Fw0aLpMWByY1ODLsX00001f] Raising event [{http://schemas.oracle.com/events/edl/ZaakAuditEvent}ZaakAuditEvent] of type [nl.minvenj.justis.wfm.model.edn.TaskEvent]";

		split2 = StringUtil.getInstance().split(test, new String[] { " ", "->" }, "[", "]");

		Assert.assertEquals("expected 16 entries", split2.size(), 16);

		test = "[2016-11-28T15:05:27.478+01:00] [zms_server1] [TRACE] [] [nl.minvenj.justis.wfm.model.proto.WFMDataObject] [tid: [ACTIVE].ExecuteThread: '6' for queue: 'weblogic.kernel.Default (self-tuning)'] [userId: t_om2] [ecid: 005G_nqiNrEFw0aLpMWByY0003Mh0003jW,0:1] [APP: ZMSApplication-1.78.506#1.78.5] [DSID: 0000LYfGG42Fw0aLpMWByY1ODLsX00001f] [SRC_CLASS: nl.minvenj.justis.wfm.model.proto.WFMDataObject] [SRC_METHOD: getProperties] User [t_om2]: load properties file [soa.properties]";
		split2 = StringUtil.getInstance().split(test, new String[] { " ", "->" }, "[", "]");

		Assert.assertEquals("expected 18 entries", split2.size(), 18);

	}

	@Test
	public void testObjectMerge() {
		class TestObject {
			private int i1;
			private String str1;
			private boolean b1;
			private Boolean bobj1;

			public TestObject(int i) {
				this.i1 = i;
			}

			public TestObject(String str) {
				this.str1 = str;
			}

			public TestObject(int i, String str, boolean b, Boolean bobj) {
				this.i1 = i;
				this.str1 = str;
				this.b1 = b;
				this.bobj1 = bobj;
			}

		}
		;
		TestObject obj1 = new TestObject("value1");
		TestObject obj2 = new TestObject(1);

		TestObject m = ObjectUtil.getInstance().merge(obj1, obj2);

		Assert.assertEquals(m.i1, obj2.i1);
		Assert.assertEquals(m.str1, obj1.str1);

		obj1 = new TestObject(1, "value1", true, null);
		obj2 = new TestObject(2, "value2", false, null);

		m = ObjectUtil.getInstance().merge(obj1, obj2);

		Assert.assertEquals(m.i1, obj2.i1);
		Assert.assertEquals(m.str1, obj2.str1);
		Assert.assertEquals(m.b1, obj2.b1);
		Assert.assertEquals(m.bobj1, obj1.bobj1);

		obj1 = new TestObject(1, "value1", true, true);
		obj2 = new TestObject("value2");

		m = ObjectUtil.getInstance().merge(obj1, obj2);

		Assert.assertEquals(m.i1, obj2.i1);
		Assert.assertEquals(m.str1, obj2.str1);
		Assert.assertEquals(m.b1, obj2.b1);
		Assert.assertEquals(m.bobj1, obj1.bobj1);
	}

	@Test
	public void testCamelCasingFormat() {
		Assert.assertEquals(StringUtil.getInstance().camelCaseFormat("aaa bbb ccc",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", " "), "aaaBbbCcc");
		Assert.assertEquals(

				StringUtil.getInstance().camelCaseFormat("aaa +-*bbb cc*&^c",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", " "),
				"aaaBbbCcc");
		Assert.assertEquals(

				StringUtil.getInstance().camelCaseFormat("+ aaa+bbb--ccc",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+-"),
				"AaaBbbCcc");
		Assert.assertEquals(

				StringUtil.getInstance().camelCaseFormat("aaa bbb ccc",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- "),
				"aaaBbbCcc");
		Assert.assertEquals(

				StringUtil.getInstance().camelCaseFormat(" aaa bbb ccc",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- "),
				"AaaBbbCcc");
		Assert.assertEquals(

				StringUtil.getInstance().camelCaseFormat("a+b+c+aa+bb+cc",
						"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- "),
				"aBCAaBbCc");
		Assert.assertEquals(StringUtil.getInstance().camelCaseFormat("a%b+%c%aa% bb-cc",
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- "), "abCaaBbCc");
		Assert.assertEquals(StringUtil.getInstance().camelCaseFormat("à%b+%ç%äå% bb-cc", "àáâãäåçèéêëìíîïòóôõö",
				"aaaaaaceeeeiiiiooooo", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- ", false),
				"abCaaBbCc");
		Assert.assertEquals(StringUtil.getInstance().camelCaseFormat("à%b+%ç%äå% bb-cc", "àáâãäåçèéêëìíîïòóôõö",
				"aaaaaaceeeeiiiiooooo", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", "+- ", true),
				"AbCaaBbCc");
	}

	@Test
	public void testReplaceInInner() {
		Assert.assertEquals(
				StringUtil.getInstance().replaceInInnerblock(
						"111 ( 222 ( 333 444( 444 555 5444 4445 444) ( 444 444) 444))", '(', ')', "() ", "444", "aha"),
				"111 ( 222 ( 333 444( aha 555 5444 4445 aha) ( aha aha) 444))");
		Assert.assertEquals(StringUtil.getInstance().replaceInInnerblock("111 ( 222 ( 333 444( 444 555) ( 444) 444))",
				'(', ')', "() ", "444", "aha"), "111 ( 222 ( 333 444( aha 555) ( aha) 444))");
		Assert.assertEquals(
				StringUtil.getInstance().replaceInInnerblock("111 ( 222 ( 333 444( 444 555 444 444) ( 444 444) 444))",
						'(', ')', "() ", "444", "aha"),
				"111 ( 222 ( 333 444( aha 555 aha aha) ( aha aha) 444))");

	}

	@Test
	public void testIsInsideBlock() {
		Assert.assertTrue(StringUtil.getInstance().isInsideBlock("@123 ", "@", "@", 4));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@", "@", "@", 4));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@@123@ @123@", "@", "@", 10));
		Assert.assertTrue(StringUtil.getInstance().isInsideBlock("@123@@123@ @123@", "@", "@", 12));
		Assert.assertTrue(StringUtil.getInstance().isInsideBlock("@1234@", "@", "@", 4));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@12@34", "@", "@", 4));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@", "@", "@", 20));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@", "@", "@", 0));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@@123@ @123@", "@", "@", 4));
		Assert.assertFalse(StringUtil.getInstance().isInsideBlock("@123@@123@ @123@", "@", "@", 5));
	}

	public static class TrivialData {
		private String name;
		private Date creationDate;
		private String creationUser;

		private List<TrivialData> childs = new ArrayList<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<TrivialData> getChilds() {
			return childs;
		}

		public void setChilds(List<TrivialData> childs) {
			this.childs = childs;
		}

		public Date getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(Date creationDate) {
			this.creationDate = creationDate;
		}

		public String getCreationUser() {
			return creationUser;
		}

		public void setCreationUser(String creationUser) {
			this.creationUser = creationUser;
		}
	}

	@Test
	public void testCSVData() {
		CSVData csvData = new CSVData();

		csvData.setHeader(new String[] { "A", "B", "C" });
		csvData.add(new String[] { "a11", "a12", "a13" });
		csvData.add(new String[] { "a21", "a22", "a23" });
		csvData.add(new String[] { "a31", "a32", "a33" });
		csvData.add(new String[] { "a41", "a42", "a43" });

		String[] row = csvData.get("B", "a22");
		Assert.assertEquals("a21", row[0]);
		row = csvData.get("C", "a43");
		Assert.assertEquals("a41", row[0]);
	}

	@Test
	public void testObjectListFilter() {
		// Three simple classdefinitions
		// B is an A
		// C is an A
		class A {
			public A(int val) {
			}

		}
		;
		class B extends A {
			public B(int valA, int val) {
				super(valA);
			}
		}
		class C extends A {
			public C(int valA, int val) {
				super(valA);
			}
		}
		List<A> items = new ArrayList<>();
		// 4 A's
		items.add(new A(1));
		items.add(new A(2));
		items.add(new A(3));
		items.add(new A(4));
		// 3 B's
		items.add(new B(5, 5));
		items.add(new B(6, 6));
		items.add(new B(7, 7));
		// 2 C's
		items.add(new C(8, 8));
		items.add(new C(9, 9));

		List<B> resultB = ObjectUtil.getInstance().filter(items, B.class);
		Assert.assertEquals("filtered list should contain 3 B's only", 3, resultB.size(), 3);
		List<C> resultC = ObjectUtil.getInstance().filter(items, C.class);
		Assert.assertEquals("filtered list should contain 2 C's only", 2, resultC.size(), 2);
		@SuppressWarnings("unchecked")
		List<A> resultBC = ObjectUtil.getInstance().filter(items, new Class[] { B.class, C.class });
		Assert.assertEquals("filtered list should contain 3 B's and 2 C's (5 in total)", 5, resultBC.size());
		List<A> resultA = ObjectUtil.getInstance().filter(items, A.class);
		Assert.assertEquals("filtered list should contain all elements (9 in total)", items.size(), resultA.size());
	}

	private boolean check(String check) {
		boolean result = false;

		byte[] bytes = StringUtil.getInstance().string2ByteArray(check);

		String str = StringUtil.getInstance().byteArray2String(bytes, false);

		result = check.equals(str);

		return result;
	}

	@Test
	public void testCSVConversion() {
		ConversionUtil.getInstance().CSV2XML(
				ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "large.csv", ';',
				ConfigurationUtil.getInstance().getTestOutputPath() + DataUtil.PATH_SEPARATOR + "large.csv.xml", "",
				CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);

	}

	@Test
	public void testByte2StringConversion() {
		String[] checks = new String[] {
				"2053db1dfa1b96689f1171554f9031f308f59fe360f47f4d1b3cc629682e92ded8a5379a89ae15e0cce7cfd8af3c30a2241d83c57e58b41a38b618994836060c",
				"1234", "123", "abcd", "abc" };
		for (String check : checks)
			Assert.assertTrue(check + " wordt niet goed geconverteerd van str naar byte[] en visa versa", check(check));
	}

	@Test
	public void testEnumUtil() {
		Assert.assertTrue(EnumUtil.getInstance().contains(new EnumTest[] { EnumTest.E1, EnumTest.E2 }, EnumTest.E1));
		Assert.assertFalse(EnumUtil.getInstance().contains(new EnumTest[] { EnumTest.E1, EnumTest.E2 }, EnumTest.E3));
		Assert.assertEquals(EnumUtil.getInstance().getByName(EnumTest.class, "E1"), EnumTest.E1);
	}

	@Test
	public void testSimplifyPath() {
		String[][] testset = new String[][] {
				{ "d://a/b/../../c", "d://c" /* d is considered a protocol! */ },
				{ "d:/a//b/../../c", "d:/c" /* d is considered a protocol! */ },
				{ "file://a/b/c/../d", "file://a/b/d" }, { "file://c:\\a\\b\\c", "file://c:/a/b/c" },
				{ "http://whatever.com/a/c/../q/r", "http://whatever.com/a/q/r" },
				{ "file://////a///b/c/..\\\\d/", "file://a/b/d" },
				{ "file://////a/././b/./c/..\\\\d/", "file://a/b/d" }, { "./b/./c", "b/c" }

		};
		for (String[] test : testset) {
			Assert.assertEquals("simplified folder failed", test[1], DataUtil.getInstance().simplifyFolder(test[0]));
		}

	}

	@Test
	public void testSimpleXsdReader() {
		SimpleXSDReader reader = new SimpleXSDReader();

		MetaData metadata = reader.read(ConfigurationUtil.getInstance().getTestResourcesPath() + "/" + "test1.xsd");

		Assert.assertEquals("All elements are found", metadata.getRoots().size(), 3);
		String[] elementNames = new String[] { "el1Typed", "el2Anonymous", "el3TypedWithSimpleTypes" };
		for (String name : elementNames)
			Assert.assertTrue("All types are found", !metadata.findElementsByName(name).isEmpty());
	}

	@Test
	public void testCDATA() {
		String xml = XMLUtil.CDATA_START + "<a />" + XMLUtil.CDATA_END;
		String test = XMLUtil.getInstance().cvtCDATA2String(xml);
		Assert.assertTrue("CDATA conversion failed", test.equals("<a />"));
	}

	@Test
	public void testXQuery() {
		XQueryTransformer tf = new XQueryTransformer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String xml = ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "xml"
				+ DataUtil.PATH_SEPARATOR + "test1.xml";
		String xquery = ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "xquery"
				+ DataUtil.PATH_SEPARATOR + "test1.xq";
		try {
			tf.transform(new XMLBinder(new FileInputStream(xml)), os, new FileInputStream(xquery));
		} catch (FileNotFoundException e) {
			Assert.assertTrue("Problem transforming test1.xml with test1.xq", false);
		}
	}

	@Test
	public void testSequence() {
		ConfigurationUtil.getInstance().clear();
		ConfigurationUtil.getInstance()
				.init(ConfigurationUtil.getInstance().getTestResourcesPath() + "sequence.test.properties");
		SequenceUtil.getInstance().makeStore("test");
		SequenceUtil.getInstance().delete("test");
		String result = SequenceUtil.getInstance().getNext("test");
		Assert.assertEquals("Unexpected sequence", "ABC2000", result);
		result = SequenceUtil.getInstance().getNext("test");
		Assert.assertEquals("Unexpected sequence", "ABC3000", result);
		Assert.assertFalse(SequenceUtil.getInstance().isDefined("invalid"));

	}

	@Test
	public void testReplacement() {
		String result = StringUtil.getInstance().replace("aa.bb.cc.dd.ee.ff.gg.hh", "(.*)\\.([^\\.]*)\\.([^\\.]*),$2");

		Assert.assertEquals("gg", result);
		result = StringUtil.getInstance().replace("aa.bb.cc.dd.ee.ff.gg.hh", "(.*)\\.([^\\.]*)\\.([^\\.]*),$2$3");
		Assert.assertEquals("gghh", result);

		result = StringUtil.getInstance().replace("aa/bb/cc/dd/ee",
				"(.*\\.Externals\\.)([^\\.]*)\\.(service|reference),$2,(.*\\/)([^\\/]*),$2");

		Assert.assertEquals("ee", result);
		result = StringUtil.getInstance().replace("ApplicationInwinnen/source/Inwinnen/Inwinnen/startBeoordelen",
				"(.*\\.Externals\\.)([^\\.]*)\\.(service|reference),$2,(.*\\/)([^\\/]*),$2");
		Assert.assertEquals("startBeoordelen", result);
	}

	@Test
	public void testXMLCopy() {
		String filename = ConfigurationUtil.getInstance().getTestResourcesPath() + "xml/copytest";
		String input = DataUtil.getInstance().readFromFile(filename + ".xml");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());
		XMLUtil.getInstance().parse(bis, new XMLSAXEchoHandler(bos));
		// we compare to a specific XML because the copy modifies the syntaxis
		// (not the semantics) of the original XML
		String compare = DataUtil.getInstance().readFromFile(filename + "-compare.xml");
		DataUtil.getInstance().writeToFile(filename + "xml.out", bos.toString());
		Assert.assertEquals("comparisson to xml/copytest-compare.xml failed", bos.toString(), compare);
	}
}

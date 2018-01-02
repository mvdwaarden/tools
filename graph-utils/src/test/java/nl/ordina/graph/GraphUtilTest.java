package nl.ordina.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import graph.GraphOption;
import graph.dm.Cluster;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.GraphIndex;
import graph.dm.GraphQuery;
import graph.dm.Node;
import graph.ext.dm.wsdl.OperationNode;
import graph.ext.dm.wsdl.WSDLNode;
import graph.ext.dm.xsd.XSDNode;
import graph.ext.mod.wsdl.WSDLReader;
import graph.ext.mod.xsd.XSDReader;
import graph.ext.parser.wsdl.WSDLSAXHandler;
import graph.ext.parser.xsd.XSDSAXHandler;
import graph.ext.persist.neo4j.Neo4JUtil;
import graph.util.GraphCycleChecker;
import graph.util.GraphUtil;
import json.JSONObject;
import json.JSONUtil;
import object.ObjectUtil;

public class GraphUtilTest {
	public enum EnumTest {
		E1, E2, E3;
	}

	@Test
	public void testCycles() {
		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new Node("0", "WHATEVER"), new Node("1", "WHATEVER"), new Node("2", "WHATEVER"),
				new Node("3", "WHATEVER"),

		};

		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[2], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[2], nodes[3], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[0], nodes[3], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[3], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);

		GraphCycleChecker<Node, Edge<Node>> checker = new GraphCycleChecker<Node, Edge<Node>>();

		List<List<Edge<Node>>> cycles = checker.checkCycles(gra, true);

		for (List<Edge<Node>> cycle : cycles) {
			LogUtil.getInstance().info("Cycle: ");
			for (Edge<Node> edge : cycle) {
				LogUtil.getInstance().info(edge.getSource().getId() + "- [" + edge.getName() + "] -> "
						+ edge.getTarget().getId() + " -> ");
			}
			LogUtil.getInstance().info(" :");
		}
		Assert.assertTrue("cycles = '" + cycles.size() + "' != 7", cycles.size() == 7);
	}

	@Test
	public void testSelfCycles() {
		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new Node("0", "WHATEVER"), new Node("1", "WHATEVER") };

		gra.addEdge(new Edge<>(nodes[0], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);

		GraphCycleChecker<Node, Edge<Node>> checker = new GraphCycleChecker<Node, Edge<Node>>();

		List<List<Edge<Node>>> cycles = checker.checkCycles(gra, true);

		for (List<Edge<Node>> cycle : cycles) {
			LogUtil.getInstance().info("Cycle: ");
			for (Edge<Node> edge : cycle) {
				LogUtil.getInstance().info(edge.getSource().getId() + "- [" + edge.getName() + "] -> "
						+ edge.getTarget().getId() + " -> ");
			}
			LogUtil.getInstance().info(" :");
		}
		Assert.assertTrue("cycles = '" + cycles.size() + "' != 3", cycles.size() == 3);
	}

	@Test
	public void testFindRoots() {
		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new Node("0", "WHATEVER"), new Node("1", "WHATEVER"), new Node("2", "WHATEVER"),
				new Node("3", "WHATEVER"), new Node("4", "WHATEVER") };

		gra.addEdge(new Edge<>(nodes[0], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[2], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[3], nodes[4], "WE"), GraphOption.CHECK_DUPLICATES);

		List<Node> roots = GraphUtil.getInstance().findRoots(gra);

		Assert.assertTrue("root count = '" + roots.size() + "' != 2", roots.size() == 2);
	}

	@Test
	public void testGetSubGraphByRoot() {
		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new Node("0", "WHATEVER"), new Node("1", "WHATEVER"), new Node("2", "WHATEVER"),
				new Node("3", "WHATEVER"), new Node("4", "WHATEVER") };

		gra.addEdge(new Edge<>(nodes[0], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[2], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[3], nodes[4], "WE"), GraphOption.CHECK_DUPLICATES);
		// Add cycle
		gra.addEdge(new Edge<>(nodes[1], nodes[2], "CYCLE"), GraphOption.CHECK_DUPLICATES);
		Graph<Node, Edge<Node>> subGraph = GraphUtil.getInstance().getGraphByNode(gra, nodes[2],
				GraphOption.CHECK_DUPLICATES);

		Assert.assertTrue("subgraph node count = '" + subGraph.getNodes().size() + "' != 3",
				subGraph.getNodes().size() == 3);

	}

	@Test
	public void testReadWsdl() {
		Graph<WSDLNode, Edge<WSDLNode>> wsdlGraph = new Graph<>();
		WSDLReader reader = new WSDLReader(ConfigurationUtil.getInstance().getTestResourcesPath());
		reader.read(ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "test.wsdl",
				new WSDLSAXHandler(wsdlGraph));

		Assert.assertTrue("er bestaand nodes", !wsdlGraph.getNodes().isEmpty());

		Assert.assertTrue("de operation node is gevonden",
				ObjectUtil.getInstance().filter(wsdlGraph.getNodes(), OperationNode.class).size() == 1);
	}

	@Test
	public void testReadXsd() {
		Graph<XSDNode, Edge<XSDNode>> xsdGraph = new Graph<>();
		XSDReader reader = new XSDReader(ConfigurationUtil.getInstance().getTestResourcesPath());
		reader.read(ConfigurationUtil.getInstance().getTestResourcesPath() + DataUtil.PATH_SEPARATOR + "test.xsd",
				new XSDSAXHandler(xsdGraph));

		Assert.assertTrue("er bestaand nodes", !xsdGraph.getNodes().isEmpty());
	}

	@Test
	public void testObject2NV() {
		C c = new C();

		c.setInt1(1);
		c.setInt2(2);
		c.setInt3(3);
		c.setInt4(4);
		c.setStr1("1");
		c.setStr2("2");
		c.setStr3("3");
		c.setEnum1(EnumTest.E1);

		Map<String, Object> map = GraphUtil.getInstance().object2NV(c, A.class);
		Assert.assertTrue("aantal entries is 8", map.size() == 8);

		map = GraphUtil.getInstance().object2NV(c, B.class);
		Assert.assertTrue("aantal entries is 5", map.size() == 5);

		map = GraphUtil.getInstance().object2NV(c, C.class);
		Assert.assertTrue("aantal entries is 3", map.size() == 3);
	}

	@Test
	public void testCreateClusters() {
		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new Node("0_GRP1", "WHATEVER"), new Node("1_GRP2", "WHATEVER"),
				new Node("2_GRP2", "WHATEVER"), new Node("3_GRP1", "WHATEVER"), new Node("4_GRP2", "WHATEVER")

		};

		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[2], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[2], nodes[3], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[3], nodes[4], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[4], nodes[0], "WE"), GraphOption.CHECK_DUPLICATES);

		List<Cluster<Node>> clusters = GraphUtil.getInstance().createClusters(gra,
				n -> n.getId().substring(n.getId().length() - 4));

		Assert.assertEquals("cluster count", 2, clusters.size());
		Assert.assertEquals("cluster1 count", 2, clusters.get(0).getNodes().size());
		Assert.assertEquals("cluster2 count", 3, clusters.get(1).getNodes().size());

	}

	class A {
		private String str1;
		private Integer int1;
		private EnumTest enum1;

		public String getStr1() {
			return str1;
		}

		public void setStr1(String str1) {
			this.str1 = str1;
		}

		public Integer getInt1() {
			return int1;
		}

		public void setInt1(Integer int1) {
			this.int1 = int1;
		}

		public EnumTest getEnum1() {
			return enum1;
		}

		public void setEnum1(EnumTest enum1) {
			this.enum1 = enum1;
		}

	}

	class B extends A {
		private String str2;
		private Integer int2;

		public String getStr2() {
			return str2;
		}

		public void setStr2(String str2) {
			this.str2 = str2;
		}

		public Integer getInt2() {
			return int2;
		}

		public void setInt2(Integer int2) {
			this.int2 = int2;
		}

	}

	class C extends B {
		private String str3;
		private Integer int3;
		private int int4;

		public String getStr3() {
			return str3;
		}

		public void setStr3(String str3) {
			this.str3 = str3;
		}

		public Integer getInt3() {
			return int3;
		}

		public void setInt3(Integer int3) {
			this.int3 = int3;
		}

		public int getInt4() {
			return int4;
		}

		public void setInt4(int int4) {
			this.int4 = int4;
		}
	}

	@Test
	public void testMeta() {
		class MetaA extends Node {
			private String a1;
			private String a2;

			public MetaA(String name, String a1, String a2) {
				super(name);
				setDescription(name);
				setName(name);
				setA1(a1);
				setA2(a2);
			}

			@SuppressWarnings("unused")
			public String getA1() {
				return a1;
			}

			public void setA1(String a1) {
				this.a1 = a1;
			}

			@SuppressWarnings("unused")
			public String getA2() {
				return a2;
			}

			public void setA2(String a2) {
				this.a2 = a2;
			}

		}
		class MetaB extends Node {
			private String b1;
			private String b2;

			public MetaB(String name, String b1, String b2) {
				super(name);
				setDescription(name);
				setName(name);
				setB1(b1);
				setB2(b2);
			}

			@SuppressWarnings("unused")
			public String getB1() {
				return b1;
			}

			public void setB1(String b1) {
				this.b1 = b1;
			}

			@SuppressWarnings("unused")
			public String getB2() {
				return b2;
			}

			public void setB2(String b2) {
				this.b2 = b2;
			}
		}

		Graph<Node, Edge<Node>> gra = new Graph<>();

		Node[] nodes = new Node[] { new MetaA("A1", "a11", "a21"), new MetaA("A2", "a21", "a22 "),
				new MetaB("B1", "b1", "b2"), new MetaB("B2", "b1", "b2") };

		gra.addEdge(new Edge<>(nodes[0], nodes[1], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[2], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[2], nodes[3], "WE"), GraphOption.CHECK_DUPLICATES);
		gra.addEdge(new Edge<>(nodes[1], nodes[3], "WE"), GraphOption.CHECK_DUPLICATES);

		Graph<Node, Edge<Node>> mg = GraphUtil.getInstance().createMetaGraph(gra);

		Assert.assertEquals("number of meta graph nodes equals 2", mg.getNodes().size(), 2);
		Assert.assertEquals("number of meta graph edges equals 3", mg.getEdges().size(), 3);

	}

	@Test
	public void testGraphWalker() {
		Node[] nodes = new Node[] { new Node("a0"), new Node("b1"), new Node("c2"), new Node("d3"), new Node("e4") };

		Graph<Node, Edge<Node>> test = new Graph<>();
		test.setName("test");

		test.addEdge(new Edge<>(nodes[0], nodes[1], "walk"), GraphOption.CHECK_DUPLICATES);
		test.addEdge(new Edge<>(nodes[0], nodes[3], "walk"), GraphOption.CHECK_DUPLICATES);
		test.addEdge(new Edge<>(nodes[1], nodes[2], "walk"), GraphOption.CHECK_DUPLICATES);
		test.addEdge(new Edge<>(nodes[3], nodes[4], "walk"), GraphOption.CHECK_DUPLICATES);
		test.addEdge(new Edge<>(nodes[4], nodes[0], "walk"), GraphOption.CHECK_DUPLICATES);
		List<Node> start = new ArrayList<>();
		start.add(nodes[1]);
		Graph<Node, Edge<Node>> graResult = GraphUtil.getInstance().getGraphByStartNodes(test, start);
		Assert.assertEquals("amount of nodes (one branch)", graResult.getNodes().size(), 2);
		start = new ArrayList<>();
		start.add(nodes[3]);
		graResult = GraphUtil.getInstance().getGraphByStartNodes(test, start);
		Assert.assertEquals("amount of nodes (cycle)", graResult.getNodes().size(), 5);
	}

	@Test
	public void testNeo4JUtil() {
		int[][] verify = new int[][] { { 1, 22, 25 }, { 1, 22, 25 }, { 1, 22, 25 } };

		for (int i = 0; i < verify.length; ++i) {
			if (verify[i][0] > 0) {
				JSONObject json = JSONUtil.getInstance()
						.parseJSON(DataUtil.getInstance()
								.readFromFile(ConfigurationUtil.getInstance().getTestResourcesPath()
										+ "neo4j.response.sample" + (i + 1) + ".json"));

				Graph<Node, Edge<Node>> gra = Neo4JUtil.getInstance().convertNeo4JResult2Graph(json);
				Assert.assertEquals("node count matches", gra.getNodes().size(), verify[i][1]);
				Assert.assertEquals("edge count matches", gra.getEdges().size(), verify[i][2]);
			}
		}
	}

	@Test
	public void testIndex() {
		class ModelNode extends Node {
			public ModelNode(String id, String name) {
				super(id, name, name);
			}
		}
		;
		class ClassNode extends ModelNode {
			public ClassNode(String name) {
				super(name, name);
			}
		}
		;

		class AttributeNode extends ModelNode {
			public AttributeNode(String name) {
				super(name, name);
			}
		}
		;

		Graph<ModelNode, Edge<ModelNode>> model = new Graph<>();

		model.setName("testmodel");
		model.addEdge(new Edge<>(new ClassNode("Car"), new AttributeNode("Brand")),
				GraphOption.CHECK_DUPLICATES);
		model.addEdge(new Edge<>(new ClassNode("Car"), new AttributeNode("TireCount")),
				GraphOption.CHECK_DUPLICATES);
		model.addEdge(new Edge<>(new ClassNode("Car"), new ClassNode("Door")), GraphOption.CHECK_DUPLICATES);
		model.addEdge(new Edge<>(new ClassNode("Door"), new AttributeNode("Window")),
				GraphOption.CHECK_DUPLICATES);
		model.addEdge(new Edge<>(new ClassNode("Door"), new AttributeNode("Knob")),
				GraphOption.CHECK_DUPLICATES);

		Assert.assertEquals("number of nodes", model.getNodes().size(), 6);
		Assert.assertEquals("number of edges", model.getNodes().size(), 6);

		GraphQuery<ModelNode, Edge<ModelNode>> qry = new GraphQuery<>(
				new GraphIndex<ModelNode, Edge<ModelNode>>(model).build(),
				model.filterNodes(n -> n.getId().equals("Car")).get(0));

		Assert.assertEquals("only nested class is a door",qry.f(ClassNode.class).get().getId(),"Door");
		Assert.assertEquals("window attribuut node of a door",qry.f(ClassNode.class).f("Window").get().getId(),"Window");
		Assert.assertEquals("only nested class is a door",qry.getByPath(">Class.id"),"Door");
		Assert.assertEquals("this node's id is a Car",qry.getByPath("id"),"Car");
	}
}

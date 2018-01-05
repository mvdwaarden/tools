package nl.ordina.tools.soa.soatool;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;

import conversion.ConversionUtil;
import csv.CSVColumnHash;
import csv.CSVData;
import csv.CSVRuleExec;
import csv.CSVUtil;
import data.ConfigurationUtil;
import data.DataUtil;
import data.EnumUtil;
import data.LogUtil;
import data.StringUtil;
import graph.GraphOption;
import graph.dm.Cluster;
import graph.dm.ClusterNode;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.dm.Graph;
import graph.dm.GraphIndex;
import graph.dm.GraphQuery;
import graph.dm.Node;
import graph.ext.WSDLConst;
import graph.ext.XSDConst;
import graph.ext.dm.fs.FileNode;
import graph.ext.dm.fs.FileSystemNode;
import graph.ext.dm.wsdl.ImportNode;
import graph.ext.dm.wsdl.InterfaceNode;
import graph.ext.dm.wsdl.WSDLNode;
import graph.ext.dm.xsd.XSDComplexTypeNode;
import graph.ext.dm.xsd.XSDElementNode;
import graph.ext.dm.xsd.XSDNode;
import graph.ext.dm.xsd.XSDSimpleTypeNode;
import graph.ext.mod.wsdl.WSDLReader;
import graph.ext.mod.xsd.XSDReader;
import graph.ext.persist.neo4j.Neo4JEmbeddedConnection;
import graph.ext.persist.neo4j.Neo4JPersist;
import graph.io.GraphConverter;
import graph.io.GraphMetrics;
import graph.link.GraphLink;
import graph.link.GraphLinker;
import graph.util.GraphUtil;
import jee.thread.ManagedThread;
import jee.thread.ThreadUtil;
import json.JSONUtil;
import metadata.MetaComposite;
import metadata.MetaData;
import metadata.MetaElement;
import metadata.MetaType;
import metadata.SimpleXSDReader;
import nl.ordina.tools.gen.XSDGenerator;
import nl.ordina.tools.java.jca.JCAConst;
import nl.ordina.tools.java.jca.dm.JCAAdapterNode;
import nl.ordina.tools.java.jca.dm.JCANode;
import nl.ordina.tools.java.jca.mod.JCAReader;
import nl.ordina.tools.soa.bpel.graph.dm.BPELNode;
import nl.ordina.tools.soa.bpel.graph.dm.InvokeNode;
import nl.ordina.tools.soa.bpel.graph.mod.BPELReader;
import nl.ordina.tools.soa.bpmn.BPMNConst;
import nl.ordina.tools.soa.bpmn.graph.dm.BPMNNode;
import nl.ordina.tools.soa.bpmn.graph.dm.ProcessNode;
import nl.ordina.tools.soa.bpmn.graph.dm.TaskNode;
import nl.ordina.tools.soa.bpmn.mod.BPMNReader;
import nl.ordina.tools.soa.desc.DescisionConst;
import nl.ordina.tools.soa.desc.graph.dm.DescisionNode;
import nl.ordina.tools.soa.desc.mod.DescisionReader;
import nl.ordina.tools.soa.osb.OSBConst;
import nl.ordina.tools.soa.osb.analysis.parsers.ServiceReferenceType;
import nl.ordina.tools.soa.osb.graph.dm.AlertDestinationNode;
import nl.ordina.tools.soa.osb.graph.dm.AlertNode;
import nl.ordina.tools.soa.osb.graph.dm.BindingNode;
import nl.ordina.tools.soa.osb.graph.dm.BusinessServiceNode;
import nl.ordina.tools.soa.osb.graph.dm.DispatchPolicyNode;
import nl.ordina.tools.soa.osb.graph.dm.OSBNode;
import nl.ordina.tools.soa.osb.graph.dm.ProxyServiceNode;
import nl.ordina.tools.soa.osb.graph.mod.OSBReader;
import nl.ordina.tools.soa.osb.test.OSBTestJMS;
import nl.ordina.tools.soa.osb.test.OSBTestService;
import nl.ordina.tools.soa.sca.SCAConst;
import nl.ordina.tools.soa.sca.graph.dm.ComponentNode;
import nl.ordina.tools.soa.sca.graph.dm.Interface;
import nl.ordina.tools.soa.sca.graph.dm.ReferenceNode;
import nl.ordina.tools.soa.sca.graph.dm.SCANode;
import nl.ordina.tools.soa.sca.graph.dm.ServiceNode;
import nl.ordina.tools.soa.sca.graph.dm.WireNode;
import nl.ordina.tools.soa.sca.graph.mod.SCAReader;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUICallNode;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUINode;
import nl.ordina.tools.soa.soapui.graph.mod.SOAPUIConfigurationReader;
import nl.ordina.tools.sql.SQLParser;
import nl.ordina.tools.sql.SQLParserResult;
import nl.ordina.tools.sql.SQLUtil;
import nl.ordina.tools.sql.SQLUtil.Dialect;
import nl.ordina.tools.wls.graph.dm.WLSNode;
import nl.ordina.tools.wls.graph.dm.WorkManagerNode;
import nl.ordina.tools.wls.graph.mod.WLSReader;
import nl.ordina.tools.writer.ParseTreeWriter;
import persist.Connection;
import rest.RESTClient;
import tool.Tool;
import xml.ReportListener;
import xml.XMLExecutionContext;
import xml.XMLSAXHandler;
import xml.XMLTransformer.ScriptType;
import xml.XMLUtil;

public class SOATool extends Tool {
	public enum OracleOption {
		NONE, ORACLE_ANALYSE_ONLY
	}

	private enum EdgeCreatorType {
		CODE, CONFIG;
	}

	public static final String GRAPH_NAME_WLS = "soa-wls";
	public static final String GRAPH_NAME_OSB = "soa-osb";
	public static final String GRAPH_NAME_SCA = "soa-sca";
	public static final String GRAPH_NAME_BPMN = "soa-bpmn";
	public static final String GRAPH_NAME_BPEL = "soa-bpel";
	public static final String GRAPH_NAME_DESC = "soa-desc";
	public static final String GRAPH_NAME_JCA = "soa-jca";
	public static final String GRAPH_NAME_XSD = "soa-xsd";
	public static final String GRAPH_NAME_WSDL = "soa-wsdl";
	public static final String GRAPH_NAME_FLOW = "soa-flow";
	public static final String GRAPH_NAME_CONVENTIONS = "cdm-conventions";
	public static final String GRAPH_NAME_SOA_LINKED = "soa-osb-linked";
	public static final String GRAPH_NAME_TOTAL = "osb";
	public static final String SOA_TOOL_ANALYZER_CLASS_NAME_POSTFIX = "Analyzer";
	public static final String SOA_TOOL_ANALYZERS = "soatool.analyzers";
	public static final String SOA_TOOL_FILE_ARTEFACT_FILTER = "soatool.file.artefact.filter";
	public static final String SOA_TOOL_SQL_DIALECT = "soatool.sql.dialect";
	public static final String SOA_TOOL_WLS_CONFIG = "wls.domain.config";
	public static final String SOA_TOOL_MAPPING_CLUSTER_DDL_PREFIX = "soatool.mapping.cluster.ddl";
	public static final String SOA_TOOL_SERVER_LISTEN_ADDRESS = "soatool.listen.addr";
	public static final String SOA_TOOL_SERVER_LISTEN_PORT = "soatool.listen.port";
	public static final String SOA_TOOL_SINK = "soatool.sink";
	public static final String SOA_TOOL_GENERATION_MAX_FILE = "soatool.max.file";
	public static final String SOA_TOOL_GENERATION_FILE_SIZE = "soatool.file.size";
	public static final String SOA_TOOL_GENERATION_FILE_NAME = "soatool.file.name";
	public static final String SOA_TOOL_CDM_CONVENTIONS_TRANSITIONS = "soatool.cdm.conventions.transitions";
	public static final String SOA_TOOL_CDM_CONVENTIONS_CLUSTERS = "soatool.cdm.conventions.clusters";
	public static final String SOA_TOOL_ARG_MANAGED_THREAD_FACTORY = "mtf";
	public static final String SOA_TOOL_ARG_LOG_SINK = "sink";
	public static final String SOA_TOOL_ARG_LOG_MESSAGE = "msg";
	public static final String SOA_TOOL_FILTER = "soatool.filter";
	public static final String SOA_TOOL_ANONYMIZE_EXTRACT_XSLT = "soatool.anonymize.extract.xslt";
	public static final String SOA_TOOL_ANONYMIZE_EXTRACT_REPLACE = "soatool.anonymize.replace";
	public static final String SOA_TOOL_ANONYMIZE_EXTRACT_FILE_FILTER_REGEX = "soatool.anoext.filter.regex";
	public static final String SOA_TOOL_ANONIMIZE_VALID_XML_PATH_REGEX = "soatool.ano.valid.xml.path.regex";
	public static final String SOA_TOOL_XSLT_MERGE = "soatool.xslt.merge";
	public static final String SOA_TOOL_XSLT_ANONYMIZE_WORK = "soatool.xslt.anonymize.work";
	public static final String SOA_TOOL_XQUERY = "soatool.xquery";
	public static final String SOA_TOOL_XSD_VALIDATION_FILE = "soatool.xsd.validation.file";
	public static final String SOA_TOOL_ANONYMIZED_DATA_FILE = "soatool.anonymized.data.file";
	public static final String SOATOOL_MAPXML_TEST_LOCALNAME_REGEX = "soatool.mapxml.test.localname.regex";
	public static final String GRAPH_AUTO_CONVERT = "soatool.graph.auto.convert";
	public static final String SOATOOL_CVT_REPLACE = "soatool.cvt.replace";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL1_REPLACE = "soatool.html.table.extract.col1.replace";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL2_REPLACE = "soatool.html.table.extract.col2.replace";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL3_REPLACE = "soatool.html.table.extract.col3.replace";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL1_NAME = "soatool.html.table.extract.col1.name";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL2_NAME = "soatool.html.table.extract.col2.name";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_COL3_NAME = "soatool.html.table.extract.col3.name";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_CONFIG = "soatool.html.table.extract.config";
	public static final String SOA_TOOL_HTML_TABLE_EXTRACT_TABLE_RULE_CONFIG = "soatool.html.table.extract.table.rule.config";
	public static final String SOA_TOOL_CSV_RULE_EXECUTE_RULE_CONFIG_INFERRED = "soatool.csv.rule.execute.rule.config.inferred";
	public static final String SOA_TOOL_CSV_RULE_EXECUTE_RULE_CONFIG = "soatool.csv.rule.execute.rule.config";
	public static final String FUNCTION_GET_WSDL_ARTIFACTS = "wsdl";
	public static final String FUNCTION_HTML_TABLE_EXTRACT = "htmltabext";
	public static final String FUNCTION_SOA_TOOL_SRV = "soatoolsrv";
	public static final String FUNCTION_CSV_RULE_EXECUTE = "csvruleexec";
	public static final String FUNCTION_LIST_CONFIGURATIONS = "list";
	public static final String FUNCTION_ORACLE_ANALYSIS = "orcl";
	public static final String FUNCTION_HEX_CLEAN = "hexclean";
	public static final String FUNCTION_SLOW_SERVER = "slow";
	public static final String FUNCTION_JMS_SERVER = "jms";
	public static final String FUNCTION_LOG = "log";
	public static final String FUNCTION_PRINT_CHARSET = "pcs";
	public static final String FUNCTION_ADF_ANALYSE = "adfanalyse";
	public static final String FUNCTION_CREATE_FILES = "genfiles";
	public static final String FUNCTION_EXTRACT_FILE_CONTENT = "efc";
	public static final String FUNCTION_CONVERT = "cvt";
	public static final String FUNCTION_COMBINE_XML_FILES = "combine";
	public static final String FUNCTION_LARGE_FILE = "largefile";
	public static final String FUNCTION_SOAP_ANALYSIS = "soapana";
	public static final String FUNCTION_DDL_ANALYSIS = "ddl";
	public static final String FUNCTION_JMC_CSV_FLATTEN = "jmcflatten";
	public static final String FUNCTION_JMC_CSV_CORRECT = "jmccsvcorrect";
	public static final String FUNCTION_ANONIMIZE_EXTRACT = "anoext";
	public static final String FUNCTION_ANONIMIZE = "ano";
	public static final String FUNCTION_OEQ_ANALYSIS = "oeq";
	public static final String FUNCTION_XSD_ANALYSIS = "xsd";
	public static final String FUNCTION_SOAPUI_ANALYSIS = "soapui";
	public static final String FUNCTION_MAPPING_ANALYSIS = "map";
	public static final String FUNCTION_MAP_XML = "mapxml";
	public static final String FUNCTION_FIND_FILES = "ff";
	public static final String EDGE_TYPE_IMPLEMENTED_BY = "IMPLEMENTED_BY";
	public static final String EDGE_TYPE_DEFINED_BY = "DEFINED_BY";
	public static final String EDGE_TYPE_ISA = "ISA";
	public static final String EDGE_TYPE_WIRED_TO = "WIRED_TO";
	public static final String EDGE_TYPE_REVERSE_WIRED_TO = "REVERSE_WIRED_TO";
	public static final String EDGE_TYPE_CALLS = "CALLS";
	public static final String EDGE_TYPE_CALLS_QUEUE_OR_DB = "QUEUE_DB";
	public static final String EDGE_TYPE_CALLS_WS = "WS";
	public static final String NEO4J_MAPPING_STORE = "map";
	public static final String NEO4J_ORACLE_STORE = "orcl";
	public static final String REST_REPORTS_DIR = "reports";
	private static final String[] CSV_HEADER_WSDL_ARTEFACTS = new String[] { "FULL_FILENAME", "RELATIVE_FILENAME" };
	public static final String SOATOOL_GRAPH_MARKUP = "soatool.graph.markup";

	/* Cache */
	CSVData cacheNodeMarkup;
	public static final Comparator<Node> JOIN_IMPORT_NODE_XSD_NODE = (o1, o2) -> {
		if ((o1 instanceof ImportNode && o2 instanceof XSDNode)
				|| (o2 instanceof ImportNode && o1 instanceof XSDNode)) {
			if (o1.getId().equals(o2.getId()))
				return 0;
			else
				return 1;
		} else
			return (o1.treatAsSame(o2)) ? 0 : 1;
	};

	public class CallEdge extends Edge<Node> {
		private EdgeCreatorType creator;

		public CallEdge(Node source, Node target) {
			super(source, target, EDGE_TYPE_CALLS);
			this.creator = EdgeCreatorType.CODE;
		}

		public EdgeCreatorType getCreator() {
			return creator;
		}

		public void setCreator(EdgeCreatorType creator) {
			this.creator = creator;
		}
	}

	public class ConnectOSBBinding2WSDL extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof nl.ordina.tools.soa.osb.graph.dm.BindingNode && o2.getClass() == WSDLNode.class
					&& null != ((nl.ordina.tools.soa.osb.graph.dm.BindingNode) o1).getWsdl())
				if (o2.getId().equalsIgnoreCase(((nl.ordina.tools.soa.osb.graph.dm.BindingNode) o1).getWsdl()))
					result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_DEFINED_BY));

			return result;
		}
	};

	/**
	 * Connect an OSB business service or proxy service to a WSDL binding.
	 */
	public class ConnectOSBBinding2WSDLBinding extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof nl.ordina.tools.soa.osb.graph.dm.BindingNode
					&& o2 instanceof graph.ext.dm.wsdl.BindingNode) {
				nl.ordina.tools.soa.osb.graph.dm.BindingNode bn = (nl.ordina.tools.soa.osb.graph.dm.BindingNode) o1;
				graph.ext.dm.wsdl.BindingNode wbn = (graph.ext.dm.wsdl.BindingNode) o2;

				if (null != bn.getBindingName() && null != bn.getBindingNamespace() && null != wbn.getId()
						&& wbn.getId().equalsIgnoreCase(
								bn.getBindingNamespace() + DataUtil.PATH_SEPARATOR + bn.getBindingName()))
					result = true;
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_DEFINED_BY));

			return result;
		}
	};

	/**
	 * Connect an alert to an alert destination
	 */

	public class ConnectOSBAlert2AlertDestination extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof AlertNode && o2 instanceof AlertDestinationNode) {
				AlertNode an = (AlertNode) o1;
				AlertDestinationNode adn = (AlertDestinationNode) o2;

				if (null != an.getDestination() && adn.getId().equalsIgnoreCase(an.getDestination()))
					result = true;
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_DEFINED_BY));
			return result;
		}
	};

	/**
	 * Connect an OSB business service or proxy service to a WSDL binding.
	 */
	public class ConnectSCAInterface2ProxyService extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof ReferenceNode && o2 instanceof ProxyServiceNode) {
				ReferenceNode ref = (ReferenceNode) o1;
				ProxyServiceNode pxy = (ProxyServiceNode) o2;

				GraphQuery<Node, Edge<Node>> qryRef = new GraphQuery<>(idx, ref);
				GraphQuery<Node, Edge<Node>> qryPxy = new GraphQuery<>(idx, pxy);

				WSDLNode refWSDL = qryRef.f(WSDLNode.class).<WSDLNode>get();
				if (null != refWSDL) {
					WSDLNode pxyWSDL = qryPxy.f(WSDLNode.class).<WSDLNode>get();
					if (null != pxyWSDL) {
						result = refWSDL.treatAsSame(pxyWSDL);
					}
				}
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();

			result.add(new CallEdge(source, target));
			return result;
		}
	};

	public class ConnectBusinessService2SCAInterface extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof BusinessServiceNode && o2 instanceof ServiceNode) {
				ServiceNode ref = (ServiceNode) o2;
				BusinessServiceNode pxy = (BusinessServiceNode) o1;

				GraphQuery<Node, Edge<Node>> qryRef = new GraphQuery<>(idx, ref);
				GraphQuery<Node, Edge<Node>> qryPxy = new GraphQuery<>(idx, pxy);

				WSDLNode refWSDL = qryRef.f(WSDLNode.class).<WSDLNode>get();
				if (null != refWSDL) {
					WSDLNode pxyWSDL = qryPxy.f(WSDLNode.class).<WSDLNode>get();
					if (null != pxyWSDL) {
						result = refWSDL.treatAsSame(pxyWSDL);
					}
				}
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();

			result.add(new CallEdge(source, target));
			return result;
		}
	};

	public class ConnectSCAServices2SCAReferences extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof ReferenceNode && o2 instanceof ServiceNode) {
				ReferenceNode ref = (ReferenceNode) o1;
				ServiceNode svc = (ServiceNode) o2;
				if (ref.getWsdlInterface().equalsIgnoreCase(svc.getWsdlInterface()))
					result = true;
				else {
					GraphQuery<Node, Edge<Node>> qryRef = new GraphQuery<>(idx, ref);
					GraphQuery<Node, Edge<Node>> svcRef = new GraphQuery<>(idx, svc);

					JCAAdapterNode refAdapter = qryRef.f(WSDLNode.class).f(JCANode.class).f(JCAAdapterNode.class)
							.<JCAAdapterNode>get();
					if (null != refAdapter) {
						JCAAdapterNode svcAdapter = svcRef.f(WSDLNode.class).f(JCANode.class).f(JCAAdapterNode.class)
								.<JCAAdapterNode>get();
						if (null != svcAdapter) {
							result = refAdapter.treatAsSame(svcAdapter);
						}
					}
				}
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			ReferenceNode ref = (ReferenceNode) source;
			ServiceNode svc = (ServiceNode) target;
			if (ref.getWsdlInterface().equalsIgnoreCase(svc.getWsdlInterface()))
				result.add(new Edge<>(source, target, EDGE_TYPE_CALLS_WS));
			else
				result.add(new Edge<>(source, target, EDGE_TYPE_CALLS_QUEUE_OR_DB));
			return result;
		}
	};

	public class ConnectSCAInterface2WSDL extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof Interface && o2 instanceof InterfaceNode) {
				Interface wpn = (Interface) o1;
				InterfaceNode ifn = (InterfaceNode) o2;
				String wsdlInterface = wpn.getWsdlInterface().replaceAll("#[^\\.]*\\.interface\\(([^\\)]*)\\)", "/$1");
				if (wsdlInterface.equals(ifn.getId()))
					result = true;
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_IMPLEMENTED_BY));

			return result;
		}
	};

	/**
	 * Connect business service to proxy services IF (a) the endpoints in the
	 * business service and the proxy service 'overlap' AND the providers are the
	 * same (i.e. jms, http etc).
	 */
	public class ConnectOSBBusinessService2ProxyServiceByEndpoints extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof BusinessServiceNode && o2 instanceof ProxyServiceNode) {
				ProxyServiceNode pn = (ProxyServiceNode) o2;
				BusinessServiceNode bn = (BusinessServiceNode) o1;

				if (null != pn.getProviderId() && pn.getProviderId().equals(bn.getProviderId())
						&& hasOverlappingEndpoints(bn, pn)) {
					result = true;
				}
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new CallEdge(source, target));

			return result;
		}
	};

	public class ConnectXSDElement2SimpleTypeOrComplexTypeOrElement extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if ((o1 instanceof XSDElementNode && o2 instanceof XSDSimpleTypeNode
					&& (((XSDSimpleTypeNode) o2).getId().equals(((XSDElementNode) o1).getType())))
					|| (o1 instanceof XSDElementNode && o2 instanceof XSDComplexTypeNode
							&& (((XSDComplexTypeNode) o2).getId().equals(((XSDElementNode) o1).getType())))
					|| (o1 instanceof XSDElementNode && o2 instanceof XSDElementNode
							&& (((XSDElementNode) o2).getId().equals(((XSDElementNode) o1).getRef()))))
				result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_ISA));

			return result;
		}
	};

	public class ConnectXSDComplexTypeInheritance extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof XSDComplexTypeNode && o2 instanceof XSDComplexTypeNode) {
				XSDComplexTypeNode ctn1 = (XSDComplexTypeNode) o1;
				XSDComplexTypeNode ctn2 = (XSDComplexTypeNode) o2;
				if (null != ctn2.getBase() && ctn2.getBase().equals(ctn1.getId()))
					result = true;
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_ISA));

			return result;
		}
	};

	public class ConnectWSDL2JCA extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1.getClass() == WSDLNode.class && o2.getClass() == JCANode.class)
				if (o1.getId().equalsIgnoreCase(((JCANode) o2).getWsdl()))
					result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_DEFINED_BY));

			return result;
		}
	};

	public class ConnectTask2Reference extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;
			// task nodes van type PROCESS_CALL NIET linken, gebeurt in
			// NodeFactory op basis van process events
			if (o1 instanceof TaskNode && o2 instanceof Interface
					&& (((TaskNode) o1).getType() == TaskNode.Type.SERVICE_CALL
							|| ((TaskNode) o1).getType() == TaskNode.Type.SEND_CALL
							|| ((TaskNode) o1).getType() == TaskNode.Type.RECEIVE_CALL))
				if (DataUtil.getInstance().removeExtension(o2.getId()).toLowerCase()
						.endsWith(((TaskNode) o1).getImplementation().toLowerCase()))
					result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			if (((TaskNode) source).getType() == TaskNode.Type.RECEIVE_CALL)
				result.add(new CallEdge(target, source));
			else
				result.add(new CallEdge(source, target));

			return result;
		}
	};

	public class ConnectComponent2Implementation extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof ComponentNode && (o2 instanceof ProcessNode || o2 instanceof BPELNode))
				if (null != ((ComponentNode) o1).getSource()
						&& o2.getId().toLowerCase().endsWith(((ComponentNode) o1).getSource().toLowerCase()))
					result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_IMPLEMENTED_BY));

			return result;
		}
	};

	public class ConnectDispatchPolicy2Workmanager extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof DispatchPolicyNode && o2 instanceof WorkManagerNode)
				if (o1.getId().equalsIgnoreCase(o2.getId()))
					result = true;

			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EdgeType.HAS));

			return result;
		}
	};

	public class ConnectBPELInvoke2BusinessService extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o2 instanceof BindingNode && o1 instanceof InvokeNode) {
				InvokeNode in = (InvokeNode) o1;
				BindingNode bn = (BindingNode) o2;
				if (bn.getId().equalsIgnoreCase(in.getReference()))
					result = true;
			} else if (o1 instanceof BindingNode && o2 instanceof BPELNode) {
				BindingNode bn = (BindingNode) o1;
				BPELNode bpel = (BPELNode) o2;
				if (null != bn.getEndpoints()) {
					for (String ep : bn.getEndpoints())
						if (ep.toLowerCase().endsWith(bpel.getId().toLowerCase())) {
							result = true;
							break;
						}
				}
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			if (source instanceof InvokeNode)
				result.add(new CallEdge(source, target));
			else
				result.add(new Edge<>(source, target, EDGE_TYPE_ISA));

			return result;
		}
	};

	public class ConnectComponent2WSDL extends GraphLink<Node, Edge<Node>> {
		@Override
		public boolean linkit(GraphIndex<Node, Edge<Node>> idx, Node o1, Node o2) {
			boolean result = false;

			if (o1 instanceof Interface && o2.getClass() == WSDLNode.class) {
				Interface rn = (Interface) o1;
				if (null != rn.getWsdlLocation() && !rn.getWsdlLocation().isEmpty()
						&& rn.getWsdlLocation().equalsIgnoreCase(o2.getId()))
					result = true;
			}
			return result;
		}

		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			List<Edge<Node>> result = new ArrayList<>();
			result.add(new Edge<>(source, target, EDGE_TYPE_DEFINED_BY));

			return result;
		}
	};

	public static void main(String args[]) {
		SOATool tool = new SOATool();

		tool.run(args);
	}

	/**
	 * Deze functie extraheert anonimiserings gegevens van test XML's bestanden.
	 * Deze geanonimiseerde gegevens kunnen vervolgens weer gebruikt worden, voor
	 * het anonimseren zelf.
	 * 
	 * <pre>
	 * 	- creeert een filelist.xml voor de bestanden in een map
	 *  - transformeert alle bestanden, filelist.xml is de input voor de XSL die is gedefinieerd door 
	 *    de setting : soatool.xslt.anonymize.extract
	 *  - de geanonimiseerde data (geextraheerd) staat in 1 document en wordt naar de DOEL map gekopieerd.
	 * </pre>
	 * 
	 * @param function
	 *            de gebruikte functie
	 * @param sourcedir
	 *            bron map
	 * @param targetdir
	 *            doel map
	 * @param options
	 *            eventuele opties
	 */
	public void anonymizeExtract(String function, String sourcedir, String targetdir, Option... options) {
		List<String[]> report = new ArrayList<>();
		ReportListener lsnr = new ReportListener() {
			@Override
			public void report(Class<?> cls, String step, String msg) {
				report.add(new String[] { step, msg });
			}
		};
		XMLExecutionContext ctx = new XMLExecutionContext(lsnr);
		/* extract anonymization data from XML files */
		ctx.load("load dir", sourcedir,
				f -> f.getAbsolutePath()
						.matches(ConfigurationUtil.getInstance()
								.getSetting(SOA_TOOL_ANONYMIZE_EXTRACT_FILE_FILTER_REGEX, "(?i).*xml")))
				.copy("copy filelist", sourcedir + DataUtil.PATH_SEPARATOR + "filelist.xml")
				.transform("extract data", ConfigurationUtil.getInstance().getSetting(SOA_TOOL_ANONYMIZE_EXTRACT_XSLT),
						ScriptType.XSLT)
				.subst("replace values", ConfigurationUtil.getInstance().getSetting(SOA_TOOL_ANONYMIZE_EXTRACT_REPLACE))
				.copy("save anonimized source", ConfigurationUtil.getInstance().getSetting(
						SOA_TOOL_ANONYMIZED_DATA_FILE, targetdir + DataUtil.PATH_SEPARATOR + "anonymized.xml"));
	}

	public void anonymize(String function, String sourcedir, String targetdir, Option... options) {
		List<String[]> report = new ArrayList<>();
		ReportListener lsnr = new ReportListener() {
			@Override
			public void report(Class<?> cls, String step, String msg) {
				report.add(new String[] { step, msg });
			}
		};
		XMLExecutionContext ctx = new XMLExecutionContext(lsnr);
		Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(sourcedir);

		String validXMLPathRegex = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_ANONIMIZE_VALID_XML_PATH_REGEX);
		int i = 0;
		int total = fsg.getNodes().size();
		for (FileSystemNode fsn : fsg.filterNodes(f -> f instanceof FileNode && (null != validXMLPathRegex
				&& !validXMLPathRegex.isEmpty() && XMLUtil.getInstance().check(f.getId(), (event, path, uri, localName,
						qName, attr, data) -> null != path && path.matches(validXMLPathRegex))))) {
			FileNode fn = (FileNode) fsn;
			ctx = new XMLExecutionContext(lsnr);
			ctx.load("load", fn.getId())
					.transform("anonymize", ConfigurationUtil.getInstance().getSetting(SOA_TOOL_XSLT_ANONYMIZE_WORK),
							ScriptType.XSLT)
					.copy("copy", targetdir + DataUtil.PATH_SEPARATOR + DataUtil.getInstance().getFilename(fn.getId()));
			if (total > 0 && i % (total / 100) == 0)
				LogUtil.getInstance().info("transformed [" + i * 100 / total + "]%");
			++i;
		}
		if (total > 0)
			LogUtil.getInstance().info("transformed [100]%");
	}

	public void hexClean(String function, String sourcedir, String targetdir, String sourcefile, Option... options) {
		String inputfile = sourcefile;
		String outputfile = sourcefile + ".cleaned";

		try (FileOutputStream os = new FileOutputStream(outputfile)) {
			DataUtil.getInstance().readBlocksFromFile(inputfile, 10000, (int blocknr, int blocksize,
					int currentReadbytes, byte[] currentBlock, int[] readbytes, byte[][] blockswindow) -> {
				int resultSize = 0;
				byte[] result = new byte[blocksize];
				for (int i = 0; i < currentReadbytes; ++i) {
					byte b = currentBlock[i];
					if ((b >= (byte) '0' && b <= (byte) '9') || (b >= (byte) 'A' && b <= (byte) 'F')
							|| (b >= (byte) 'a' && b <= (byte) 'f'))
						result[resultSize++] = b;
				}

				if (resultSize > 0)
					try {
						os.write(result, 0, resultSize);
					} catch (IOException e) {
						LogUtil.getInstance().error("problem write output to clean hexfile : [" + outputfile + "]");
					}
				return true;
			});
		} catch (Exception e) {
			LogUtil.getInstance().error("problem cleaning hexfile : [" + inputfile + "]", e);
		}

	}

	public void JMCCSVCorrect(String function, String sourcedir, String targetdir, String sourcefile,
			Option... options) {
		String inputfile = sourcefile;
		String outputfile = targetdir + DataUtil.PATH_SEPARATOR + "out.csv";

		CSVUtil.getInstance().concatShiftUp(inputfile, outputfile, ';', 3, CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
	}

	public void JMCFlatten(String function, String sourcedir, String targetdir, Option... options) {
		String outputfile = targetdir + DataUtil.PATH_SEPARATOR + "flattened.txt";
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputfile))) {
			forEachDir(sourcedir, "(?i).*\\.csv", (fsg) -> {
				for (FileSystemNode fn : fsg.filterNodes(f -> f instanceof FileNode)) {
					CSVUtil.getInstance().readFromFile(fn.getId(), ';', (int linenr, String[] row) -> {
						try {
							os.write(row[0].getBytes());
							if (row.length >= 0 && null != row[row.length - 1] && !row[row.length - 1].isEmpty())
								os.write("\n".getBytes());
						} catch (IOException e) {
							LogUtil.getInstance().info("problem creating file [" + outputfile + "], line [" + linenr
									+ "] from file [" + fn.getId() + "]");
						}
					});
				}
				return true;
			});
		} catch (Exception e) {
			LogUtil.getInstance().info("problem creating file [" + outputfile + "]");
		}
	}

	public void convertFiles(String function, String sourcedir, String targetdir, Option... options) {
		forEachDir(sourcedir, (fsg) -> {
			for (FileSystemNode fn : fsg.filterNodes(f -> f instanceof FileNode)) {
				String replaceConfig = ConfigurationUtil.getInstance().getSetting(SOATOOL_CVT_REPLACE + "."
						+ DataUtil.getInstance().getFilenameWithoutExtension(fn.getName()).toLowerCase(), "");
				ConversionUtil.getInstance().CSV2XML(fn.getId(), ',',
						targetdir + DataUtil.PATH_SEPARATOR
								+ DataUtil.getInstance().getFilenameWithoutExtension(fn.getId()) + ".xml",
						replaceConfig, CSVUtil.Option.TRIM_VALUES);
			}
			return true;
		});
	}

	public void combineXMLFiles(String function, String sourcedir, String targetdir, Option... options) {
		XMLExecutionContext ctx = new XMLExecutionContext();

		ctx.combine("combine files", sourcedir, "(?i).*\\.xml", targetdir + DataUtil.PATH_SEPARATOR + "combined.xml",
				"https://combined", "com", "combined");
	}

	public void analyzeADFLogFiles(String function, String sourcedir, String targetdir, Option... options) {
		int nr = 0;
		forEachDir(sourcedir, (fsg) -> {
			LogUtil.getInstance().info("Analyzing ADF content");
			String outputFilename = targetdir + DataUtil.PATH_SEPARATOR + "adf-analysis.log";
			String outputConcatFilename = targetdir + DataUtil.PATH_SEPARATOR + "adf-concat.log";
			String outputSQLFilename = targetdir + DataUtil.PATH_SEPARATOR + "adf-sql.log";

			try (OutputStream out = new FileOutputStream(outputFilename);
					OutputStream outcat = new FileOutputStream(outputConcatFilename);
					OutputStream outsql = new FileOutputStream(outputSQLFilename)) {
				for (FileSystemNode fn : fsg.filterNodes(f -> f instanceof FileNode)) {
					LogUtil.getInstance().info("Start analyzing ADF content, file " + fn.getId());
					class Locals {
						String previousline = "";
						String currentline = "";
						Date previoustime;
						Date nexttime;
						long diff = 0;
						SimpleDateFormat fmtIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
						SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
					}
					Function<String, String> csvwaarde = (str) -> {
						String result = str;
						if (str.contains(";"))
							result = str.replace(";", ",");
						if (result.startsWith("["))
							result = result.substring(1);
						if (result.endsWith("]"))
							result = result.substring(0, result.length() - 1);

						return result;
					};
					Function<Locals, String> catmapper = (_locals) -> {
						String result = "";
						if (null != _locals.previoustime) {
							result = _locals.fmtOut.format(_locals.previoustime) + ";" + _locals.diff;
							String[] fields = StringUtil.getInstance().split(_locals.previousline, " ", "[", "]");
							// append server
							if (fields.length > 0 && fields[1].length() > 2)
								result += ";" + csvwaarde.apply(fields[1]);
							else
								result += ";onbekend";
							// append full class name and class name
							if (fields.length > 3 && fields[4].length() > 2)
								result += ";" + csvwaarde.apply(fields[4]) + ";" + csvwaarde
										.apply(DataUtil.getInstance().getFilenameWithoutExtension(fields[4]));
							else
								result += ";onbekend;onbekend";
							// append full alternative class name
							if (fields.length > 9) {
								String[] parts = fields[9].split(":");
								if (parts.length > 1)
									result += ";" + csvwaarde.apply(parts[0]) + ";" + csvwaarde.apply(parts[1]);
								else if (parts.length > 0 && parts[0].length() > 1)
									result += ";" + csvwaarde.apply(parts[0]) + ";onbekend";
								else
									result += ";onbekend;onbekend";
							} else
								result += ";onbekend;onbekend";
							// append full alternative method name
							if (fields.length > 10) {
								String[] parts = fields[10].split(":");
								if (parts.length > 1)
									result += ";" + csvwaarde.apply(parts[0]) + ";" + csvwaarde.apply(parts[1]);
								else if (parts.length > 0 && parts[0].length() > 1)
									result += ";" + csvwaarde.apply(parts[0]) + ";onbekend";
								else
									result += ";onbekend;onbekend";
							} else
								result += ";onbekend;onbekend";
							// append classification
							if (null != _locals.previousline
									&& (_locals.previousline.contains("jbo") || _locals.previousline.contains("Query=")
											|| _locals.previousline.contains("SELECT")))
								result += ";database\n";
							else if (_locals.diff > 50000)
								result += ";big_50s\n";
							else if (_locals.diff > 10000 && _locals.diff <= 50000)
								result += ";big_10_50s\n";
							else
								result += ";anders\n";
						}
						return result;
					};
					Function<Locals, String> sqlmapper = (_locals) -> {
						String result = "";
						if (null != _locals.previoustime) {
							int idx = (null != _locals.previousline) ? _locals.previousline.indexOf("SELECT") : -1;

							if (idx > 0)
								result = _locals.fmtOut.format(_locals.previoustime) + ";" + _locals.diff + ";"
										+ _locals.previousline.substring(idx).replace(";", "|");
						}
						return result;
					};

					Locals _locals = new Locals();
					DataUtil.getInstance().readLinesFromFile(fn.getId(), null, null, (idx, line) -> {
						if (line.length() > 10 && line.substring(0, 11).matches("\\[\\d\\d\\d\\d\\-\\d\\d-\\d\\d")) {
							// calculate time
							try {
								_locals.nexttime = _locals.fmtIn.parse(line.substring(1, 24));
							} catch (ParseException ep) {
								_locals.nexttime = null;
							}
							if (null != _locals.previousline && _locals.previousline.length() > 23) {
								try {
									_locals.previoustime = _locals.fmtIn.parse(_locals.previousline.substring(1, 24));
								} catch (ParseException ep) {
									_locals.previoustime = null;
								}
							}
							if (null != _locals.previoustime) {
								if (null != _locals.nexttime)
									_locals.diff = _locals.nexttime.getTime() - _locals.previoustime.getTime();
								else
									_locals.diff = 0;
							}
							// do the stuf
							_locals.currentline.replaceAll("\n", "");
							_locals.currentline += "\n";
							_locals.previousline = _locals.currentline;
							_locals.currentline = line;
							try {
								String mappedline = catmapper.apply(_locals);
								if (mappedline.length() > 0)
									out.write(mappedline.getBytes());
								mappedline = sqlmapper.apply(_locals);
								if (mappedline.length() > 0)
									outsql.write(mappedline.getBytes());
								outcat.write(_locals.currentline.getBytes());

							} catch (IOException e) {
								LogUtil.getInstance().error("problem  writing file for content", e);
							}

						} else {
							_locals.currentline += line;
						}
					});
					try {
						String mappedline = catmapper.apply(_locals);
						if (mappedline.length() > 0)
							out.write(mappedline.getBytes());
						mappedline = sqlmapper.apply(_locals);
						if (mappedline.length() > 0)
							outsql.write(mappedline.getBytes());
						outcat.write(_locals.currentline.getBytes());
					} catch (IOException e) {
						LogUtil.getInstance().error("problem  writing file for content", e);
					}
					LogUtil.getInstance().info("Done analyzing ADF content, file " + fn.getId());
				}
			} catch (Exception ex) {
				LogUtil.getInstance().error("problem creating output file [" + outputFilename + "]", ex);
			}
			return true;
		});
		LogUtil.getInstance().info("Created [" + nr + "] files in [" + targetdir + "]");

	}

	public void extractFileContent(String function, String sourcedir, String targetdir, Option... options) {
		class Local {
			int nr = 0;
		}
		;
		Local _local = new Local();
		forEachDir(sourcedir, (fsg) -> {
			LogUtil.getInstance().info("Extracting file content");
			for (FileSystemNode fn : fsg.filterNodes(f -> f instanceof FileNode)) {
				class LoopVar {
					boolean started;
					boolean stopped;
					FileOutputStream fos;
					int nr;
					int sequence;
				}
				;
				final LoopVar lvI = new LoopVar();
				lvI.started = false;
				lvI.stopped = false;
				lvI.nr = _local.nr;
				lvI.sequence = 0;
				try {
					DataUtil.getInstance().readLinesFromFile(fn.getId(), null, null, (idx, line) -> {
						String correctedLine = line;
						try {
							if (!lvI.started) {
								int idxStart = line.indexOf("<?xml");
								if (idxStart >= 0) {
									DataUtil.getInstance().close(lvI.fos);
									String filename = targetdir + DataUtil.PATH_SEPARATOR
											+ DataUtil.getInstance().getFilenameWithoutExtension(fn.getId()) + "."
											+ (lvI.nr + lvI.sequence++) + ".request.xml";
									lvI.fos = new FileOutputStream(filename);
									lvI.started = true;
									correctedLine = line.substring(idxStart);
								} else
									correctedLine = null;
							}
							if (lvI.started) {
								int idxStop = line.indexOf("Envelope>");
								if (idxStop >= 0) {
									lvI.stopped = true;
									correctedLine = line.substring(0, idxStop + "Envelope>".length());
								}
							}
							if (null != correctedLine) {
								lvI.fos.write(line.getBytes());
							}
							if (lvI.stopped) {
								lvI.stopped = false;
								lvI.started = false;
								DataUtil.getInstance().close(lvI.fos);

							}
						} catch (Exception e) {
							LogUtil.getInstance().error("problem creating files for content", e);
						}
					});
				} finally {
					DataUtil.getInstance().close(lvI.fos);
				}
				_local.nr += lvI.sequence;
			}
			return true;
		});
		LogUtil.getInstance().info("Created [" + _local.nr + "] files in [" + targetdir + "]");
	}

	@Override
	public void dispatch(String function, String configuration, String sourcedir, String targetdir, String sourcefile,
			String[] args, Option... options) {
		boolean autoconvert = ConfigurationUtil.getInstance().getBooleanSetting(GRAPH_AUTO_CONVERT, false);

		DataUtil.getInstance().makeDirectories(targetdir + DataUtil.PATH_SEPARATOR);

		if (null == function || function.isEmpty())
			function = FUNCTION_ORACLE_ANALYSIS;
		if (function.equals(FUNCTION_HEX_CLEAN)) {
			hexClean(function, sourcedir, targetdir, sourcefile, options);
		} else if (function.equals(FUNCTION_SOAP_ANALYSIS)) {
			analyzeSOAPMessages(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_PRINT_CHARSET)) {
			printCharset(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_JMC_CSV_CORRECT)) {
			JMCCSVCorrect(function, sourcedir, targetdir, sourcefile, options);
		} else if (function.equals(FUNCTION_LOG)) {
			log(function, sourcedir, targetdir, StringUtil.getInstance().getArgument(args, SOA_TOOL_ARG_LOG_SINK),
					StringUtil.getInstance().getArgument(args, SOA_TOOL_ARG_LOG_MESSAGE), options);
		} else if (function.equals(FUNCTION_FIND_FILES)) {
			findfiles(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_ANONIMIZE_EXTRACT)) {
			anonymizeExtract(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_ANONIMIZE)) {
			anonymize(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_CREATE_FILES)) {
			createFiles(sourcedir, targetdir);
		} else if (function.equals(FUNCTION_ORACLE_ANALYSIS)) {
			analyzeOracle(FUNCTION_ORACLE_ANALYSIS, sourcedir, targetdir, OracleOption.NONE, options);
		} else if (function.equals(FUNCTION_SLOW_SERVER) && EnumUtil.getInstance().contains(options, Option.CLIENT)) {
			startSlowServer(sourcedir, targetdir);
		} else if (function.equals(FUNCTION_JMS_SERVER) && EnumUtil.getInstance().contains(options, Option.CLIENT)) {
			String server = StringUtil.getInstance().getArgument(args, "server", "default");
			startJMSRequestResponse(sourcedir, targetdir, server);
		} else if (function.equals(FUNCTION_LARGE_FILE)) {
			int size = StringUtil.getInstance().getIntegerArgument(args, "size", 200000000);
			createLargeFile(targetdir + DataUtil.PATH_SEPARATOR + "large.dat", size, '0');
		} else if (function.equals(FUNCTION_GET_WSDL_ARTIFACTS)) {
			extractArtefactsFromWSDLs(sourcedir, targetdir,
					StringUtil.getInstance().split(sourcefile, ",", "\"", "\""));
		} else if (function.equals(FUNCTION_DDL_ANALYSIS)) {
			String dialect = StringUtil.getInstance().getArgument(args, "dialect",
					ConfigurationUtil.getInstance().getSetting(SOA_TOOL_SQL_DIALECT));
			analyzeDDL(function, targetdir, sourcefile, dialect);
		} else if (function.equals(FUNCTION_JMC_CSV_FLATTEN)) {
			JMCFlatten(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_XSD_ANALYSIS)) {
			cleanseXsd(function, sourcedir, targetdir, sourcefile);
		} else if (function.equals(FUNCTION_EXTRACT_FILE_CONTENT)) {
			extractFileContent(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_ADF_ANALYSE)) {
			analyzeADFLogFiles(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_CONVERT)) {
			convertFiles(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_COMBINE_XML_FILES)) {
			combineXMLFiles(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_MAPPING_ANALYSIS)) {
			analyzeMapping(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_MAP_XML)) {
			mapXML(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_OEQ_ANALYSIS)) {
			oracleCompare(function, sourcedir, targetdir);
		} else if (function.equals(FUNCTION_SOAPUI_ANALYSIS)) {
			analyzeSOAPUI(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_HTML_TABLE_EXTRACT)) {
			extractHTMLTable(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_CSV_RULE_EXECUTE)) {
			executeCSVRules(function, sourcedir, targetdir, options);
		} else if (function.equals(FUNCTION_SOA_TOOL_SRV)) {
			executeSOAToolSrv(args);
		}
		LogUtil.getInstance().info("Execute subdirectory graph conversions");
		GraphConverter cvt = new GraphConverter();
		cvt.convertGV2GML_PNG_SVG(targetdir, autoconvert);
	}

	private void executeSOAToolSrv(String[] args) {
		String endpoint = StringUtil.getInstance().getArgument(args, "ep");
		String configuration = StringUtil.getInstance().getArgument(args, "conf");
		SOAToolCln cln;
		switch (StringUtil.getInstance().getArgument(args, "op")) {
		case "init":
			cln = new SOAToolCln(endpoint, configuration);
			cln.initNeo4J();
			break;
		case "query":
			cln = new SOAToolCln(endpoint, configuration);
			String queries = StringUtil.getInstance().getArgument(args, "query");
			for (String item : queries.split(","))
				cln.queryExecute(item);
			break;
		case "list":
			cln = new SOAToolCln(endpoint, configuration);
			for (String item : cln.queryList()) {
				LogUtil.getInstance().info(item);
			}

			break;
		}

	}

	public void analyzeSOAPMessages(String function, String sourcedir, String targetdir, Option[] options) {
		CSVData csv = new CSVData();
		csv.setHeader(new String[] { "NS", "TYPE", "RELATIE", "DATUM", "COUNT" });
		forEachDir(sourcedir, (fsg) -> {
			for (FileSystemNode fsn : fsg.filterNodes((f) -> f instanceof FileNode)) {
				FileNode fn = (FileNode) fsn;

				XMLUtil.getInstance().parse(fn.getId(), new XMLSAXHandler() {
					boolean foundTrigger;
					boolean done;
					boolean write;
					String ns;
					String type;
					String relatie;
					String datum;

					public void startElement(String uri, String localName, String qName, Attributes atts) {
						super.startElement(uri, localName, qName, atts);
						if (done) {
						} else if (!foundTrigger && pathTest("Body")) {
							foundTrigger = true;
						} else if (foundTrigger) {
							if (null == ns) {
								ns = uri;
								type = localName;
							}
						}
					}

					public void endElement(String uri, String localName, String qName) {
						if (pathTest("relatieCode")) {
							relatie = getData().toString();
						} else if (pathTest("aanvraagdatum")) {
							datum = getData().toString();
							done = true;
						} else if (pathTest("Body")) {
							write = true;
						}
						if (write) {
							write = false;
							csv.add(new String[] { ns, type, relatie, datum, "1" });
						}
						super.endElement(uri, localName, qName);
					}
				});
			}
			CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "SOAPmessageAnalysis.csv", csv, ';',
					CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);

			return true;
		});
	}

	public void printCharset(String function, String sourcedir, String targetdir, Option[] options) {
		int start = 0;
		int end = 1024;
		class _charset {
			String charset;
			int maxchars;
			boolean utf;

			public _charset(String charset, int maxchars, boolean utf) {
				this.charset = charset;
				this.maxchars = maxchars;
				this.utf = utf;
			}

			public String pad(String str, int length) {
				while (str.length() < length)
					str = " " + str;

				return str;
			}

			public String padCenter(String str, int length) {
				while (str.length() < length) {
					str = " " + str;
					if (str.length() < length)
						str = str + " ";
				}
				return str;
			}

			public String get(int idx) {
				try {
					if (idx >= maxchars)
						return " ";
					else if (utf)
						return new String(toUTF8(idx), this.charset);
					else
						return new String(new byte[] { (byte) idx }, this.charset);
				} catch (UnsupportedEncodingException e) {
					LogUtil.getInstance().error("encoding error", e);
					return "?";
				}

			}

			public byte[] toUTF8(int i) {
				byte[] result = new byte[] { 0x00 };

				if (i < 128)
					result = new byte[] { (byte) i };
				else if (i > 127) {
					int tmp1, tmp2;
					tmp1 = (((i >> 6) & 0x001f) | 0x00c0);
					tmp2 = ((i & 0x003f) | 0x0080);
					result = new byte[] { (byte) tmp1, (byte) tmp2 };
				}

				return result;
			}

			public int getMaxCharsetLength(_charset[] charsets) {
				int result = 0;
				for (_charset cs : charsets) {
					if (cs.charset.length() > result)
						result = cs.charset.length();
				}
				return result;
			}
		}

		_charset[] charsets = { new _charset("UTF-8", 1024, true), new _charset("ISO-8859-1", 256, false),
				new _charset("US-ASCII", 128, false), new _charset("ISO-8859-5", 256, false),
				new _charset("windows-1252", 256, false), };
		int maxColWidth = charsets[0].getMaxCharsetLength(charsets);
		String tmp = "" + end;
		int maxNumWidth = tmp.length();

		for (_charset cs : charsets)
			System.out.print("    [" + charsets[0].padCenter(cs.charset, maxColWidth) + "]");
		System.out.println();
		for (int i = start; i < end; ++i) {
			System.out.print(charsets[0].pad("" + i, maxNumWidth));
			for (_charset cs : charsets)
				System.out.print("[" + charsets[0].padCenter(cs.get(i), maxColWidth) + "]");
			System.out.println();
		}

	}

	public void log(String function, String sourcedir, String targetdir, String sink, String message,
			Option[] options) {
		RESTClient client = new RESTClient();

		String uri = getSetting(SOA_TOOL_SINK + "." + sink, function);

		uri = StringUtil.getInstance().replace(uri, var -> (var.equals("sink.message") ? message : ""));

		client.put(uri, new String[][] {}, null);
	}

	public void extractArtefactsFromWSDLs(String sourcedir, String targetdir, String[] wsdlfiles) {
		List<String> artefacts = new ArrayList<>();
		for (String wsdlFilename : wsdlfiles)
			artefacts.addAll(getArtefactsFromWSDL(wsdlFilename));

		CSVData csvData = new CSVData();
		csvData.setHeader(CSV_HEADER_WSDL_ARTEFACTS);
		for (String file : artefacts)
			csvData.add(new String[] { DataUtil.getInstance().stripProtocol(file),
					DataUtil.getInstance().getRelativename(sourcedir, file) });

		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "wsdl-artefacts.csv", csvData, ';');
		String cmd = "";
		for (String[] file : csvData.getLines()) {
			String targetfile = targetdir + DataUtil.PATH_SEPARATOR
					+ DataUtil.getInstance().getRelativename(sourcedir, file[0]);
			DataUtil.getInstance().makeDirectories(targetfile);
			targetfile = targetfile.replace('/', '\\');
			cmd += "copy \"" + file[0].replace('/', '\\') + "\" \"" + targetfile + "\"\n";
		}
		DataUtil.getInstance().writeToFile(targetdir + "\\copy-artefacts.cmd", cmd);
		// ScriptUtil.getInstance().executeBatchScript(cmd, new String[] {});
		return;
	}

	public void createFiles(String sourcedir, String targetdir) {
		int max = ConfigurationUtil.getInstance().getIntegerSetting(SOA_TOOL_GENERATION_MAX_FILE, 500);
		int size = ConfigurationUtil.getInstance().getIntegerSetting(SOA_TOOL_GENERATION_FILE_SIZE, 5000000);
		String name = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_GENERATION_FILE_NAME, "gen%number%.dat");

		for (int i = 0; i < max; ++i)
			this.createLargeFile(targetdir + DataUtil.PATH_SEPARATOR + name.replace("%number%", "" + i), size, 'a');
	}

	private void startJMSRequestResponse(String sourcedir, String targetdir, String server) {
		OSBTestJMS jmsServer = new OSBTestJMS(server);

		jmsServer.init();
		jmsServer.start();
	}

	private void startSlowServer(String sourcedir, String targetdir) {
		OSBTestService service = new OSBTestService();

		service.init();
		service.start();
	}

	/**
	 * Consider rethinking the purpose of this method. - check if a file contains
	 * XML with UTF-8 characters - check if a file is UTF-8 encoded and contains
	 * specific UTF-8 characters
	 * 
	 * @param file
	 * @param regex
	 * @return
	 */
	private boolean containsUTF(String file, String regex) {
		class Locals {
			boolean result;
		}
		;
		Locals _locals = new Locals();

		DataUtil.getInstance().readBlocksFromFile(file, 5000,
				(blocknr, blocksize, currentReadBytes, currentBlock, readbytes, blockswindow) -> {
					boolean result = true;

					for (int i = 0; i < currentReadBytes; ++i) {
						if (!((currentBlock[i] < 127 && currentBlock[i] >= ' ') || currentBlock[i] == '\t'
								|| currentBlock[i] == '\n' || currentBlock[i] == '\r')) {

							result = false;
							_locals.result = true;
							break;
						}
					}

					if (result) {
						byte[] totalblocks = DataUtil.getInstance().mergeArrays(readbytes, blockswindow);
						String str = new String(totalblocks);
						if (str.matches(regex)) {
							result = false;
							_locals.result = true;
						}
					}
					return result;
				});

		return _locals.result;

	}

	public void findfiles(String function, String sourcedir, String targetdir, Option... options) {
		Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(sourcedir);

		for (FileSystemNode fsn : fsg
				.filterNodes(f -> f instanceof FileNode && containsUTF(f.getId(), ".*&#[^;]*;.*"))) {
			LogUtil.getInstance().info("found file [" + fsn.getId() + "]");
		}
	}

	public Graph<Node, Edge<Node>> analyzeOracle(String function, String sourcedir, String targetdir,
			OracleOption option, Option... options) {
		Analyzer[] allAnalyzers = new Analyzer[] { new OSBAnalyzer(), new WSDLAnalyzer(), new XSDAnalyzer(),
				new SCAAnalyzer(), new BPMNAnalyzer(), new DescisionAnalyzer(), new JCAAnalyzer(), new WLSAnalyzer() };
		// Build analyzer list and info
		List<Analyzer> analyzers = new ArrayList<>();
		String cfgAnalyzers = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_ANALYZERS);
		String choosenAnalyzers = "";
		for (Analyzer analyzer : allAnalyzers) {
			choosenAnalyzers += (choosenAnalyzers.isEmpty() ? "" : ",") + analyzer.getClass().getSimpleName();
			choosenAnalyzers += "[";
			for (String cfg : cfgAnalyzers.split(",")) {
				// instanceof can NOT be used since this is a configurable test
				if (analyzer.getClass().getSimpleName().equals(cfg + SOA_TOOL_ANALYZER_CLASS_NAME_POSTFIX)) {
					analyzers.add(analyzer);
					choosenAnalyzers += "X";
					break;
				}
			}
			choosenAnalyzers += "]";
		}
		LogUtil.getInstance().info("choosen [" + choosenAnalyzers + "]");
		Map<String, Graph<?, ?>> result = new HashMap<>();
		// setup file problem CSV
		CSVData problems = new CSVData();
		problems.setHeader(new String[] { "ARTEFACT_TYPE", "FILE_NAME", "PROBLEM" });
		String[] dirs = sourcedir.split(",");
		// fork all the analyzers
		for (String dir : dirs) {
			LogUtil.getInstance().info("Start analyzing folder [" + dir + "]");
			Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(dir,
					getSetting(SOA_TOOL_FILE_ARTEFACT_FILTER, function));
			List<ManagedThread> threads = new ArrayList<>();
			// setup analyzer managed threads
			for (Analyzer analyzer : analyzers) {
				threads.add(ThreadUtil.getInstance().createManagedThread("", () -> {
					List<Graph<?, ?>> graphs = analyzer.analyze(dir, targetdir, fsg, problems);
					for (Graph<?, ?> gra : graphs) {
						synchronized (result) {
							Graph<?, ?> current = getGraph(result, gra.getName());
							if (null == current)
								result.put(gra.getName(), gra);
							else
								current.append(gra);
						}
					}
				}, ThreadUtil.Option.INHERIT_UTILS));
			}
			// fork the threads and wait for completion
			ThreadUtil.getInstance().fork(threads, ThreadUtil.Option.WAIT);
			LogUtil.getInstance().info("Done analyzing folder [" + dir + "]");
		}
		// write file analyzer problem CSV
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "problems.csv", problems, ';');
		// combine
		combine(result);
		connect(result);
		Graph<Node, Edge<Node>> graTotal = getGraph(result, GRAPH_NAME_TOTAL);

		if (!EnumUtil.getInstance().contains(options, Option.ORACLE_ANALYSE_ONLY)) {
			// Specials
			GraphMetrics metrics = new GraphMetrics();
			LogUtil.getInstance().info("Write analysis meta model (gv and xsd)");
			// Generate XSD for Graph Model
			MetaData metadata = GraphUtil.getInstance().createMetaData(graTotal);
			metadata.setRootTag(GRAPH_NAME_TOTAL);
			XSDGenerator xsdGen = new XSDGenerator();
			xsdGen.setTargetdir(targetdir);
			// Write the XSD to a file
			xsdGen.generate(metadata);
			// Write the meta model as a JSON file
			String json = JSONUtil.getInstance().writeJSON(JSONUtil.getInstance().java2JSON(metadata));
			DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + graTotal.getName() + ".json",
					json);
			// Write the meta model as a graph
			Graph<Node, Edge<Node>> graMeta = GraphUtil.getInstance().createMetaGraph(graTotal);
			metrics.writeGraphWithCycleInfo(targetdir, graMeta, getNodeMarkup(), GraphOption.CLEANUP_CYCLE_INFO);
			List<Cluster<Node>> clusters = getConventionClusters(result);
			Graph<ClusterNode<Node>, Edge<ClusterNode<Node>>> graConventions = GraphUtil.getInstance()
					.createGraphForClusters(graTotal, clusters, null);
			graConventions.setName("conventions");
			LogUtil.getInstance().info("Write analysis graphs");
			for (String name : result.keySet()) {
				Graph<Node, Edge<Node>> gra = getGraph(result, name);
				gra = filterGraph(function, gra);
				metrics.writeGraphWithCycleInfo(targetdir, gra, getNodeMarkup(), GraphOption.CLEANUP_CYCLE_INFO);
			}
			// Determine conventions
			metrics.writeGraphWithCycleInfo(targetdir, getGraph(result, GRAPH_NAME_CONVENTIONS), clusters,
					getNodeMarkup(), GraphOption.CLEANUP_CYCLE_INFO);
			CSVData conventionViolation = analyzeConventions(getGraph(result, GRAPH_NAME_CONVENTIONS), clusters);
			CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "cdm-convention-violation.csv",
					conventionViolation, ';');
			// Determine reuse
			CSVData reuse = analyzeReuse(getGraph(result, GRAPH_NAME_TOTAL));
			CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "reuse.csv", reuse, ';');

			if (EnumUtil.getInstance().contains(options, Option.CLIENT)) {
				LogUtil.getInstance().info("Write analysis to graph store (neo4j)");
				writeGraphToNeo4J(graTotal, NEO4J_ORACLE_STORE);
			} else {
				LogUtil.getInstance().info("Skipped writing analysis to graph store (neo4j)");
			}
		}
		return graTotal;
	}

	public Graph<Node, Edge<Node>> filterGraph(String function, Graph<Node, Edge<Node>> gra) {
		String filter = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_FILTER + "." + gra.getName());
		Graph<Node, Edge<Node>> result = gra;
		if (null != filter && !filter.isEmpty()) {
			String[] descriptions = filter.split(",");

			List<Node> startNodes = new ArrayList<>();
			for (String description : descriptions) {
				List<Node> tmp = gra.filterNodes(n -> description.equals(n.getDescription()));
				for (Node n : tmp)
					startNodes.add(n);
			}
			result = GraphUtil.getInstance().getGraphByStartNodes(gra, startNodes);
		}
		return result;
	}

	public void combine(Map<String, Graph<?, ?>> graphs) {
		LogUtil.getInstance().info("Combining analysis graphs");
		Graph<Node, Edge<Node>> graCdmConventions = new Graph<>();
		graCdmConventions.setName(GRAPH_NAME_CONVENTIONS);
		graCdmConventions.append(getGraph(graphs, GRAPH_NAME_XSD), GraphOption.CHECK_DUPLICATES);
		graCdmConventions.append(getGraph(graphs, GRAPH_NAME_WSDL), JOIN_IMPORT_NODE_XSD_NODE,
				GraphOption.CHECK_DUPLICATES);
		graphs.put(GRAPH_NAME_CONVENTIONS, graCdmConventions);
		// soa -> cdm-conventions + soa-wls + soa-osb + soa-sca + soa-jca
		Graph<Node, Edge<Node>> graTotal = new Graph<>();
		graTotal.setName("soa");
		graTotal.append(graCdmConventions, GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_WLS), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_OSB), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_SCA), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_BPMN), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_DESC), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_BPEL), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_JCA), GraphOption.CHECK_DUPLICATES);
		graTotal.append(getGraph(graphs, GRAPH_NAME_FLOW), GraphOption.CHECK_DUPLICATES);
		graphs.put(GRAPH_NAME_TOTAL, graTotal);
		// Create OSB links
		Graph<Node, Edge<Node>> graLinkedOsb = new Graph<>();
		graLinkedOsb.setName(GRAPH_NAME_SOA_LINKED);
		graLinkedOsb.append(getGraph(graphs, GRAPH_NAME_OSB), GraphOption.CHECK_DUPLICATES);
		graLinkedOsb.append(getGraph(graphs, GRAPH_NAME_FLOW), GraphOption.CHECK_DUPLICATES);
		graLinkedOsb.append(getGraph(graphs, GRAPH_NAME_WLS), GraphOption.CHECK_DUPLICATES);
		graphs.put(GRAPH_NAME_SOA_LINKED, graLinkedOsb);
	}

	@SuppressWarnings("unchecked")
	public void connect(Map<String, Graph<?, ?>> graphs) {
		// Connect OSB graphs nodes
		Graph<Node, Edge<Node>> osbLinked = (Graph<Node, Edge<Node>>) getGraph(graphs, GRAPH_NAME_SOA_LINKED);
		GraphLinker<Node, Edge<Node>> osbLinker = new GraphLinker<Node, Edge<Node>>(new GraphLink[] {
				new ConnectOSBBinding2WSDL(), new ConnectOSBBinding2WSDLBinding(), new ConnectWSDL2JCA(),
				new ConnectDispatchPolicy2Workmanager(), new ConnectXSDElement2SimpleTypeOrComplexTypeOrElement(),
				new ConnectXSDComplexTypeInheritance(), new ConnectOSBBusinessService2ProxyServiceByEndpoints(),
				new ConnectBPELInvoke2BusinessService(), new ConnectOSBAlert2AlertDestination() });
		osbLinker.link(osbLinked, new GraphIndex<Node, Edge<Node>>(osbLinked).build(), null);
		// Connect nodes in the total graphs
		Graph<Node, Edge<Node>> graTotal = (Graph<Node, Edge<Node>>) getGraph(graphs, GRAPH_NAME_TOTAL);
		LogUtil.getInstance().info("Linking analysis graphs, before linking result has : nodes ["
				+ graTotal.getNodes().size() + "] edges [" + graTotal.getEdges().size() + "]");
		GraphLinker<Node, Edge<Node>> totalLinker1 = new GraphLinker<Node, Edge<Node>>(new GraphLink[] {
				new ConnectOSBBinding2WSDL(), new ConnectOSBBinding2WSDLBinding(), new ConnectWSDL2JCA(),
				new ConnectDispatchPolicy2Workmanager(), new ConnectComponent2WSDL(),
				new ConnectComponent2Implementation(), new ConnectTask2Reference(),
				new ConnectXSDElement2SimpleTypeOrComplexTypeOrElement(), new ConnectXSDComplexTypeInheritance(),
				new ConnectOSBBusinessService2ProxyServiceByEndpoints(), new ConnectBPELInvoke2BusinessService(),
				new ConnectSCAInterface2WSDL(), new ConnectOSBAlert2AlertDestination() });
		GraphLinker<Node, Edge<Node>> totalLinker2 = new GraphLinker<Node, Edge<Node>>(
				new GraphLink[] { new ConnectSCAServices2SCAReferences(), new ConnectSCAInterface2ProxyService(),
						new ConnectBusinessService2SCAInterface() });
		GraphLinker<Node, Edge<Node>> scaLinker = new GraphLinker<Node, Edge<Node>>(
				new GraphLink[] { new ConnectSCAServices2SCAReferences() });
		Graph<Node, Edge<Node>> gra = getGraph(graphs, GRAPH_NAME_SCA);
		scaLinker.link(gra, new GraphIndex<Node, Edge<Node>>(gra).build(), null);
		totalLinker1.link(graTotal, new GraphIndex<Node, Edge<Node>>(graTotal).build(), null);
		totalLinker2.link(graTotal, new GraphIndex<Node, Edge<Node>>(graTotal).build(), null);
		LogUtil.getInstance().info("After linking analysis graphs, after linking result has : nodes ["
				+ graTotal.getNodes().size() + "] edges [" + graTotal.getEdges().size() + "]");

	}

	@SuppressWarnings("unchecked")
	public Graph<Node, Edge<Node>> getGraph(Map<String, Graph<?, ?>> graphs, String name) {
		Graph<?, ?> result = graphs.get(name);

		if (null == result) {
			result = new Graph<>();
			result.setName(name);
			graphs.put(name, result);
			return null;
		}

		return (Graph<Node, Edge<Node>>) result;
	}

	public List<Cluster<Node>> getConventionClusters(Map<String, Graph<?, ?>> graphs) {
		Graph<Node, Edge<Node>> graCdmConventions = getGraph(graphs, GRAPH_NAME_CONVENTIONS);
		// Create an overview based on CDM hierarchy
		List<Cluster<Node>> clusters = GraphUtil.getInstance().createClusters(graCdmConventions, n -> {
			String result = "IRRELEVANT";
			List<String[]> validExtensions = StringUtil.getInstance().split(
					ConfigurationUtil.getInstance().getSetting(SOA_TOOL_CDM_CONVENTIONS_CLUSTERS),
					new String[] { ",", "=" }, "\"", "\"");
			for (String[] ext : validExtensions) {
				try {
					if ((n instanceof XSDNode || n.getClass() == WSDLNode.class)
							&& n.getDescription().matches(ext[0])) {
						result = ext[1];
						break;
					}
				} catch (PatternSyntaxException e) {
					LogUtil.getInstance()
							.info("pattern problem with [" + n.getDescription() + ".matches(" + ext[0] + ")", e);
				}
			}
			return result;
		});

		// Cleanup conventions
		Optional<Cluster<Node>> optNode = clusters.stream().filter(c -> c.getName().equals("IRRELEVANT")).findFirst();
		if (optNode.isPresent()) {
			for (Node n : optNode.get().getNodes())
				graCdmConventions.removeNode(n);
			clusters.remove(optNode.get());
		}

		return clusters;
	}

	public CSVData analyzeReuse(Graph<Node, Edge<Node>> graTotal) {
		CSVData reuse = new CSVData();
		LogUtil.getInstance().info("Write OSB reuse");
		Map<Node, Integer> reuseMap = GraphUtil.getInstance().getNodeReferenceCount(graTotal, null);

		reuse.setHeader(new String[] { "TYPE", "NAME", "DESCRIPTION", "USED_COUNT" });
		for (Entry<Node, Integer> e : reuseMap.entrySet()) {
			reuse.add(new String[] { e.getKey().getClass().getSimpleName(), e.getKey().getId(),
					e.getKey().getDescription(), "" + e.getValue() });
		}
		return reuse;
	}

	public CSVData analyzeConventions(Graph<Node, Edge<Node>> graCdmConventions, List<Cluster<Node>> clusters) {
		LogUtil.getInstance().info("Analyzing CDM conventions");
		// Detect invalid transitions
		List<String[]> validTransitions = StringUtil.getInstance().split(
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_CDM_CONVENTIONS_TRANSITIONS),
				new String[] { ",", "->" }, "\"", "\"");
		List<Edge<Node>> invalidTransitionEdges = new ArrayList<>();
		for (Edge<Node> e : graCdmConventions.getEdges()) {
			if (e.getSource() instanceof XSDNode || e.getSource() instanceof WSDLNode
					|| e.getTarget() instanceof XSDNode || e.getTarget() instanceof WSDLNode) {
				// find source cluster
				Cluster<Node> sourceCluster = null;
				for (Cluster<Node> c : clusters)
					if (c.contains(e.getSource(), GraphOption.CLUSTER_RECURSIVE)) {
						sourceCluster = c;
						break;
					}
				// find target cluster
				Cluster<Node> targetCluster = null;
				for (Cluster<Node> c : clusters)
					if (c.contains(e.getTarget(), GraphOption.CLUSTER_RECURSIVE)) {
						targetCluster = c;
						break;
					}
				if (null != sourceCluster && null != targetCluster) {
					boolean found = false;
					for (String[] transition : validTransitions) {
						if (sourceCluster.getName().equals(transition[0])
								&& targetCluster.getName().equals(transition[1])) {
							found = true;
							break;
						}
					}
					if (!found)
						invalidTransitionEdges.add(e);
				}
			}
		}
		// Write invalid transitions
		LogUtil.getInstance().info("Write CDM violations");
		CSVData conventionViolation = new CSVData();
		conventionViolation
				.setHeader(new String[] { "FROM", "TO", "FROM_TYPE", "TO_TYPE", "FROM_FILE", "TO_FILE", "TYPE" });
		for (Edge<Node> e : invalidTransitionEdges) {
			conventionViolation.add(new String[] { e.getSource().getDescription(), e.getTarget().getDescription(),
					e.getSource().getClass().getSimpleName(), e.getTarget().getClass().getSimpleName(),
					e.getSource().getId(), e.getTarget().getId(), "CDM_HIERARCHY" });
		}

		return conventionViolation;
	}

	public void analyzeSOAPUI(String function, String sourcedir, String targetdir, Option... options) {
		forEachDir(sourcedir, "(?i).*\\.xml", (fsg) -> {
			for (FileSystemNode fn : fsg.filterNodes(fn -> fn instanceof FileNode)) {
				SOAPUIConfigurationReader soapuiReader = new SOAPUIConfigurationReader();

				Graph<SOAPUINode, Edge<SOAPUINode>> soapuiConfiguration = soapuiReader.read(fn.getId());

				for (SOAPUINode rn : soapuiConfiguration.filterNodes(n -> n instanceof SOAPUICallNode)) {
					SOAPUICallNode cn = (SOAPUICallNode) rn;
					DataUtil.getInstance().writeToFile(
							targetdir + DataUtil.PATH_SEPARATOR + makeValidFilename(cn.getName()) + ".request.xml",
							cn.getRequest());
				}
			}
			return true;
		});
	}

	public void executeCSVRules(String function, String sourcedir, String targetdir, Option... options) {
		char commaToken = ';';
		CSVData csvRuleConfig = CSVUtil.getInstance().readFromFile(
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_CSV_RULE_EXECUTE_RULE_CONFIG), commaToken,
				CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		CSVData csvRuleConfigInferred = CSVUtil.getInstance().readFromFile(
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_CSV_RULE_EXECUTE_RULE_CONFIG_INFERRED), commaToken,
				CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		csvRuleConfig.append(csvRuleConfigInferred);
		CSVRuleExec ruleExec = new CSVRuleExec(csvRuleConfig);
		class Locals {
			String[] header;
		}

		Locals _locals = new Locals();
		forEachDir(sourcedir, "(?i).*\\.csv?", (fsg) -> {
			for (FileSystemNode fn : fsg.filterNodes(fn -> fn instanceof FileNode)) {
				String outFilename = targetdir + DataUtil.PATH_SEPARATOR
						+ DataUtil.getInstance().getFilename(fn.getId());
				LogUtil.getInstance().error("start converting CSV file [" + fn.getId() + "]");
				try (OutputStream os = new FileOutputStream(outFilename)) {
					CSVUtil.getInstance().readFromFile(fn.getId(), ';', (linenr, row) -> {
						List<String> newRow = new ArrayList<>();
						boolean skip = false;
						// clone row
						for (String val : row) {
							newRow.add(val);
						}
						if (linenr == 0) {
							_locals.header = row;
							newRow.add("KEY");
							newRow.add("VALUE");
						} else {
							while (newRow.size() < _locals.header.length)
								newRow.add("");
							String[] kv = ruleExec.getKeyValue(_locals.header, row);
							if (null == kv[0] || kv[0].equals(":") || kv[0].isEmpty())
								skip = true;
							newRow.add(kv[0]);
							newRow.add(kv[1]);
						}
						if (!skip) {
							try {
								String newLine = CSVUtil.getInstance().makeLine(newRow.toArray(new String[] {}),
										commaToken) + "\n";
								os.write(newLine.getBytes());
							} catch (IOException e) {
								LogUtil.getInstance()
										.error("problem making row [" + linenr + "]for file [" + fn.getId() + "]", e);
							}
						}
					});
				} catch (Exception ex) {
					LogUtil.getInstance().error("problem converting CSV file [" + fn.getId() + "]", ex);
				}
				LogUtil.getInstance().error("stop converting CSV file [" + fn.getId() + "]");
			}
			return true;
		});
	}

	/**
	 * <pre>
	 * Parses a directory and subdirectory for html files. Tables in the html files are added to a CSV.
	 * 
	 * The first three cells in a row, contain the following column name and data:
	 * - 'soatool.html.table.extract.col1.name': The filename (string substitution from the filename 'soatool.html.table.extract.replace.col1')
	 * - 'soatool.html.table.extract.col2.name': The enviroment  (string substitution from the filename 'soatool.html.table.extract.replace.col2')
	 * - 'soatool.html.table.extract.col3.name': The server  (string substitution from the filename 'soatool.html.table.extract.replace.col3')
	 * </pre>
	 * 
	 * @param function
	 * @param srcdir
	 * @param targetdir
	 * @param options
	 */
	public void extractHTMLTable(String function, String sourcedir, String targetdir, Option... options) {
		CSVData problems = new CSVData();
		CSVData csv = new CSVData();
		CSVData csvDir = new CSVData();
		CSVData meta = new CSVData();
		problems.setHeader(new String[] { "FILE", "STATUS" });
		meta.setHeader(new String[] { "TABLE", "LEVEL", "NAAM" });
		// determine the column names
		csv.setHeader(new String[] {
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL1_NAME, "FILE"),
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL2_NAME, "OMGEVING"),
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL3_REPLACE, "SERVER"), "TABLE",
				"COL1", "COL2", "COL3", "COL4", "COL5", "COL6", "COL7", "COL8", "COL9", });
		csvDir.setHeader(csv.getHeader());
		CSVData csvExtractConfig = CSVUtil.getInstance().readFromFile(
				ConfigurationUtil.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_CONFIG), ';',
				CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		forEachDir(sourcedir, "(?i).*\\.html?", (fsg) -> {
			csvDir.clearData();
			String server = StringUtil.getInstance().replace(fsg.getName(),
					ConfigurationUtil.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL3_REPLACE));
			for (FileSystemNode fn : fsg.filterNodes(fn -> fn instanceof FileNode)) {
				String problem = "OK";
				LogUtil.getInstance().error("start parsing HTM(L) file [" + fn.getId() + "]");
				try {
					if (XMLUtil.getInstance().parse(DataUtil.getInstance().convertToFileURL(fn.getId()),
							new XMLSAXHandler() {
								private String currentTable;
								private List<List<String>> rows;
								private List<String> row;
								private List<String> header = new ArrayList<>();
								private int colIdx = 0;
								boolean isHeader;
								String inferredTable;

								@Override
								public void startElement(String uri, String localName, String qName,
										org.xml.sax.Attributes atts) {
									optClearDataOnElementStart = false;
									super.startElement(uri, localName, qName, atts);
									if (localName.equals("table")) {
										currentTable = atts.getValue("summary");
										colIdx = 1;
										isHeader = false;
										rows = new ArrayList<>();
									} else if (localName.equals("tr")) {
										row = new ArrayList<>();
										row.add(StringUtil.getInstance().replace(fn.getId(), ConfigurationUtil
												.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL1_REPLACE)));
										row.add(StringUtil.getInstance().replace(fn.getId(), ConfigurationUtil
												.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL2_REPLACE)));
										row.add(StringUtil.getInstance().replace(fn.getId(), ConfigurationUtil
												.getInstance().getSetting(SOA_TOOL_HTML_TABLE_EXTRACT_COL3_REPLACE)));
										row.add(currentTable);
									} else if (localName.equals("td") || localName.equals("th")) {
										clearData();
										if (localName.equals("th"))
											isHeader = true;
									}
								}

								@Override
								public void endElement(String uri, String localName, String qName) {
									if (localName.equals("th")) {
										meta.add(new String[] { currentTable, "COL" + colIdx++, getData() });
										header.add(getData());
									} else if (localName.equals("td")) {
										row.add(getData());
									} else if (localName.equals("tr")) {
										if (isHeader) {
											inferredTable = getInferredTableName(header, csvExtractConfig);
											isHeader = false;
											header.clear();
										} else {
											// insert the inferred table name at
											// column 3 (TABLE column), the rest
											// will shift to
											// the right
											if (null != inferredTable && !inferredTable.isEmpty())
												row.add(3, inferredTable);
											rows.add(row);
										}

									} else if (localName.equals("table")) {
										prependAttributes(currentTable, csv.getColumnHash(), rows, csvExtractConfig);
										for (List<String> line : rows) {
											csvDir.add(line);
											csv.add(line);
										}
									}
									super.endElement(uri, localName, qName);
								}
							})) {

					} else {
						problem = "ERROR: "
								+ LogUtil.getInstance().formatExceptionInline(LogUtil.getInstance().getLastError());
					}
				} catch (Exception ex) {
					LogUtil.getInstance().info("problem parsing file [" + fn.getId() + "]", ex);
					problem = LogUtil.getInstance().formatExceptionInline(ex);
				}
				LogUtil.getInstance().error("done parsing HTM(L) file [" + fn.getId() + "]");
				problems.add(new String[] { fn.getId(), problem });
			}
			CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + server + ".csv", csvDir, ';');
			return true;
		});
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "parsingproblems.csv", problems, ';');
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "combined.csv", csv, ';');
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "meta.csv", meta, ';');
	}

	/**
	 * <pre>
	 * Prepends data to cells based on:
	 *  - Data - 
	 * ----------------------------------------------------------
	 * |   TABLE   |   COL1    |   COL2   |   COL3   |   COL4   |
	 * ----------------------------------------------------------
	 * |   TAB_A   |  CELL11   |  CELL12  |  CELL13  |   CELL14 |
	 * |   TAB_A   |  CELL21   |  CELL22  |  CELL23  |   CELL24 |
	 * |   TAB_B   |  CELL31   |  CELL32  |  CELL33  |   CELL34 |
	 * ----------------------------------------------------------
	 * - Configuration - (LET OP DE COLOM NAMEN MOETEN MATCHEN!) 
	 *  -------------------------------------------------------------------------------
	 * |   TABLE   |          TYPE        |    COL1  |   COL2   |   COL3   |   COL4   |
	 *  -------------------------------------------------------------------------------
	 * |   TAB_A   |   PREPEND_ATTRIBUTE  |   CELL21 |          |    $     |          |
	 *  -------------------------------------------------------------------------------
	 * 
	 * 1) Zoek in de data naar de rij waarbij de colom COL1 de waarde CELL21 bevat. Dit vindt plaats op basis van een match 
	 *    tussen de colomnaam van de data en de configuratie. In de voorbeeld configuratie staat de CELL21 waarde in (colom) COL1, 
	 *    dus in de data wordt dan ook in (colom) COL1 gezocht naar (de waarde  van) CELL21. 
	 * 2) Bepaald de waarde die voor het prependen gebruikt moet worden op basis van $. Ook hier wordt de waarde 
	 *    bepaald op een match tussen de colomnaam van de data en de configuratie. In de voorbeeldconfiguratie staat $ in COL3, dus als 
	 *    prepend waarde wordt dan ook de specifieke data in de gevonden rij (in de configuratie) bij (1) aan alle COL1 
	 *    waarden geprepend aan de data in (colom) COL1 (dat is dus (de waarde van) CELL23).
	 * 3) Vervang in de data alle waarden in de colom (COL1) door de $ waarde (van CELL23 in de configuratie) er vooraan 
	 *    aan toe te voegen.
	 * 
	 * Het resultaat wordt dan ook
	 *  - Data Nieuw - 
	 * ----------------------------------------------------------------
	 * |   TABLE   |       COL1      |   COL2   |   COL3   |   COL4   |
	 * ----------------------------------------------------------------
	 * |   TAB_A   |  CELL23CELL11   |  CELL12  |  CELL13  |   CELL14 |
	 * |   TAB_A   |  CELL23CELL21   |  CELL22  |  CELL23  |   CELL24 |
	 * |   TAB_B   |  CELL23CELL31   |  CELL32  |  CELL33  |   CELL34 |
	 * ----------------------------------------------------------------
	 * 
	 * Concreet voorbeeld:
	 * Dit is nogal redelijk complex methode. Stel je hebt een tabel van <naam,waarde> paren. Dan is één van
	 * de <naam,waarde> paren <Naam,Joep>. Nu wil je 'Joep' aan alle namen van de paren, prependen. 
	 * Dit kan handig zijn wanneer je 'iets' parsed en meerdere instanties van dezelfde <naam,waarde> tabel in 
	 * één en dezelfde CSV wil toevoegen, maar toch onderscheidende <naam,waarde> paren wil hebben.
	 * 
	 * B.v.data in tabel [Attributen]: 
	 * [NAAM,WAARDE]	 * 
	 * [Plaats,Den Haag]
	 * [Naam,Joep]
	 * [Tijd,20:14]
	 * [Type,EVENT]
	 * 
	 * Wordt dan:
	 * [NAAM,WAARDE]
	 * [Joep_Plaats,Den Haag]
	 * [Joep_Naam,Joep]
	 * [Joep_Tijd,20:14]
	 * [Joep_Type,EVENT]
	 *
	 * De benodigde configuratie is dan:
	 * [TABLE,TYPE,NAAM,WAARDE]
	 * [Attributen,PREPEND_ATTRIBUTE,Naam,$]
	 * 
	 * Let op: De colom namen in de configuratie worden gebruikt om de colom index in de data
	 * te bepalen. Dus onderstaande configuratie is ook juist (de colommen zijn hier 
	 * omgedraaid)
	 * [TABLE,TYPE,WAARDE,NAAM]
	 * [Attributen,PREPEND_ATTRIBUTE,$,Naam]
	 * 
	 * </pre>
	 */
	private void prependAttributes(String table, CSVColumnHash colHash, List<List<String>> rows, CSVData cfgExtract) {
		for (String[] cfg : cfgExtract.getLines()) {
			if (cfg[1].equals("PREPEND_ATTRIBUTE")) {
				if (cfg[0].equals(table)) {
					String attributeLookupValue = null;
					int attributeLookupValueIdx = -1;
					int attributePrependValueIdx = -1;
					String attributePrependValue = null;
					// vind attribute lookup waarde en zijn index
					// de index is op basis van een colomnaam match tussen
					// configuratie en de data
					for (int i = 2; i < cfg.length; ++i) {
						if (null != cfg[i] && !cfg[i].isEmpty()) {
							if (cfg[i].equals("$")) {
								attributePrependValueIdx = colHash.get(cfgExtract.getHeader()[i]);
							} else {
								attributeLookupValue = cfg[i];
								attributeLookupValueIdx = colHash.get(cfgExtract.getHeader()[i]);
							}
						}
					}
					// now lookup the value
					if (attributePrependValueIdx >= 0 && null != attributeLookupValue) {
						for (List<String> row : rows) {
							if (attributeLookupValue.equals(row.get(attributeLookupValueIdx))) {
								attributePrependValue = row.get(attributePrependValueIdx);
								break;
							}
						}
						// now prepend the attribute
						if (null != attributePrependValue && !attributePrependValue.isEmpty()) {
							for (List<String> row : rows) {
								row.set(attributeLookupValueIdx,
										attributePrependValue + "_" + row.get(attributeLookupValueIdx));
							}
						}
					}
				}
			}
		}
	}

	private String getInferredTableName(List<String> header, CSVData cfgExtract) {
		return getInferredTableName(header.toArray(new String[] {}), cfgExtract);
	}

	/**
	 * Bug if headers only contain a part of the inferred table key.
	 * 
	 * This method infers a table name based on the header column names. If the
	 * columns match the inferred table name is the first value in a row (first
	 * column).
	 * 
	 * @param header
	 * @param cfgExtract
	 * @return
	 */
	private String getInferredTableName(String[] header, CSVData cfgExtract) {
		String result = null;
		for (String[] cfg : cfgExtract.getLines()) {
			if (cfg[1].equals("INFER_TABLE")) {
				if (cfg.length > header.length + 1) {
					boolean found = true;
					for (int i = 0; i < header.length; ++i) {
						if (null == cfg[i + 2] || cfg[i + 2].isEmpty())
							break;
						if (!cfg[i + 2].equals(header[i].trim())) {
							found = false;
							break;
						}
					}
					if (found) {
						result = cfg[0];
						break;
					}
				}
			}
		}
		return result;
	}

	// @Test
	// public void test() throws ParseException {
	// CSVData in =
	// CSVUtil.getInstance().readFromFile("d:/customers/venj/performance/run/20161128/analyse.csv",
	// ';',
	// CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
	//
	// CSVData out = new CSVData();
	// out.setHeader(in.getHeader());
	// SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	// for (String[] line : in.getLines()) {
	// if (!line[4].isEmpty())
	// line[2] = fmt.format(new Date(fmt.parse(line[4]).getTime() + 1139000));
	// if (!line[5].isEmpty())
	// line[3] = fmt.format(new Date(fmt.parse(line[5]).getTime() + 1141000));
	// out.add(line);
	// }
	//
	// CSVUtil.getInstance().writeToFile("d:/customers/venj/performance/run/20161128/analyse.cor.csv",
	// out, ';',
	// CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
	// }
	//
	// public void testDeprecated() {
	// if (true) {
	// String[] headers = new String[] { "FILE", "OMGEVING", "SERVER", "TABLE",
	// "COL1", "COL2", "COL3", "COL4",
	// "COL5", "COL6", "COL7", "COL8", "COL9", };
	//
	// String[] configHeader = new String[] { "TABLE", "NAAM", "LEVEL",
	// "AANTAL_KOLOMMEN", "RULE" };
	// String[][] configData = new String[][] { { "TABLE_A", "VELD1", "COL1",
	// "6", "KEY" },
	// { "TABLE_A", "VELD2", "COL2", "6", "KEY" }, { "TABLE_A", "VELD3", "COL3",
	// "6", "" },
	// { "TABLE_A", "VELD4", "COL4", "6", "VALUE" }, { "TABLE_A", "VELD5",
	// "COL5", "6", "VALUE" },
	// { "TABLE_A", "VELD6", "COL6", "6", "" }, { "TABLE_B", "VELD1", "COL1",
	// "6", "" },
	// { "TABLE_B", "VELD2", "COL2", "6", "VALUE" }, { "TABLE_B", "VELD3",
	// "COL3", "6", "KEY" }, };
	// CSVData config = new CSVData();
	// config.setHeader(configHeader);
	// for (String[] line : configData)
	// config.add(line);
	// CSVRuleExec ruleExec = new CSVRuleExec(config);
	// String[] kv = ruleExec.getKeyValue(headers,
	// new String[] { "d:/file", "ACC", "MGS001", "TABLE_A", "key1", "key2",
	// "ignore", "col1", "col2" });
	// Assert.assertEquals(kv[0], "TABLE_AVELD1VELD2:key1key2");
	// Assert.assertEquals(kv[1], "col1col2");
	// kv = ruleExec.getKeyValue(headers, new String[] { "d:/file", "ACC",
	// "MGS001", "TABLE_B", "ignore", "col",
	// "key", "ignore", "ignore", "col2" });
	// Assert.assertEquals(kv[0], "TABLE_BVELD3:key");
	// Assert.assertEquals(kv[1], "col");
	// String[][] extractConfigData = new String[][] {
	// { "TABLE_A", "INFER_TABLE", "VELD1", "VELD2", "VELD3", "VELD4", "VELD5",
	// "VELD6" },
	// { "TABLE_B", "INFER_TABLE", "VELD0", "VELD2", "VELD3", "VELD4", "VELD5"
	// },
	// { "TABLE_A", "PREPEND_ATTRIBUTE", "VELD4", "", "$", "" } };
	// CSVData extractConfig = new CSVData();
	// extractConfig.setHeader(new String[] { "TABLE", "TYPE", "NAAM", "LEVEL",
	// "AANTAL_KOLOMMEN", "RULE", "COL7",
	// "COL8", "COL9" });
	// for (String[] line : extractConfigData) {
	// extractConfig.add(line);
	// }
	// Assert.assertEquals("TABLE_A", getInferredTableName(
	// new String[] { "VELD1", "VELD2", "VELD3", "VELD4", "VELD5", "VELD6" },
	// extractConfig));
	// Assert.assertEquals("TABLE_B",
	// getInferredTableName(new String[] { "VELD0", "VELD2", "VELD3", "VELD4",
	// "VELD5" }, extractConfig));
	//
	// List<List<String>> prependData = new ArrayList<>();
	// for (String[] line : configData) {
	// List<String> row = new ArrayList<>();
	// for (String cell : line)
	// row.add(cell);
	// prependData.add(row);
	// }
	// prependAttributes("TABLE_A", config.getColumnHash(), prependData,
	// extractConfig);
	// Assert.assertEquals(prependData.get(0).get(1), "6_VELD1");
	// Assert.assertEquals(prependData.get(3).get(1), "6_VELD4");
	// }
	// }

	private String makeValidFilename(String name) {
		return StringUtil.getInstance().camelCaseFormat(name,
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-", " ");
	}

	private void createLargeFile(String filename, int size, char b) {
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(filename))) {

			for (int i = 0; i < size; ++i)
				os.write(b);
			LogUtil.getInstance().error("created large file [" + filename + "] of size [" + size + "]");
		} catch (IOException e) {
			LogUtil.getInstance().error("problem creating large file [" + filename + "] of size [" + size + "]", e);
		}
	}

	private <N extends Node, E extends Edge<N>> void writeGraphToNeo4J(Graph<N, E> gra, String store) {
		try (Connection conn = new Neo4JEmbeddedConnection(store)) {
			conn.connect();
			Neo4JPersist<N, E> neo4j = new Neo4JPersist<N, E>();
			neo4j.save(conn, gra, true);
		} catch (IOException e) {
			LogUtil.getInstance().error("problem writer graph to neo4j", e);
		}
	}

	protected List<String> getArtefactsFromWSDL(String wsdlFilename) {
		List<String> result = new ArrayList<>();
		String folder = DataUtil.getInstance().getFoldername(DataUtil.getInstance().simplifyFolder(wsdlFilename));

		WSDLReader wsdlReader = new WSDLReader(folder);
		result.add(wsdlFilename);
		Graph<WSDLNode, Edge<WSDLNode>> graWsdl = wsdlReader.read(wsdlFilename);
		List<WSDLNode> imports = graWsdl.filterNodes(n -> n instanceof ImportNode);
		Map<String, String> artefacts = new HashMap<>();
		// get the XSD imports in the WSDL
		for (WSDLNode wn : imports) {
			ImportNode imp = (ImportNode) wn;

			String xsdFile = folder + DataUtil.PATH_SEPARATOR + imp.getSchemaLocation();
			// get nested XSD imports
			getXSDArtefacts(folder, xsdFile, artefacts);
		}
		result.addAll(artefacts.values());
		return result;
	}

	protected Map<String, String> getXSDArtefacts(String baseUrl, String filename, Map<String, String> artefacts) {
		if (null != filename && !filename.isEmpty()) {
			XSDReader xsdReader = new XSDReader(baseUrl);
			Graph<XSDNode, Edge<XSDNode>> graXsd = null;
			String xsdFile = DataUtil.getInstance().protocolReplace(filename);

			if (!DataUtil.getInstance().isAbsolutePath(xsdFile))
				xsdFile = baseUrl + DataUtil.PATH_SEPARATOR + xsdFile;
			xsdFile = DataUtil.getInstance().simplifyFolder(xsdFile);
			String key = DataUtil.getInstance().stripProtocol(xsdFile.toLowerCase());
			if (null == artefacts.get(key)) {
				artefacts.put(key, xsdFile);
				try {
					graXsd = xsdReader.read(xsdFile);
					baseUrl = DataUtil.getInstance().getFoldername(xsdFile);
					for (XSDNode xsd : graXsd.filterNodes(n -> n instanceof XSDNode)) {
						try {
							getXSDArtefacts(baseUrl, xsd.getSchemaLocation(), artefacts);
						} catch (Exception el) {
							LogUtil.getInstance().error("unable to analyse [" + baseUrl + "," + xsd.getSchemaLocation()
									+ "] in XSD [" + filename + "]", el);
						}
					}
				} catch (Exception e) {
					LogUtil.getInstance().error("unable to analyse [" + baseUrl + "," + filename + "]", e);
				}
			}
		}

		return artefacts;
	}

	public void analyzeDDL(String function, String targetdir, String files, String dialect) {
		for (String file : files.split(",")) {
			LogUtil.getInstance().info("Start analyzing DDL [" + file + "]");
			try {
				SQLParserResult result = parseDDL(file, dialect);
				if (null != result.getContext()) {
					CSVData csvData = result.getEntityAttributeCSV();
					Graph<Node, Edge<Node>> graMeta = result.getMetaGraph();
					String name = DataUtil.getInstance().getFilenameWithoutExtension(file);
					graMeta.setName(name);
					LogUtil.getInstance().info("Writing DLL CSV information");
					CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + name + ".csv", csvData,
							';');
					ParseTreeWriter wri = new ParseTreeWriter();
					DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + name + ".pti",
							wri.write(result.getContext()).toString());
					DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + name + ".ddl",
							result.getCreateScript());
					LogUtil.getInstance().info("Writing DLL graph information");
					GraphMetrics metrics = new GraphMetrics();
					metrics.writeGraphWithCycleInfo(targetdir, graMeta, getNodeMarkup());
					DataUtil.getInstance().writeToFile(
							targetdir + DataUtil.PATH_SEPARATOR
									+ DataUtil.getInstance().getFilenameWithoutExtension(file) + ".xml",
							wri.write(result.getContext()).toString());
					XSDGenerator gen = new XSDGenerator();
					gen.setTargetdir(targetdir);
					MetaData metadata = result.getMetaData();
					metadata.setRootTag(DataUtil.getInstance().getFilenameWithoutExtension(file));
					gen.generate(metadata);
				}
			} catch (Exception ex) {
				LogUtil.getInstance().error("problem analysing file [" + file + "]", ex);
			}
			LogUtil.getInstance().info("Done analyzing DDL [" + file + "]");
		}
	}

	private SQLParserResult parseDDL(String file, String dialect) {
		SQLParserResult result = null;
		Dialect sqlDialect = null;
		int idx = file.indexOf("@");
		if (idx >= 0) {
			sqlDialect = (Dialect) EnumUtil.getInstance().getByName(Dialect.class, file.substring(0, idx));
			if (null == sqlDialect)
				LogUtil.getInstance().warning("invalid dialect for file [" + file + "]");
			else if (file.length() > idx)
				file = file.substring(idx + 1);
		}
		if (null == sqlDialect) {
			sqlDialect = (Dialect) EnumUtil.getInstance().getByName(Dialect.class, dialect);
			if (null == sqlDialect)
				LogUtil.getInstance().warning("invalid dialect [" + dialect + "]");
		}

		if (null != sqlDialect) {
			LogUtil.getInstance().info("Parsing DDL using dialect [" + sqlDialect.name() + "]");
			SQLParser parser = SQLUtil.getInstance().getSQLParser(sqlDialect);

			result = parser.parseDDL(file);
		}

		return result;
	}

	public void cleanseXsd(String function, String sourcedir, String targetdir, String files) {
		String[] _files;
		if (null != sourcedir && !sourcedir.isEmpty()) {
			Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(sourcedir, "(?i).*\\.xsd");
			_files = fsg.filterNodes(fn -> fn instanceof FileNode).stream().map(fn -> fn.getId())
					.collect(Collectors.toList()).toArray(new String[0]);
		} else
			_files = files.split(",");
		Graph<XSDNode, Edge<XSDNode>> graTotal = new Graph<>();
		graTotal.setName(GRAPH_NAME_XSD);
		GraphMetrics metrics = new GraphMetrics();
		@SuppressWarnings({ "unchecked" })
		GraphLinker<XSDNode, Edge<XSDNode>> linker = new GraphLinker<XSDNode, Edge<XSDNode>>(
				new GraphLink[] { new ConnectXSDElement2SimpleTypeOrComplexTypeOrElement() });
		for (String file : _files) {
			LogUtil.getInstance().info("Start cleansing XSD [" + file + "]");
			SimpleXSDReader reader = new SimpleXSDReader();
			MetaData meta = reader.read(file);
			LogUtil.getInstance().info("Writing cleansed XSD");
			meta.setRootTag(DataUtil.getInstance().getFilenameWithoutExtension(file));
			XSDGenerator gen = new XSDGenerator();
			gen.setTargetdir(targetdir);
			gen.generate(meta);
			LogUtil.getInstance().info("Writing XSD meta graph");
			Graph<Node, Edge<Node>> gra = GraphUtil.getInstance().createMetaGraph(meta);

			metrics.writeGraphWithCycleInfo(targetdir, gra, getNodeMarkup());
			LogUtil.getInstance().info("Writing CSV ");
			writeMetaDataCSV(targetdir, meta);
			LogUtil.getInstance().info("Done cleansing XSD [" + file + "]");
			XSDReader xsdReader = new XSDReader(sourcedir);
			Graph<XSDNode, Edge<XSDNode>> graXsd = xsdReader.read(file);
			graXsd.setName(DataUtil.getInstance().getFilenameWithoutExtension(file) + "_gra");
			linker.link(graXsd, new GraphIndex<XSDNode, Edge<XSDNode>>(graXsd).build(), null);
			graTotal.append(graXsd, GraphOption.CHECK_DUPLICATES);
			metrics.writeGraphWithCycleInfo(targetdir, graXsd, getNodeMarkup());
		}
		linker.link(graTotal, new GraphIndex<XSDNode, Edge<XSDNode>>(graTotal).build(), null);
		metrics.writeGraphWithCycleInfo(targetdir, graTotal, getNodeMarkup());
	}

	private void appendMetaCompositeToCSV(CSVData csv, MetaComposite composite) {
		boolean appended = false;
		for (MetaElement el : composite.getElements()) {
			if (!(el.getType() instanceof MetaComposite)) {
				csv.add(new String[] { composite.getName(), el.getName(), el.getType().getName() });
				appended = true;
			} else
				appendMetaCompositeToCSV(csv, (MetaComposite) el.getType());
		}
		if (!appended)
			csv.add(new String[] { composite.getName(), "empty", "empty" });
	}

	private void writeMetaDataCSV(String targetdir, MetaData metadata) {
		CSVData csv = new CSVData();
		csv.add(SQLParser.getCsvEntityHeader());

		for (MetaType type : metadata.getTypes()) {
			if (type instanceof MetaComposite) {
				appendMetaCompositeToCSV(csv, (MetaComposite) type);
			}
		}
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + metadata.getRootTag() + ".csv", csv,
				';');
	}

	/**
	 * Creates a mapping based on <from cluster>2<to cluser> CSV files.
	 * 
	 * @param folder
	 * @param clusters
	 * @return
	 */
	protected Graph<Node, Edge<Node>> createMappingGraph(String folder, List<Cluster<Node>> clusters) {
		Graph<Node, Edge<Node>> result = new Graph<>();
		result.setName("mapping");

		for (String file : folder.split(",")) {
			Graph<FileSystemNode, Edge<FileSystemNode>> fsg = getFileGraph(file, "(?i)[^2]*2[^2]*\\.csv");
			for (FileSystemNode f : fsg.filterNodes(n -> n instanceof FileNode)) {
				appendMappingCSV2GraphAndClusters(f.getId(), result, clusters);
			}
		}

		return result;
	}

	public void oracleCompare(String function, String sourcedir, String targetdir) {
		String[] dirs = sourcedir.split(",");

		if (dirs.length > 1) {
			Graph<Node, Edge<Node>> gra1 = analyzeOracle(function, dirs[0], targetdir,
					OracleOption.ORACLE_ANALYSE_ONLY);
			Graph<Node, Edge<Node>> gra2 = analyzeOracle(function, dirs[1], targetdir,
					OracleOption.ORACLE_ANALYSE_ONLY);
			LogUtil.getInstance().info("Start comparison");
			Graph<Node, Edge<Node>> graCompared = GraphUtil.getInstance().graphCompare(gra1, gra2, null, (n1,
					n2) -> ((n1.getClass() == n2.getClass() && n1.getName().equalsIgnoreCase(n2.getName())) ? 0 : -1)

			);
			graCompared.setName("comparison");
			GraphMetrics metrics = new GraphMetrics();
			LogUtil.getInstance().info("Write comparison");
			metrics.writeGraphWithCycleInfo(targetdir, graCompared, getNodeMarkup());
		} else {
			LogUtil.getInstance().info("Specify at least two directories for the sourcedir (using ',')");
		}
	}

	/**
	 * <pre>
	 * Creert een entiteiten mapping tussen meerdere modellen op basis van CSV mapping bestanden.
	 * - Een map wordt gescanned op CSV bestanden. 
	 * 	 * De naam van deze CSV bestanden is <ModelVan>2<ModelNaar>.csv.
	 *   * Het CSV bestand bevat 2 kolommen (een VAN en NAAR kolom per entiteit)  
	 * - Op basis van de CSV bestanden worden per model clusters gemaakt (een model wordt dus op een graaf cluster afgebeeld)
	 *   * De naam van de clusters is op basis van de filename, dus <ModelA> en <ModelB>
	 *   * De beschrijving van de clusters is op basis van de kolom naam (VAN en NAAR)
	 * - Per cluster wordt het model geladen op basis van de setting soatool.mapping.cluster.ddl.<clusternaam>
	 *   * Het model wordt ge-append aan de mapping graph.
	 *   * De link tussen de mapping graph en modellen wordt gemaakt op basis van de beschrijving van het model element
	 *     * Voor een XSD is dit: de naam van het element (zie ook GraphUtil::createMetaGraph())
	 *     * Voor een DDL is dit: de tabel naam.
	 * </pre>
	 * 
	 * @param function
	 * @param sourcedir
	 * @param targetdir
	 * @param options
	 */
	public void analyzeMapping(String function, String sourcedir, String targetdir, Option... options) {
		List<Cluster<Node>> clusters = new ArrayList<>();
		Graph<Node, Edge<Node>> graMapping = createMappingGraph(sourcedir, clusters);

		// Enrich the mapping graph nodes with model information
		Graph<Node, Edge<Node>> graMappingEnriched = new Graph<>();
		graMappingEnriched = graMapping.shallowCopy();

		for (Cluster<Node> cluster : clusters) {
			// determine the DDL filename for this cluster
			String file = getSetting(SOA_TOOL_MAPPING_CLUSTER_DDL_PREFIX, cluster.getName());
			if (null != file && !file.isEmpty()) {
				Graph<Node, Edge<Node>> graMeta = null;
				MetaData meta = null;
				Comparator<Node> enrichComparator = (o1, o2) -> {
					int result = (o1.getId().equalsIgnoreCase(cluster.getName() + DataUtil.PATH_SEPARATOR + StringUtil
							.getInstance().replace(o2.getDescription(), "\\[?([^\\]]*)\\]?\\.\\[?([^\\]]*)\\]?,$2")))
									? 0
									: 1;
					return result;
				};

				// try to read the model information
				if (XSDConst.FILE_EXTENSION_XSD.equalsIgnoreCase(DataUtil.getInstance().getFileExtension(file))) {
					SimpleXSDReader reader = new SimpleXSDReader();
					meta = reader.read(file);
					graMeta = GraphUtil.getInstance().createMetaGraph(meta);
				} else {
					SQLParserResult result = parseDDL(file, Dialect.ORACLE.name());
					if (!result.isEmpty()) {
						meta = result.getMetaData();
						graMeta = result.getMetaGraph();
					}
				}
				// check if model information is found
				if (null != graMeta) {
					graMappingEnriched.append(graMeta, enrichComparator);
					// Add all added nodes to the cluster
					for (Node metaNode : graMeta.getNodes()) {
						boolean found = false;
						for (Node clusterNode : cluster.getNodes()) {
							if (enrichComparator.compare(clusterNode, metaNode) == 0) {
								found = true;
								break;
							}
						}
						if (!found)
							cluster.addNode(metaNode);
					}
				}
				if (null != meta) {
					XSDGenerator gen = new XSDGenerator();
					gen.setTargetdir(targetdir);
					meta.setRootTag(DataUtil.getInstance().getFilenameWithoutExtension(file));
					gen.generate(meta);
				}
			}
		}

		GraphMetrics metrics = new GraphMetrics();
		metrics.writeGraphWithCycleInfo(targetdir, graMapping, clusters, getNodeMarkup());
		metrics.writeGraphWithCycleInfo(targetdir, graMappingEnriched, clusters, getNodeMarkup());
		if (EnumUtil.getInstance().contains(options, Option.CLIENT)) {
			LogUtil.getInstance().info("Write mapping analysis to graph store (neo4j)");
			writeGraphToNeo4J(graMappingEnriched, NEO4J_MAPPING_STORE);
		} else {
			LogUtil.getInstance().info("Skipped writing mapping analysis to graph store (neo4j)");
		}

	}

	/**
	 * Map XML
	 * 
	 * @param function
	 * @param sourcedir
	 * @param targetdir
	 * @param options
	 */
	public void mapXML(String function, String sourcedir, String targetdir, Option... options) {
		Map<String, Integer> mapGemeente = new HashMap<>();
		String localNameRegex = ConfigurationUtil.getInstance().getSetting(SOATOOL_MAPXML_TEST_LOCALNAME_REGEX);
		forEachDir(sourcedir, (fsg) -> {
			for (FileSystemNode fn : fsg.filterNodes(f -> f instanceof FileNode)) {
				XMLUtil.getInstance().parse(fn.getId(), new XMLSAXHandler() {
					public void endElement(String uri, String localName, String qName) {
						if (localName.matches(localNameRegex)) {
							String tmp = getData();
							Integer count = mapGemeente.get(tmp);

							if (null == count)
								count = 1;
							else
								count = count + 1;
							mapGemeente.put(tmp, count);
						}
					}
				});
			}
			return true;
		});
		CSVData csvData = new CSVData();
		csvData.setHeader(new String[] { "GEMEENTE_CODE", "AANTAL" });
		for (Entry<String, Integer> e : mapGemeente.entrySet())
			csvData.add(new String[] { e.getKey(), "" + e.getValue() });
		CSVUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + "gemeente_code_count.csv", csvData,
				';');
	}

	/**
	 * Append CSV mapping information to a mapping graph. The cluster information is
	 * determined based the filename (<ModelFrom>2<ModelTarget>.CSV). The node name
	 * is prefixed by the column header.
	 * 
	 * @param file
	 * @param gra
	 * @param clusters
	 */
	private void appendMappingCSV2GraphAndClusters(String file, Graph<Node, Edge<Node>> gra,
			List<Cluster<Node>> clusters) {
		class NodeNameMaker {
			public String make(String prefix, String name) {
				return prefix + DataUtil.PATH_SEPARATOR + name;
			}
		}
		NodeNameMaker maker = new NodeNameMaker();
		String[] names = DataUtil.getInstance().getFilenameWithoutExtension(file).split("2");
		String sourcePrefix = names[0];
		String targetPrefix = names[1];
		CSVData csvData = CSVUtil.getInstance().readFromFile(file, ';', CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		Cluster<Node> sourceCluster = null;
		Cluster<Node> targetCluster = null;
		String[] header = csvData.getHeader();
		// If they do not exist yet, add clusters for source and target
		String sourceClusterdescription = header[0];
		String targetClusterdescription = header[1];
		sourceCluster = findClusterByName(clusters, sourcePrefix);
		if (null == sourceCluster) {
			sourceCluster = new Cluster<Node>();
			sourceCluster.setName(sourcePrefix);
			sourceCluster.setDescription(sourceClusterdescription);
			clusters.add(sourceCluster);
		}
		targetCluster = findClusterByName(clusters, targetPrefix);
		if (null == targetCluster) {
			targetCluster = new Cluster<Node>();
			targetCluster.setName(targetPrefix);
			targetCluster.setDescription(targetClusterdescription);
			clusters.add(targetCluster);
		}

		for (String[] line : csvData.getLines()) {
			if (line.length >= 0) {
				Node source = null;
				Node target = null;

				if (null != line[0] && !line[0].isEmpty()) {
					source = new Node(maker.make(sourcePrefix, line[0]), line[0], line[0]);
					if (!gra.exists(source, null)) {
						gra.addNode(source);
						sourceCluster.addNode(source);
					}
				}
				if (null != line[1] && !line[1].isEmpty()) {
					target = new Node(maker.make(targetPrefix, line[1]), line[1], line[1]);
					if (!gra.exists(target, null)) {
						targetCluster.addNode(target);
						gra.addNode(target);
					}
				}
				if (null != source && null != target) {
					gra.addEdge(new Edge<>(source, target, "MAPS_TO"), GraphOption.CHECK_DUPLICATES);
				}

			}
		}
	}

	private Cluster<Node> findClusterByName(List<Cluster<Node>> clusters, String name) {
		Cluster<Node> result = null;
		for (Cluster<Node> cluster : clusters) {
			if (cluster.getName().equals(name)) {
				result = cluster;
				break;
			}
		}

		return result;
	}

	private static boolean hasOverlappingEndpoints(BindingNode node1, BindingNode node2) {
		boolean result = false;

		for (String ep1 : node1.getEndpoints()) {
			for (String ep2 : node2.getEndpoints()) {
				if (isSameEndpoint(ep1, ep2)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private static boolean isSameEndpoint(String ep1, String ep2) {
		boolean result = false;

		Pattern pattern = Pattern.compile("[^:]*://[^/]*(.*)");

		String[][] source = new String[][] { { ep1, null }, { ep2, null } };
		for (String[] s : source) {
			Matcher m = pattern.matcher(s[0]);
			if (m.matches() && m.groupCount() > 0) {
				int groupCount = m.groupCount();
				String tmp = m.group(groupCount);
				s[1] = tmp;
			}
		}
		if (null != source[0][1] && null != source[1][1] && source[0][1].equals(source[1][1])) {
			result = true;
		}

		return result;
	}

	public CSVData getNodeMarkup() {
		if (null == cacheNodeMarkup)
			cacheNodeMarkup = CSVUtil.getInstance().readFromFile(
					ConfigurationUtil.getInstance().getSetting(SOATOOL_GRAPH_MARKUP), ';',
					CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		return cacheNodeMarkup;
	}

	interface Analyzer {
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems);
	}

	class OSBAnalyzer implements Analyzer {
		private Map<String, Integer> serviceReferenceTypes = new HashMap<>();
		private Map<String, Integer> transportProviders = new HashMap<>();
		private Map<String, Integer> bindingTypes = new HashMap<>();

		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();
			Graph<OSBNode, Edge<OSBNode>> graOsb = new Graph<>();
			Graph<OSBNode, Edge<OSBNode>> graFlow = new Graph<>();
			graFlow.setName(GRAPH_NAME_FLOW);
			graOsb.setName(GRAPH_NAME_OSB);
			result.add(graFlow);
			result.add(graOsb);
			LogUtil.getInstance().info("Start analyzing OSB artifacts");
			for (FileSystemNode fn : files
					.filterNodes(f -> f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_PROXY_SERVICE)
							|| f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_BUSINESS_SERVICE)
							|| f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_BUSINESS_SERVICE_X)
							|| f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_PIPELINE)
							|| f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_ALERT)
							|| f.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_FLOW))) {
				try {
					if (fn.getId().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_FLOW)) {
						BPELReader bpelReader = new BPELReader(sourcedir);
						Graph<BPELNode, Edge<BPELNode>> tmp = bpelReader.read(fn.getId());
						graFlow.append(tmp, GraphOption.CHECK_DUPLICATES);
					} else {
						collectServiceReferenceType(fn.getId());
						OSBReader osbReader = new OSBReader(sourcedir);
						Graph<OSBNode, Edge<OSBNode>> tmp = osbReader.read(fn.getId());
						graOsb.append(tmp, GraphOption.CHECK_DUPLICATES);
						write(targetdir);
					}

				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading OSB [" + fn.getId() + "]", e);
					problems.add(new String[] { "OSB", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing OSB artifacts");

			return result;
		}

		public void collectServiceReferenceType(String filename) {
			ServiceReferenceType handler = new ServiceReferenceType();

			XMLUtil.getInstance().parse(filename, handler);

			for (String type : handler.getServiceReferenceTypes()) {
				Integer current = serviceReferenceTypes.get(type);

				serviceReferenceTypes.put(type, (null == current) ? 1 : ++current);
			}
			{
				Integer current = transportProviders.get(handler.getTransportProvider());
				transportProviders.put(handler.getTransportProvider(), (null == current) ? 1 : ++current);

			}
			{
				Integer current = bindingTypes.get(handler.getBindingType());
				bindingTypes.put(handler.getBindingType(), (null == current) ? 1 : ++current);
			}
		}

		public void write(String targetdir) {
			String filename = targetdir + DataUtil.PATH_SEPARATOR + "osb_domain_values.csv";
			CSVData csvData = new CSVData();
			csvData.setHeader(new String[] { "DOMAIN", "VALUE", "COUNT" });
			for (Entry<String, Integer> e : serviceReferenceTypes.entrySet())
				csvData.add(new String[] { "REFERENCE_TYPE", e.getKey(), "" + e.getValue() });
			for (Entry<String, Integer> e : transportProviders.entrySet())
				csvData.add(new String[] { "TRANSPORT_PROVIDER", e.getKey(), "" + e.getValue() });
			for (Entry<String, Integer> e : bindingTypes.entrySet())
				csvData.add(new String[] { "BINDING_TYPE", e.getKey(), "" + e.getValue() });

			CSVUtil.getInstance().writeToFile(filename, csvData, ';');
		}
	}

	class WSDLAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();
			Graph<WSDLNode, Edge<WSDLNode>> graWsdl = new Graph<>();
			graWsdl.setName("soa-wsdl");
			result.add(graWsdl);
			LogUtil.getInstance().info("Start analyzing WSDL artifacts");
			for (FileSystemNode fn : files
					.filterNodes(f -> f.getId().toLowerCase().endsWith(WSDLConst.FILE_EXTENSION_WSDL))) {
				try {
					WSDLReader wsdlReader = new WSDLReader(sourcedir);
					Graph<WSDLNode, Edge<WSDLNode>> tmp = wsdlReader.read(fn.getId());
					graWsdl.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading WSDL [" + fn.getId() + "]", e);
					problems.add(new String[] { "WSDL", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing WSDL artifacts");
			return result;
		}
	}

	class XSDAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();
			Graph<XSDNode, Edge<XSDNode>> graXsd = new Graph<>();
			graXsd.setName(GRAPH_NAME_XSD);
			result.add(graXsd);
			LogUtil.getInstance().info("Start analyzing XSD artifacts");
			for (FileSystemNode fn : files.filterNodes(
					f -> f instanceof FileNode && (f.getId().toLowerCase().endsWith(XSDConst.FILE_EXTENSION_XSD)
							|| f.getId().toLowerCase().endsWith(WSDLConst.FILE_EXTENSION_WSDL)))) {
				try {
					XSDReader xsdReader = new XSDReader(sourcedir);
					Graph<XSDNode, Edge<XSDNode>> tmp = xsdReader.read(fn.getId());
					graXsd.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading XSD [" + fn.getId() + "]", e);
					problems.add(new String[] { "XSD", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing XSD artifacts");
			return result;
		}
	}

	class DescisionAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();

			Graph<DescisionNode, Edge<DescisionNode>> graDesc = new Graph<>();
			graDesc.setName(GRAPH_NAME_DESC);
			result.add(graDesc);
			LogUtil.getInstance().info("Start analyzing Descision [rule engine] artifacts");
			for (FileSystemNode fn : files
					.filterNodes(f -> f.getId().toLowerCase().endsWith(DescisionConst.FILE_EXTENSION_DESCISION))) {
				try {
					DescisionReader decsReader = new DescisionReader(sourcedir);
					Graph<DescisionNode, Edge<DescisionNode>> tmp = decsReader.read(fn.getId());
					graDesc.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading Descision [" + fn.getId() + "]", e);
					problems.add(new String[] { "Descision", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing Descision [rule engine] artifacts");
			return result;
		}
	}

	class BPMNAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();

			Graph<SCANode, Edge<SCANode>> graSca = new Graph<>();
			graSca.setName(GRAPH_NAME_BPMN);
			result.add(graSca);
			LogUtil.getInstance().info("Start analyzing BPMN artifacts");
			for (FileSystemNode fn : files
					.filterNodes(f -> f.getId().toLowerCase().endsWith(BPMNConst.FILE_EXTENSION_BPMN))) {
				try {
					BPMNReader bpmnReader = new BPMNReader(sourcedir);
					Graph<BPMNNode, Edge<BPMNNode>> tmp = bpmnReader.read(fn.getId());
					graSca.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading BPMN [" + fn.getId() + "]", e);
					problems.add(new String[] { "BPMN", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing BPMN artifacts");
			return result;
		}
	}

	class SCAAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();

			Graph<SCANode, Edge<SCANode>> graSca = new Graph<>();
			graSca.setName(GRAPH_NAME_SCA);
			result.add(graSca);
			LogUtil.getInstance().info("Start analyzing SCA artifacts");
			for (FileSystemNode fn : files.filterNodes(
					f -> f.getId().toLowerCase().endsWith("." + SCAConst.FILE_EXTENSION_COMPONENT_TYPE.toLowerCase())
							|| f.getId().toLowerCase().endsWith(SCAConst.FILENAME_COMPOSITE))) {
				try {
					SCAReader scaReader = new SCAReader(sourcedir);
					Graph<SCANode, Edge<SCANode>> tmp = scaReader.read(fn.getId());
					graSca.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading SCA [" + fn.getId() + "]", e);
					problems.add(new String[] { "SCA", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Connecting SCA wires");
			List<Edge<SCANode>> wires = new ArrayList<>();
			List<SCANode> wires2Remove = new ArrayList<>();
			for (SCANode wire : graSca.filterNodes(n -> n instanceof WireNode)) {
				WireNode wn = (WireNode) wire;
				Optional<SCANode> source = graSca.getNodes().stream().filter(n -> n.getId().equals(wn.getSourceUri()))
						.findFirst();
				Optional<SCANode> target = graSca.getNodes().stream().filter(n -> n.getId().equals(wn.getTargetUri()))
						.findFirst();
				if (source.isPresent() && target.isPresent()) {
					Edge<SCANode> connection = new Edge<>(source.get(), target.get(), EDGE_TYPE_WIRED_TO);
					wires.add(connection);
					// add reverse wire for asynchronous communication
					if (source.get() instanceof Interface
							&& ((Interface) source.get()).getInteractionType() == Interface.InteractionType.ASYNCHRONOUS
							&& target.get() instanceof Interface && ((Interface) target.get())
									.getInteractionType() == Interface.InteractionType.ASYNCHRONOUS) {
						connection = new Edge<>(target.get(), source.get(), EDGE_TYPE_REVERSE_WIRED_TO);
						wires.add(connection);
					}
					wires2Remove.add(wire);
				}
			}
			for (Edge<SCANode> wire : wires) {
				graSca.addEdge(wire, GraphOption.CHECK_DUPLICATES);
			}
			for (SCANode wire : wires2Remove)
				graSca.removeNode(wire);
			LogUtil.getInstance().info("Done analyzing SCA artifacts");
			return result;
		}
	}

	class JCAAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();

			Graph<JCANode, Edge<JCANode>> graJca = new Graph<>();
			graJca.setName(GRAPH_NAME_JCA);
			result.add(graJca);
			LogUtil.getInstance().info("Start analyzing JCA artifacts");
			for (FileSystemNode fn : files
					.filterNodes(f -> f.getId().toLowerCase().endsWith(JCAConst.FILE_EXTENSION_JCA.toLowerCase()))) {
				try {
					JCAReader jcaReader = new JCAReader(sourcedir);
					Graph<JCANode, Edge<JCANode>> tmp = jcaReader.read(fn.getId());
					graJca.append(tmp, GraphOption.CHECK_DUPLICATES);
				} catch (Exception e) {
					LogUtil.getInstance().error("problem reading JCA [" + fn.getId() + "]", e);
					problems.add(new String[] { "JCA", fn.getId(), LogUtil.getInstance().formatException(e) });
				}
			}
			LogUtil.getInstance().info("Done analyzing JCA artifacts");
			return result;
		}
	}

	class WLSAnalyzer implements Analyzer {
		@Override
		public List<Graph<?, ?>> analyze(String sourcedir, String targetdir,
				Graph<FileSystemNode, Edge<FileSystemNode>> files, CSVData problems) {
			List<Graph<?, ?>> result = new ArrayList<>();

			Graph<WLSNode, Edge<WLSNode>> graWlsConfig = new Graph<>();
			graWlsConfig.setName(GRAPH_NAME_WLS);
			result.add(graWlsConfig);
			LogUtil.getInstance().info("Start analyzing WebLogic artifacts");
			WLSReader wlsReader = new WLSReader(sourcedir);
			String wlsconfig = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_WLS_CONFIG);
			if (null != wlsconfig && !wlsconfig.isEmpty())
				graWlsConfig = wlsReader.read(wlsconfig);
			LogUtil.getInstance().info("Done analyzing WebLogic artifacts");

			return result;
		}
	}
}

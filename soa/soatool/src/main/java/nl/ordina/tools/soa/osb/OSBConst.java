package nl.ordina.tools.soa.osb;

import data.DataUtil;

public interface OSBConst {
	String NS_OSB_TRANSFORM = "http://www.bea.com/wli/sb/stages/transform/config";
	String NS_OSB_TYPESYSTEM = "http://www.bea.com/wli/sb/typesystem/config";
	String NS_OSB_ROUTING = "http://www.bea.com/wli/sb/stages/routing/config";
	String NS_OSB_TRANSPORTS = "http://www.bea.com/wli/sb/transports";
	String NS_OSB_SERVICES = "http://www.bea.com/wli/sb/services";
	String NS_OSB_STAGES = "http://www.bea.com/wli/sb/stages/config";
	String NS_OSB_SERVICES_BINDINGS = "http://www.bea.com/wli/sb/services/bindings/config";
	String NS_OSB_BUSINESS_SERVICE = "http://xmlns.oracle.com/servicebus/business/config";
	String NS_OSB_LOGGING = "http://www.bea.com/wli/sb/stages/logging/config";
	String NS_OSB_PIPELINE = "http://www.bea.com/wli/sb/pipeline/config";
	String NS_OSB_ALERT = "http://www.bea.com/wli/sb/stages/alert/config";
	String NS_OSB_MONITORING = "http://www.bea.com/wli/monitoring/alert";
	String EL_CALLOUT = "wsCallout";
	String EL_SERVICE = "service";
	String EL_VARIABLE = "variable";
	String EL_VARIABLE_NAME = "varName";
	String EL_XPATH_TEXT = "xpathText";
	String EL_INVOKE = "invoke";
	String EL_LABELS = "labels";
	String EL_XML_FRAGMENT = "xml-fragment";
	String EL_CONTEXT = "context";
	String EL_STAGE = "stage";
	String EL_WSDL = "wsdl";
	String EL_KEY = "key";
	String EL_ASSIGN = "assign";
	String EL_ID = "id";
	String EL_REPLACE = "replace";
	String EL_INSERT = "insert";
	String EL_XQUERYTEXT = "xqueryText";
	String EL_LOG = "log";
	String EL_REPORT = "report";
	String EL_MESSAGE = "message";
	String EL_DESCRIPTION = "description";
	String EL_LOG_LEVEL = "logLevel";
	String EL_SEVERITY = "severity";
	String EL_ALERT = "alert";
	String EL_DESTINATION = "destination";
	String EL_PROXY_SERVICE_ENTRY = "proxyServiceEntry";;
	String EL_BUSINESS_SERVICE_ENTRY = "businessServiceEntry";
	String EL_PIPELINE_ENTRY = "pipelineEntry";
	String EL_PIPELINE = "pipeline";
	String EL_DISPATCH_POLICY = "dispatch-policy";
	String EL_RESOURCE = "resource";
	String EL_INPUT = "input";
	String EL_XSLT_TRANSFORM = "xsltTransform";
	String EL_QXUERY_TRANSFORM = "xqueryTransform";
	String EL_PARAM = "param";
	String EL_PATH = "path";
	String EL_ROUTE_NODE = "route-node";
	String EL_BINDING = "binding";
	String EL_FLOW = "flow";
	String ATTR_REF = "ref";
	String ATTR_NAME = "name";
	String ATTR_VAR_NAME = "varName";
	String ATTR_TYPE = "type";
	String ATTR_PATH = "path";
	String PATH_CONFIG_URI_VALUE = "config/URI/value";
	String EL_ALERT_TO_CONSOLE = "AlertToConsole";
	String EL_ALERT_TO_REPORTING_DATASET = "AlertToReportingDataSet";
	String EL_ALERT_TO_SNMP = "AlertToSMNP";
	String PATH_ENDPOINT_CONFIG_URI_VALUE = "endpointConfig/URI/value";
	String PATH_ENDPOINT_PROVIDER_ID = "endpointConfig/provider-id";
	String PATH_CORE_ENTRY_BINDING_WSDL = "coreEntry/binding/wsdl";
	String PATH_CORE_ENTRY_BINDING = "coreEntry/binding";
	String PATH_CORE_ENTRY_TRANSACTIONS = "coreEntry/transactions";
	String PATH_BINDING_NAME = "binding/name";
	String PATH_BINDING_NAMESPACE = "binding/namespace";
	String PATH_PORT_NAME = "port/name";
	String PATH_ROUTER_FLOW = "router/flow";
	String PATH_PORT_NAMESPACE = "port/namespace";
	String PATH_TRANSFORM_INPUT = "Transform/" + EL_INPUT;
	String PATH_XQUERY_TRANSFORM_RESOURCE = OSBConst.EL_QXUERY_TRANSFORM + DataUtil.PATH_SEPARATOR
			+ OSBConst.EL_RESOURCE;
	String PATH_XQUERY_TRANSFORM_PARAMETER = OSBConst.EL_QXUERY_TRANSFORM + DataUtil.PATH_SEPARATOR + OSBConst.EL_PARAM;
	String PATH_XSLT_TRANSFORM_RESOURCE = OSBConst.EL_XSLT_TRANSFORM + DataUtil.PATH_SEPARATOR + OSBConst.EL_RESOURCE;
	String PATH_XSLT_TRANSFORM_PARAMETER = OSBConst.EL_XSLT_TRANSFORM + DataUtil.PATH_SEPARATOR + OSBConst.EL_PARAM;
	String PATH_PARAMETER_VALUE = OSBConst.EL_PARAM + DataUtil.PATH_SEPARATOR + OSBConst.EL_PATH;
	String PATH_INSERT_ID = OSBConst.EL_INSERT + DataUtil.PATH_SEPARATOR + OSBConst.EL_ID;
	String PATH_REPLACE_ID = OSBConst.EL_REPLACE + DataUtil.PATH_SEPARATOR + OSBConst.EL_ID;
	String PATH_ASSIGN_ID = OSBConst.EL_ASSIGN + DataUtil.PATH_SEPARATOR + OSBConst.EL_ID;
	String FILE_EXTENSION_BUSINESS_SERVICE = ".biz";
	String FILE_EXTENSION_BUSINESS_SERVICE_X = ".bix";
	String FILE_EXTENSION_FLOW = ".flow";
	String FILE_EXTENSION_PROXY_SERVICE = ".proxy";
	String FILE_EXTENSION_PIPELINE = ".pipeline";
	String FILE_EXTENSION_ALERT = ".alert";
	String EL_TRANSACTIONS = "transactions";
	String ATTR_TRANSACTION_REQUIRED = "isRequired";
	String ATTR_SAME_TRANSACTION_FOR_RESPONSE = "sameTxForResponse";
	String ATTR_ERROR_HANDLER = "errorHandler";
}

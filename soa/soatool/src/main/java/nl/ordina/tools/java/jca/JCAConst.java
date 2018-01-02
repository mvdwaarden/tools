package nl.ordina.tools.java.jca;

public interface JCAConst {
	String NS_JCA_META = "http://platform.integration.oracle/blocks/adapter/fw/metadata";
	String EL_ADAPTER_CONFIG = "adapter-config";
	String EL_ENDPOINT_INTERACTION = "endpoint-interaction";
	String EL_ENDPOINT_ACTIVATION = "endpoint-activation";
	String EL_CONNECTION_FACTORY = "connection-factory";
	String EL_INTERACTION_SPECFICATION = "interaction-spec";
	String EL_ACTIVATION_SPECIFICATION = "activation-spec";
	String PATH_INTERATION_SPECIFICATION_PROPERTY = "interaction-spec/property";
	String PATH_ACTIVATION_SPECIFICATION_PROPERTY = "activation-spec/property";
	String ATTR_PORT_TYPE = "portType";
	String ATTR_OPERATION = "operation";
	String ATTR_LOCATION = "location";
	String ATTR_WSDL_LOCATION = "wsdlLocation";
	String ATTR_ADAPTER = "adapter";
	String ATTR_INTERFACE = "interface";
	String ATTR_NAME = "name";
	String ATTR_VALUE = "value";
	String ATTR_CLASS_NAME = "className";
	String PROP_KEY_DESTINATION_NAME = "DestinationName";
	String PROP_KEY_DELIVERY_MODE = "DeliveryMode";
	String PROP_KEY_PAYLOAD_TYPE = "PayloadType";
	String PROP_KEY_SCHEMA_NAME = "SchemaName";
	String PROP_KEY_PACKAGE_NAME = "PackageName";
	String PROP_KEY_PROCEDURE_NAME = "ProcedureName";
	String PROP_KEY_SQL_STRING = "SqlString";
	String FILE_EXTENSION_JCA = ".jca";
	String ENUM_ADAPTER_TYPE_JMS = "JMS Adapter";
	String ENUM_ADAPTER_TYPE_DATABASE = "Database Adapter";
}

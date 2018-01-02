package nl.ordina.tools.soa.sca;

public interface SCAConst {
	String NS_SCA = "http://xmlns.oracle.com/sca/1.0";
	String NS_DESIGNER = "http://xmlns.oracle.com/soa/designer/";
	String NS_SOA_DESIGNER = "http://xmlns.oracle.com/soa/designer/";
	String EL_COMPONENT = "component";
	String EL_COMPOSITE = "composite";
	String EL_COMPONENT_TYPE = "componentType";	
	String EL_SERVICE = "service";
	String EL_WIRE = "wire";
	String EL_DEFINITION = "definition";
	String EL_SOURCE_URI = "source.uri";
	String EL_TARGET_URI = "target.uri";
	String EL_REFERENCE = "reference";
	String EL_INTERFACE_WSDL = "interface.wsdl";
	String EL_BINDING_JCA = "binding.jca";
	String EL_IMPLEMENTATION_BPMN = "implementation.bpmn";
	String EL_IMPLEMENTATION_MEDIATOR = "implementation.mediator";
	String EL_IMPLEMENTATION_BPEL = "implementation.bpel";
	String EL_IMPLEMENTATION_WORKFLOW = "implementation.workflow";
	String EL_IMPLEMENTATION_DECISION = "implementation.decision";
	String ATTR_INTERFACE = "interface";
	String ATTR_CALLBACK_INTERFACE = "callbackInterface";
	String ATTR_CONFIG = "config";
	String ATTR_WSDL_LOCATION = "wsdlLocation";
	String ATTR_INTERFACDE = "interface";
	String ATTR_ID = "id";
	String ATTR_MODE = "mode";
	String ATTR_LABEL = "label";
	String ATTR_STATE = "state";
	String ATTR_REVISION = "revision";
	String ATTR_NAME = "name";
	String ATTR_SRC = "src";
	String FILENAME_COMPOSITE = "composite.xml";
	String FILE_EXTENSION_COMPONENT_TYPE = "componentType";
	String FILE_EXTENSION_BPEL = ".bpel";
	String REFERENCE_NAME_REPLACE_FORMAT = "(?i)(.*\\.Externals\\.)?([^\\.]*)\\.(service|reference),$2,(.*\\/)?([^\\/]*),$2";
}
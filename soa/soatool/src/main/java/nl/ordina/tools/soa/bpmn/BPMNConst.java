package nl.ordina.tools.soa.bpmn;

public interface BPMNConst {
	String NS_BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";
	String NS_BPM_ORACLE_EXTENSION = "http://xmlns.oracle.com/bpm/OracleExtensions";
	String EL_PROCESS_CALL_CONVERSATIONAL_DEFINITION = "ProcessCallConversationalDefinition";	
	String EL_SERVICE_TASK = "serviceTask";
	String EL_SEND_TASK = "sendTask";
	String EL_RECEIVE_TASK = "receiveTask";
	String EL_START_EVENT = "startEvent";
	String EL_END_EVENT = "endEvent";	
	String EL_PROCESS = "process";
	String EL_SUBPROCESS = "subProcess";
	String ATTR_ID = "id";
	String ATTR_MODE = "mode";
	String ATTR_LABEL = "label";
	String ATTR_STATE = "state";
	String ATTR_REVISION = "revision";
	String ATTR_NAME = "name";
	String ATTR_MESSAGE_REF = "messageRef";
	String ATTR_OPERATION_REF = "operationRef";
	String ATTR_TARGET_CONVERSATIONAL = "targetConversational";	
	String ATTR_IMPLEMENTATION = "implementation";
	String FILE_EXTENSION_BPMN = ".bpmn";	
}

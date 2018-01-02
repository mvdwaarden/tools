package nl.ordina.tools.soa.bpmn.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import graph.dm.Edge;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.soa.bpmn.BPMNConst;
import nl.ordina.tools.soa.bpmn.graph.dm.BPMNNode;
import nl.ordina.tools.soa.bpmn.graph.dm.EventNode;
import nl.ordina.tools.soa.bpmn.graph.dm.ProcessNode;
import nl.ordina.tools.soa.bpmn.graph.dm.TaskNode;


/**
 * 
 * @author mwa17610
 *
 */
public class BPMNFactory extends GraphHandlerFactory<BPMNNode, Edge<BPMNNode>> {
	public BPMNFactory(BPMNSAXHandler handler) {
		super(handler);
	}

	@Override
	public BPMNNode createNode(String uri, String localName, String qName, Attributes atts) {
		BPMNNode result = null;
		if (nodeTest(qName, BPMNConst.NS_BPM_ORACLE_EXTENSION, BPMNConst.EL_PROCESS_CALL_CONVERSATIONAL_DEFINITION, uri,
				localName) && getCurrentNode() instanceof TaskNode) {
			((TaskNode) getCurrentNode()).setImplementation(atts.getValue(BPMNConst.ATTR_TARGET_CONVERSATIONAL));
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_START_EVENT, uri, localName)) {
			EventNode event = null;
			result = event = new EventNode();
			event.setType(EventNode.Type.START_EVENT);
			setEventName(event, atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_END_EVENT, uri, localName)) {
			EventNode event = null;
			result = event = new EventNode();
			event.setType(EventNode.Type.END_EVENT);
			setEventName(event, atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_PROCESS, uri, localName)) {
			result = createProcessNode(ProcessNode.Type.PROCESS, atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_SUBPROCESS, uri, localName)) {
			result = createProcessNode(ProcessNode.Type.SUBPROCESS, atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_SERVICE_TASK, uri, localName)) {
			result = createTaskNode(atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_SEND_TASK, uri, localName)) {
			result = createSendTaskNode(atts);
		} else if (nodeTest(qName, BPMNConst.NS_BPMN, BPMNConst.EL_RECEIVE_TASK, uri, localName)) {
			result = createReceiveTaskNode(atts);
		}
		return result;
	}

	private TaskNode createSendTaskNode(Attributes atts) {
		TaskNode result = null;

		String messageRef = atts.getValue(BPMNConst.ATTR_MESSAGE_REF);

		result = createTaskNode(TaskNode.Type.SEND_CALL,
				DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()) + DataUtil.PATH_SEPARATOR
						+ messageRef,
				atts);

		return result;
	}

	private TaskNode createReceiveTaskNode(Attributes atts) {
		TaskNode result = null;

		String messageRef = atts.getValue(BPMNConst.ATTR_MESSAGE_REF);
		// strip the ".Callback" text
		messageRef = DataUtil.getInstance().getFilenameWithoutExtension(messageRef);

		result = createTaskNode(TaskNode.Type.RECEIVE_CALL,
				DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()) + DataUtil.PATH_SEPARATOR
						+ messageRef,
				atts);

		return result;
	}

	private TaskNode createTaskNode(Attributes atts) {
		TaskNode result = null;

		String implementation = atts.getValue(BPMNConst.ATTR_IMPLEMENTATION);
		if (null != implementation && !implementation.isEmpty()) {
			result = createTaskNode(TaskNode.Type.SERVICE_CALL,
					DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()) + DataUtil.PATH_SEPARATOR
							+ implementation,
					atts);
		} else {
			result = createTaskNode(TaskNode.Type.PROCESS_CALL, atts.getValue(BPMNConst.ATTR_NAME), atts);
		}

		return result;
	}

	private TaskNode createTaskNode(TaskNode.Type type, String implementation, Attributes atts) {
		TaskNode result = null;

		result = new TaskNode();
		setNodeValuesIfEmpty(result, atts.getValue(BPMNConst.ATTR_ID));
		result.setImplementation(implementation);
		result.setDescription(atts.getValue(BPMNConst.ATTR_NAME));
		result.setType(type);
		result.setOperation(atts.getValue(BPMNConst.ATTR_OPERATION_REF));

		return result;
	}

	private ProcessNode createProcessNode(ProcessNode.Type type, Attributes atts) {
		ProcessNode result = new ProcessNode();

		result.setType(type);
		switch (type) {
		case PROCESS:
			setNodeValuesIfEmpty(result, getSourceUrl());
			break;
		case SUBPROCESS:
			setNodeValuesIfEmpty(result, DataUtil.getInstance().removeExtension(getSourceUrl()) + DataUtil.PATH_SEPARATOR
					+ atts.getValue(BPMNConst.ATTR_NAME));
			break;
		}

		result.setDescription(atts.getValue(BPMNConst.ATTR_NAME));

		return result;
	}

	public void setEventName(EventNode node, Attributes atts) {
		setNodeValuesIfEmpty(node, DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()) + " - "
				+ atts.getValue(BPMNConst.ATTR_NAME));
		node.setId(atts.getValue(BPMNConst.ATTR_ID));
	}

	@SuppressWarnings({ "unchecked", "incomplete-switch" })
	@Override
	public List<Edge<BPMNNode>> createEdges(BPMNNode source, BPMNNode target) {
		List<Edge<BPMNNode>> result = null;
		if (source instanceof ProcessNode && target instanceof TaskNode) {
			// append a reference node for a tasknode
			TaskNode tasknode = (TaskNode) target;

			switch (tasknode.getType()) {
			case PROCESS_CALL:
				EventNode event = new EventNode();
				setNodeValuesIfEmpty(event, tasknode.getImplementation());
				result = createList(new Edge[] { new Edge<>(source, target), new Edge<>(target, event) });
				break;
			}
		}

		if (null == result)
			result = createList(new Edge[] { new Edge<>(source, target) });

		return result;
	}

	@Override
	public void postNodeCreation(BPMNNode currentNode, String uri, String localName, String qName, StringBuilder data) {

	}

}

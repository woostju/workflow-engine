package com.github.bryx.workflow.domain.process.buildtime;

import lombok.Data;
import org.activiti.bpmn.model.*;
import org.springframework.beans.BeanUtils;

/**
 * the base element in process def
 */
@Data
public class ProcessDefElement {
	public enum ProcessDefElementType {
		USER_TASK,
		FIRST_USER_TASK,
		START_NODE,
		END_NODE,
		EXCLUSIVE_GATEWAY,
		PARALLEL_GATEWAY,
		SEQUENCE_FLOW
	}
	private ProcessDefElementType type;
	private String id;
	private String name;
	private String processDefId;
	private String documentation;

	public static ProcessDefElement of(FlowElement flowElement){
		ProcessDefElement defElement = null;
		if(flowElement instanceof UserTask){
			TaskDef task = new TaskDef();
			task.setType(ProcessDefElementType.USER_TASK);
			BeanUtils.copyProperties(flowElement, task);
			defElement = task;
		}else if(flowElement instanceof EndEvent){
			defElement = new ProcessDefElement();
			defElement.setType(ProcessDefElementType.END_NODE);
			BeanUtils.copyProperties(flowElement, defElement);
		}else if(flowElement instanceof  StartEvent){
			defElement = new ProcessDefElement();
			defElement.setType(ProcessDefElementType.START_NODE);
			BeanUtils.copyProperties(flowElement, defElement);
		}else if (flowElement instanceof SequenceFlow) {
			SequenceDef sequence = new SequenceDef();
			sequence.setType(ProcessDefElementType.SEQUENCE_FLOW);
			BeanUtils.copyProperties(flowElement, sequence);
			defElement = sequence;
		}else if (flowElement instanceof ExclusiveGateway) {
			ExclusiveGatewayDef exclusiveGatewayDef = new ExclusiveGatewayDef();
			exclusiveGatewayDef.setType(ProcessDefElementType.EXCLUSIVE_GATEWAY);
			BeanUtils.copyProperties(flowElement, exclusiveGatewayDef);
			defElement = exclusiveGatewayDef;
		}
		return defElement;
	}
}

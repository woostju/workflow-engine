package com.github.bryx.workflow.domain.process.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * the base element in process inst
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessActivityObject {
	public enum ProcessInstActivityType {
		USER_TASK,
		START_NODE,
		END_NODE,
		EXCLUSIVE_GATEWAY,
		PARALLEL_GATEWAY,
		SEQUENCE_FLOW
	}
	private ProcessInstActivityType type;
	private String id;
	private String definitionId;
	private String name;
	private String documentation;
	private Date startTime;
	private Date endTime;
	private Date executionTime;
	private String executionId;
	private String processId;
	private String processDefinitionId;
}

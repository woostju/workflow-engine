package com.github.bryx.workflow.domain.process.buildtime;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDef extends ProcessDefElement {

	List<String> candidateUserIds;
	List<String> candidateGroupIds;
	
	private List<TaskTimer> timers;

}

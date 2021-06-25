package com.github.bryx.workflow.domain.process.buildtime;

import java.util.Date;

import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import lombok.Data;

@Data
public class TaskTimer {
	private String jobId;
	private String definitionId;
	private Date triggerTime;
	private TaskObject task;
}

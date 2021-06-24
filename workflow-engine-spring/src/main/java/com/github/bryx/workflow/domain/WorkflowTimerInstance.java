package com.github.bryx.workflow.domain;

import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import lombok.Data;

import java.util.Date;

@Data
public class WorkflowTimerInstance {

    private String definitionId;

    private Date triggerTime;

    private TaskObject task;

}

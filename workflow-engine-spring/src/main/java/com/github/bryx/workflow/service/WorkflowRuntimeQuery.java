package com.github.bryx.workflow.service;

import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
import com.github.bryx.workflow.domain.WorkflowTimerJob;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
public interface WorkflowRuntimeQuery {

    public WorkflowInstance getWorkflowInstanceById(String workflowInstanceId);

    public WorkflowTaskInstance getWorkflowTaskInstanceById(String workflowInstanceTaskId);

    public List<WorkflowTaskInstance> getWorkflowTaskInstanceByIds(List<String> workflowInstanceTaskIds);

    public List<WorkflowTaskInstance> getWorkflowTaskInstances(String workflowInstanceId);

    public List<WorkflowTimerJob> getWorkflowTimerJobsOnTask(String workflowInstanceTaskId);

    public List<WorkflowTimerJob> getWorkflowTimerJobsOnWorkflow(String workflowInstanceId);
}

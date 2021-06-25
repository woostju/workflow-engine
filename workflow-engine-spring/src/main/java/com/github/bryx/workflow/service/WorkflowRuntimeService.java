package com.github.bryx.workflow.service;

import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.dto.runtime.*;

import java.util.List;
import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
public interface WorkflowRuntimeService {

    public String start(String workflowDefId, Map<String, Object> formData, String executorId);

    public List<String> rejectBack(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId);

    public List<String> rejectBackAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId);

    public void claim(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId);

    public void transfer(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId);

    public void modify(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId);

    public List<String> submit(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId);

    public List<String> submitAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId);

    public void close(String workflowInstanceId, String executorId);

    public void delete(String workflowInstanceId, String executorId);

    public String createWorkflowInstance(CreateWorkflowInstanceDto dto);

    public void updateWorkflowInstance(UpdateWorkflowInstanceDto dto);

    public WorkflowTaskInstance createWorkflowTaskInstance(CreateWorkflowTaskInstanceDto dto);

    public String createWorkflowTimerJob(CreateWorkflowTimerJobDto createWorkflowTimerJobDto);

    public void updateWorkflowTaskInstance(UpdateWorkflowTaskInstanceDto dto);

    public WorkflowRuntimeQuery query();
}

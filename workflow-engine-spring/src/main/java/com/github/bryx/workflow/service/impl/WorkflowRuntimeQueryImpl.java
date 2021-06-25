package com.github.bryx.workflow.service.impl;

import com.github.bryx.workflow.domain.WorkflowTimerJob;
import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.service.dao.WorkflowTimerJobDao;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.service.dao.WorkflowInstanceDao;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
import com.github.bryx.workflow.service.WorkflowRuntimeQuery;
import com.github.bryx.workflow.service.dao.WorkflowTaskInstanceDao;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Service
public class WorkflowRuntimeQueryImpl implements WorkflowRuntimeQuery {

    @Autowired
    WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    WorkflowTaskInstanceDao workflowTaskInstanceDao;

    @Autowired
    ProcessService processService;

    @Autowired
    WorkflowTimerJobDao workflowTimerJobDao;

    @Override
    public WorkflowInstance getWorkflowInstanceById(String id) {
        return workflowInstanceDao.getById(id);
    }

    @Override
    public WorkflowTaskInstance getWorkflowTaskInstanceById(String taskId) {
        WorkflowTaskInstance taskInstance = workflowTaskInstanceDao.getById(taskId);
        return taskInstance;
    }

    @Override
    public List<WorkflowTaskInstance> getWorkflowTaskInstanceByIds(List<String> workflowInstanceTaskIds) {
        return workflowTaskInstanceDao.lambdaQuery().in(WorkflowTaskInstance::getId, workflowInstanceTaskIds).list();
    }

    @Override
    public List<WorkflowTaskInstance> getWorkflowTaskInstances(String workflowInstanceId) {
        List<WorkflowTaskInstance> taskInstances = workflowTaskInstanceDao.lambdaQuery().eq(WorkflowTaskInstance::getWorkflowInstanceId, workflowInstanceId)
                .eq(WorkflowTaskInstance::getStatus, WorkflowTaskInstance.WorkflowTaskInstanceStatus.ONGOING).list();
        if (CollectionsUtil.isNotEmpty(taskInstances)){
            Map<String, TaskObject> idToTaskObject = processService.getTasks(taskInstances.stream().map(WorkflowTaskInstance::getProcessTaskId).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(TaskObject::getId, Functions.identity()));
            taskInstances.forEach(inst->{
                inst.setAssigneeUserIds(idToTaskObject.get(inst.getProcessTaskId()).getAssignedUserIds());
                inst.setAssigneeGroupIds(idToTaskObject.get(inst.getProcessTaskId()).getAssignedGroupIds());
            });
        }
        return taskInstances;
    }

    @Override
    public List<WorkflowTimerJob> getWorkflowTimerJobsOnTask(String workflowInstanceTaskId) {
        List<WorkflowTimerJob> workflowTimerJobs = workflowTimerJobDao.lambdaQuery().eq(WorkflowTimerJob::getWorkflowTaskInstanceId, workflowInstanceTaskId).list();
        return workflowTimerJobs;
    }

    @Override
    public List<WorkflowTimerJob> getWorkflowTimerJobsOnWorkflow(String workflowInstanceId) {
        List<WorkflowTimerJob> workflowTimerJobs = workflowTimerJobDao.lambdaQuery().eq(WorkflowTimerJob::getWorkflowInstanceId, workflowInstanceId).list();
        return workflowTimerJobs;
    }
}

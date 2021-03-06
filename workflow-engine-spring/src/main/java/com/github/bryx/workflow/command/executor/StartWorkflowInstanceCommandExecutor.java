package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.domain.*;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.StartWorkflowInstanceCommand;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowInstanceDto;
import com.github.bryx.workflow.event.WorkflowStartEvent;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StartWorkflowInstanceCommandExecutor extends CommandExecutor<String, StartWorkflowInstanceCommand> {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ProcessService processService;

    @Override
    @Transactional
    public String run() {
        StartWorkflowInstanceCommand command = this.getCommand();
        WorkflowDefRev latestEnabledWorkflowDefRev = workflowBuildTimeService.query().getLatestEnabledWorkflowDefRev(command.getWorkflowDefId());
        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(latestEnabledWorkflowDefRev.getDefId());
        workflowDef.setRev(latestEnabledWorkflowDefRev);
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());
        final Map<String, WorkflowDefProcessConfig.UserTaskConfig> userTasksConfig = workflowDef.getRev().getProcessConfig().getUserTasks();
        // 1. ??????process??????
        // 2. ??????workflowInstanceInterceptor?????????????????????
        WorkflowInstance workflowInstance = WorkflowInstance.builder().creatorId(command.getExecutorId()).formData(command.getFormData()).build();
        String processId = processService.startProcess(workflowDef.getRev().getProcessDefId(),
                command.getExecutorId(), command.getFormData(), (tasks, processObject) -> {
                    Map<String, List<TaskObjectAssignee>> taskObjectAssigneesMap = Maps.newHashMap();
                    tasks.forEach(taskObject -> {
                        userTasksConfig.get(taskObject.getDefinitionId()).setTaskDefId(taskObject.getDefinitionId());
                        taskObjectAssigneesMap.put(taskObject.getId(), Optional.ofNullable(workflowInstanceInterceptor.userTaskAssign(workflowInstance, taskObject, workflowDef))
                                .orElse(Lists.newArrayList()));
                    });
                    return taskObjectAssigneesMap;
                });
        // ??????????????????????????????
        String workflowInstanceId = workflowRuntimeService.createWorkflowInstance(CreateWorkflowInstanceDto.builder()
                .creatorId(command.getExecutorId())
                .defId(workflowDef.getRev().getDefId())
                .defRevId(workflowDef.getRev().getId())
                .formData(command.getFormData())
                .processId(processId).build());
        // ??????????????????
        List<TaskObject> tasks = processService.getTasks(processId);
        List<WorkflowTaskInstance> workflowTaskInstances = this.getCommandExecutorHelper().createWorkflowTaskInstances(workflowInstanceId, workflowDef.getRev(), tasks);

        // ?????????????????????timer?????????
        this.getCommandExecutorHelper().createWorkflowTimerJobsIfNecessary(workflowInstance, workflowTaskInstances, latestEnabledWorkflowDefRev.getProcessConfig(), workflowInstanceInterceptor);

        // ??????WorkflowStartEvent
        WorkflowStartEvent event = WorkflowStartEvent.builder()
                .executorId(command.getExecutorId())
                .workflowInstanceId(workflowInstanceId)
                .newWorkflowTaskInstanceIds(workflowTaskInstances.stream().map(WorkflowTaskInstance::getId).collect(Collectors.toList()))
                .build();
        workflowInstanceInterceptor.handleEvent(event);

        return workflowInstanceId;
    }
}

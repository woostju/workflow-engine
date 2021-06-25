package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.RejectBackUserTaskCommand;
import com.github.bryx.workflow.domain.*;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowInstanceDto;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.event.UserTaskRejectBackEvent;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class RejectBackUserTaskCommandExecutor extends CommandExecutor<List<String>, RejectBackUserTaskCommand> {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ProcessService processService;

    @Override
    public List<String> run() {
        RejectBackUserTaskCommand command = this.getCommand();
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());
        WorkflowTaskInstance workflowTaskInstance = workflowRuntimeService.query().getWorkflowTaskInstanceById(command.getWorkflowTaskInstanceId());
        WorkflowDefRev workflowDefRev = workflowBuildTimeService.query().getWorkflowDefRevById(workflowInstance.getDefRevId());
        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowDefRev.getDefId());
        workflowDef.setRev(workflowDefRev);

        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());

        // 更新任务的状态
        UpdateWorkflowTaskInstanceDto updateTaskDto = UpdateWorkflowTaskInstanceDto.builder()
                .executorId(command.getExecutorId())
                .status(WorkflowTaskInstance.WorkflowTaskInstanceStatus.COMPLETED)
                .id(workflowTaskInstance.getId())
                .formData(command.getFormData())
                .build();
        workflowRuntimeService.updateWorkflowTaskInstance(updateTaskDto);

        // 分配用户，驳回并指定受理人
        final Map<String, WorkflowDefProcessConfig.UserTaskConfig> userTasksConfig = workflowDefRev.getProcessConfig().getUserTasks();
        List<String> newTaskIds = processService.rejectBack(workflowInstance.getProcessId(), workflowTaskInstance.getProcessTaskId(), command.getExecutorId(),command.getFormData(), (tasks, processObject) -> {
            Map<String, List<TaskObjectAssignee>> taskObjectAssigneesMap = Maps.newHashMap();
            tasks.forEach(taskObject -> {
                userTasksConfig.get(taskObject.getDefinitionId()).setTaskDefId(taskObject.getDefinitionId());
                List<TaskObjectAssignee> taskObjectAssignees = Optional.ofNullable(TaskObjectAssignee.createTaskObjectAssignees(command.getAssigneeUserIds(), command.getAssigneeGroupIds()))
                        .orElse(workflowInstanceInterceptor.userTaskAssign(workflowInstance, taskObject, workflowDef));
                taskObjectAssigneesMap.put(taskObject.getId(), Optional.ofNullable(taskObjectAssignees).orElse(Lists.newArrayList()));
            });
            return taskObjectAssigneesMap;
        });

        // 更新流程实例的表单数据
        UpdateWorkflowInstanceDto updateInstanceDto = UpdateWorkflowInstanceDto.builder()
                .id(workflowInstance.getId())
                .executorId(command.getExecutorId())
                .formData(command.getFormData())
                .build();
        workflowRuntimeService.updateWorkflowInstance(updateInstanceDto);

        // 数据库记录任务，并发送用户提交事件
        List<TaskObject> tasks = processService.getTasks(newTaskIds);
        List<WorkflowTaskInstance> newWorkflowTaskInstances = this.getCommandExecutorHelper()
                .createWorkflowTaskInstances(workflowInstance.getId(), workflowDefRev, tasks);
        // 创建timer
        this.getCommandExecutorHelper().createWorkflowTimerJobsIfNecessary(workflowInstance, newWorkflowTaskInstances, workflowDefRev.getProcessConfig(), workflowInstanceInterceptor);
        // 发送 UserTaskRejectBackEvent
        UserTaskRejectBackEvent event =UserTaskRejectBackEvent.builder()
                .executorId(command.getExecutorId())
                .workflowInstanceId(command.getWorkflowInstanceId())
                .workflowTaskInstanceId(command.getWorkflowTaskInstanceId())
                .newWorkflowTaskInstanceIds(newWorkflowTaskInstances.stream().map(WorkflowTaskInstance::getId).collect(Collectors.toList()))
                .build();
        workflowInstanceInterceptor.handleEvent(event);

        return newWorkflowTaskInstances.stream().map(WorkflowTaskInstance::getId).collect(Collectors.toList());
    }
}

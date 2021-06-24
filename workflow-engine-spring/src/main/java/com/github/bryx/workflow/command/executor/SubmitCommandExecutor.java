package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.SubmitCommand;
import com.github.bryx.workflow.domain.*;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowInstanceDto;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.event.UserTaskSubmitEvent;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubmitCommandExecutor extends BaseCommandExecutor<List<String>, SubmitCommand> {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ProcessService processService;

    @Override
    @Transactional
    public List<String> run() {
        SubmitCommand command = this.getCommand();
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());
        WorkflowTaskInstance workflowTaskInstance = workflowRuntimeService.query().getWorkflowTaskInstanceById(command.getWorkflowTaskInstanceId());
        Validate.notNull(workflowTaskInstance, "task not found");
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

        // 分配用户，提交并指定受理人 ｜ 提交
        final Map<String, WorkflowDefProcessConfig.UserTaskConfig> userTasksConfig = workflowDefRev.getProcessConfig().getUserTasks();
        List<String> newTaskIds = processService.execute(workflowInstance.getProcessId(), workflowTaskInstance.getProcessTaskId(), command.getExecutorId(), command.getFormData(), (tasks, processObject) -> {
            Map<String, List<TaskObjectAssignee>> taskObjectAssigneesMap = Maps.newHashMap();
            tasks.forEach(taskObject -> {
                Optional.ofNullable(userTasksConfig.get(taskObject.getDefinitionId()))
                        .orElseThrow(()->new WorkflowRuntimeException(String.format("please config user task for %s", taskObject.getDefinitionId())))
                        .setTaskDefId(taskObject.getDefinitionId());
                List<TaskObjectAssignee> taskObjectAssignees = Optional.ofNullable(TaskObjectAssignee.createTaskObjectAssignees(command.getAssigneeIds(), command.getAssigneeGroupIds()))
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

        List<String> newWorkflowTaskInstanceIds = Lists.newArrayList();
        if (CollectionsUtil.empty(newTaskIds)){
            // 可以检查是不是流程已经结束了
            List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstance.getId());
            if (CollectionsUtil.empty(workflowTaskInstances)){
                updateInstanceDto.setStatus(WorkflowInstance.WorkflowInstanceStatus.COMPLETED);
            }
        }else{
            // 数据库记录任务
            List<TaskObject> tasks = processService.getTasks(newTaskIds);
            newWorkflowTaskInstanceIds = this.getCommandExecutorHelper().createWorkflowTaskInstances(workflowInstance.getId(), workflowDefRev, tasks);
            this.getCommandExecutorHelper().createWorkflowTimerInstancesIfNecessary(tasks, workflowInstance, workflowDefRev.getProcessConfig(), workflowInstanceInterceptor);
        }
        workflowRuntimeService.updateWorkflowInstance(updateInstanceDto);
        // 发送UserTaskSubmitEvent
        UserTaskSubmitEvent event =UserTaskSubmitEvent.builder()
                .executorId(command.getExecutorId())
                .workflowInstanceId(command.getWorkflowInstanceId())
                .workflowTaskInstanceId(command.getWorkflowTaskInstanceId())
                .newWorkflowTaskInstanceIds(newWorkflowTaskInstanceIds)
                .build();
        workflowInstanceInterceptor.handleEvent(event);
        return newWorkflowTaskInstanceIds;
    }
}

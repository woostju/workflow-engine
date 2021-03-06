package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowInstanceDto;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.event.UserTaskTransferEvent;
import com.github.bryx.workflow.command.TransferUserTaskCommand;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransferUserTaskCommandExecutor extends CommandExecutor<Void, TransferUserTaskCommand> {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ProcessService processService;

    @SneakyThrows
    @Override
    @Transactional
    public Void run() {
        TransferUserTaskCommand command = this.getCommand();
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());
        WorkflowTaskInstance workflowTaskInstance = workflowRuntimeService.query().getWorkflowTaskInstanceById(command.getWorkflowTaskInstanceId());
        WorkflowDefRev workflowDefRev = workflowBuildTimeService.query().getWorkflowDefRevById(workflowInstance.getDefRevId());
        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowDefRev.getDefId());
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());

        processService.updateAssignees(workflowTaskInstance.getProcessTaskId(), TaskObjectAssignee.createTaskObjectAssignees(command.getAssigneeUserIds(), command.getAssigneeGroupIds()));
        UpdateWorkflowTaskInstanceDto updateTaskDto = UpdateWorkflowTaskInstanceDto.builder()
                .executorId(command.getExecutorId())
                .id(workflowTaskInstance.getId())
                .build();
        workflowRuntimeService.updateWorkflowTaskInstance(updateTaskDto);

        if (command.getFormData() != null){
            UpdateWorkflowInstanceDto updateInstanceDto = UpdateWorkflowInstanceDto.builder()
                    .id(workflowInstance.getId())
                    .executorId(command.getExecutorId())
                    .formData(command.getFormData())
                    .build();
            workflowInstance.setFormData(command.getFormData());
            workflowRuntimeService.updateWorkflowInstance(updateInstanceDto);
        }
        // ??????????????????
        UserTaskTransferEvent userTaskTransferEvent = UserTaskTransferEvent.builder()
                .sourceAssigneeUserIds(command.getAssigneeUserIds())
                .sourceAssigneeGroupIds(command.getAssigneeGroupIds())
                .targetAssigneeUserIds(workflowTaskInstance.getAssigneeUserIds())
                .targetAssigneeGroupIds(workflowTaskInstance.getAssigneeGroupIds())
                .workflowInstanceId(command.getWorkflowInstanceId())
                .workflowTaskInstanceId(command.getWorkflowTaskInstanceId())
                .executorId(command.getExecutorId())
                .build();
        workflowInstanceInterceptor.handleEvent(userTaskTransferEvent);
        return null;
    }
}

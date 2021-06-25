package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowInstanceDto;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.event.UserTaskModifyEvent;
import com.github.bryx.workflow.command.ModifyUserTaskCommand;
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
public class ModifyUserTaskCommandExecutor extends CommandExecutor<Void, ModifyUserTaskCommand> {

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
        ModifyUserTaskCommand command = this.getCommand();

        UpdateWorkflowTaskInstanceDto updateTaskDto = UpdateWorkflowTaskInstanceDto.builder()
                .executorId(command.getExecutorId())
                .id(command.getWorkflowTaskInstanceId())
                .build();
        workflowRuntimeService.updateWorkflowTaskInstance(updateTaskDto);

        UpdateWorkflowInstanceDto updateInstanceDto = UpdateWorkflowInstanceDto.builder()
                .id(command.getWorkflowInstanceId())
                .executorId(command.getExecutorId())
                .formData(command.getFormData())
                .build();
        workflowRuntimeService.updateWorkflowInstance(updateInstanceDto);

        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());
        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowInstance.getDefId());
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());
        UserTaskModifyEvent event = UserTaskModifyEvent.builder()
                .workflowInstanceId(command.getWorkflowInstanceId())
                .workflowTaskInstanceId(command.getWorkflowTaskInstanceId())
                .executorId(command.getExecutorId())
                .build();
        workflowInstanceInterceptor.handleEvent(event);
        return null;
    }
}

package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowInstanceDto;
import com.github.bryx.workflow.dto.runtime.UpdateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.event.WorkflowCloseEvent;
import com.github.bryx.workflow.command.CloseCommand;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CloseCommandExecutor extends BaseCommandExecutor<Void, CloseCommand> {

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
        CloseCommand command = this.getCommand();
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());

        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(command.getWorkflowInstanceId());
        workflowTaskInstances.forEach(taskInst->{
            UpdateWorkflowTaskInstanceDto updateTaskDto = UpdateWorkflowTaskInstanceDto.builder()
                    .id(taskInst.getId())
                    .status(WorkflowTaskInstance.WorkflowTaskInstanceStatus.INTERRUPTED)
                    .build();
            workflowRuntimeService.updateWorkflowTaskInstance(updateTaskDto);
            taskInst.setStatus(WorkflowTaskInstance.WorkflowTaskInstanceStatus.INTERRUPTED);
        });
        processService.closeProcess(workflowInstance.getProcessId(), command.getExecutorId());

        UpdateWorkflowInstanceDto updateInstanceDto = UpdateWorkflowInstanceDto.builder()
                .id(workflowInstance.getId())
                .executorId(command.getExecutorId())
                .status(WorkflowInstance.WorkflowInstanceStatus.CLOSED)
                .build();
        workflowRuntimeService.updateWorkflowInstance(updateInstanceDto);

        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowInstance.getDefId());
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());
        WorkflowCloseEvent event = WorkflowCloseEvent.builder()
                .workflowInstance(workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId()))
                .executorId(command.getExecutorId())
                .interruptedTaskInstances(workflowTaskInstances)
                .build();
        workflowInstanceInterceptor.handleEvent(event);
        return null;
    }
}

package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.ClaimUserTaskCommand;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.event.UserTaskClaimEvent;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.google.common.collect.Lists;
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
public class ClaimUserTaskCommandExecutor extends CommandExecutor<Void, ClaimUserTaskCommand> {

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
        ClaimUserTaskCommand command = this.getCommand();
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(command.getWorkflowInstanceId());
        WorkflowTaskInstance workflowTaskInstance = workflowRuntimeService.query().getWorkflowTaskInstanceById(command.getWorkflowTaskInstanceId());
        List<String> sourceAssigneeUserIds = workflowTaskInstance.getAssigneeUserIds();
        List<String> sourceAssigneeGroupIds = workflowTaskInstance.getAssigneeGroupIds();

        processService.claimTask(workflowTaskInstance.getProcessTaskId(), command.getExecutorId());
        workflowTaskInstance.setAssigneeUserIds(Lists.newArrayList(command.getExecutorId()));
        workflowTaskInstance.setAssigneeGroupIds(Lists.newArrayList());
        // send event to interceptor
        UserTaskClaimEvent userTaskClaimEvent = UserTaskClaimEvent.builder()
                .executorId(command.getExecutorId())
                .sourceAssigneeUserIds(sourceAssigneeUserIds)
                .sourceAssigneeGroupIds(sourceAssigneeGroupIds)
                .workflowInstanceId(command.getWorkflowInstanceId())
                .workflowTaskInstanceId(command.getWorkflowTaskInstanceId())
                .build();

        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowInstance.getDefId());
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());
        workflowInstanceInterceptor.handleEvent(userTaskClaimEvent);
        return null;
    }
}

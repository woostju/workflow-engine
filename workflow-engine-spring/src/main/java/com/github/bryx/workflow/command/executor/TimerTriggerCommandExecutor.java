package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.event.TimerTriggerEvent;
import com.github.bryx.workflow.command.TimerTriggerCommand;
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
public class TimerTriggerCommandExecutor extends CommandExecutor<Void, TimerTriggerCommand> {

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
        TimerTriggerCommand command = this.getCommand();

        TimerTriggerEvent event = TimerTriggerEvent.builder()
                .workflowInstance(command.getWorkflowInstance())
                .workflowTaskInstance(command.getWorkflowTaskInstance())
                .timerInstance(command.getWorkflowTimerInstance())
                .build();

        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefById(command.getWorkflowInstance().getDefId());
        WorkflowInstanceInterceptor workflowInstanceInterceptor = this.getWorkflowInstanceInterceptor(workflowDef.getProcessDefType());
        workflowInstanceInterceptor.handleEvent(event);
        return null;
    }
}

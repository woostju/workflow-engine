package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.TimerTriggerCommandExecutor;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CommandConfiguration(executor = TimerTriggerCommandExecutor.class)
public class TimerTriggerCommand extends WorkflowCommand {
    private WorkflowTaskInstance workflowTaskInstance;
    private WorkflowInstance workflowInstance;
    private WorkflowTimerInstance workflowTimerInstance;
}

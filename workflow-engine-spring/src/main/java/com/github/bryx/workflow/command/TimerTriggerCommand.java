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
public class TimerTriggerCommand extends BaseCommand {
    /**
     * 流程任务
     */
    private WorkflowTaskInstance workflowTaskInstance;
    /**
     * 流程实例
     */
    private WorkflowInstance workflowInstance;
    /**
     * Timer实例
     */
    private WorkflowTimerInstance workflowTimerInstance;
}

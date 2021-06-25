package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.CloseWorkflowInstanceCommandExecutor;
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
@CommandConfiguration(executor = CloseWorkflowInstanceCommandExecutor.class)
public class CloseWorkflowInstanceCommand extends BaseCommand {
    /**
     * 流程实例id
     */
    private String workflowInstanceId;
    /**
     * 执行人id
     */
    private String executorId;
}

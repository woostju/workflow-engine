package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.ClaimCommandExecutor;
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
@CommandConfiguration(executor = ClaimCommandExecutor.class)
public class ClaimCommand extends WorkflowCommand {
    private String workflowTaskInstanceId;
    private String workflowInstanceId;
    private String executorId;
}

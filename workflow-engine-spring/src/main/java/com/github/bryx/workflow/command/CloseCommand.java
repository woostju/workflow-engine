package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.CloseCommandExecutor;
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
@CommandConfiguration(executor = CloseCommandExecutor.class)
public class CloseCommand extends WorkflowCommand {
    private String workflowInstanceId;
    private String executorId;
}

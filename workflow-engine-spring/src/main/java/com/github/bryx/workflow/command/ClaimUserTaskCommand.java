package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.ClaimUserTaskCommandExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 认领任务
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CommandConfiguration(executor = ClaimUserTaskCommandExecutor.class)
public class ClaimUserTaskCommand extends BaseCommand {
    /**
     * 任务实例id
     */
    private String workflowTaskInstanceId;
    /**
     * 流程实例id
     */
    private String workflowInstanceId;
    /**
     * 认领人
     */
    private String executorId;
}

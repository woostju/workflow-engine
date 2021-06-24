package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.TransferCommandExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CommandConfiguration(executor = TransferCommandExecutor.class)
public class TransferCommand extends WorkflowCommand {
    private String workflowTaskInstanceId;
    private String workflowInstanceId;
    private String executorId;
    private Map<String, Object> formData;
    private List<String> assigneeIds;
    private List<String> assigneeGroupIds;
}

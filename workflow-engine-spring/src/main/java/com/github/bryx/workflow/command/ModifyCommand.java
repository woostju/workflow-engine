package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.ModifyCommandExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CommandConfiguration(executor = ModifyCommandExecutor.class)
public class ModifyCommand extends WorkflowCommand {
    private String workflowTaskInstanceId;
    private String workflowInstanceId;
    private String executorId;
    private Map<String, Object> formData;
}

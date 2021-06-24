package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.StartCommandExecutor;
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
@CommandConfiguration(executor = StartCommandExecutor.class)
public class StartCommand<T> extends WorkflowCommand<T> {
    private String workflowDefId;
    private Map<String, Object> formData;
    private String executorId;
}

package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.StartWorkflowInstanceCommandExecutor;
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
@CommandConfiguration(executor = StartWorkflowInstanceCommandExecutor.class)
public class StartWorkflowInstanceCommand<T> extends BaseCommand<T> {
    /**
     * 流程定义id
     */
    private String workflowDefId;
    /**
     * 表单数据
     */
    private Map<String, Object> formData;
    /**
     * 执行人id
     */
    private String executorId;
}

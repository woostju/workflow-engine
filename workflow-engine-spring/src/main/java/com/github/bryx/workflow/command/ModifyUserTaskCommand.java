package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.ModifyUserTaskCommandExecutor;
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
@CommandConfiguration(executor = ModifyUserTaskCommandExecutor.class)
public class ModifyUserTaskCommand extends BaseCommand {
    /**
     * 流程任务实例id
     */
    private String workflowTaskInstanceId;
    /**
     * 流程实例id
     */
    private String workflowInstanceId;
    /**
     * 执行人id
     */
    private String executorId;
    /**
     * 表单数据
     */
    private Map<String, Object> formData;
}

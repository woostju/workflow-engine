package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.RejectBackUserTaskCommandExecutor;
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
@CommandConfiguration(executor = RejectBackUserTaskCommandExecutor.class)
public class RejectBackUserTaskCommand extends BaseCommand {
    /**
     * 流程任务id
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
    /**
     * 受理人ids
     */
    private List<String> assigneeUserIds;
    /**
     * 受理组ids
     */
    private List<String> assigneeGroupIds;
}

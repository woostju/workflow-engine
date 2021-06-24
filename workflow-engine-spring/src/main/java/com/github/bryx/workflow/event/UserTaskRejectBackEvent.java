package com.github.bryx.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskRejectBackEvent implements WorkflowEvent{

    private String workflowInstanceId;
    private String workflowTaskInstanceId;
    private List<String> newWorkflowTaskInstanceIds;
    private String executorId;

    @Override
    public Type getType() {
        return Type.USER_TASK_REJECT_BACK;
    }
}

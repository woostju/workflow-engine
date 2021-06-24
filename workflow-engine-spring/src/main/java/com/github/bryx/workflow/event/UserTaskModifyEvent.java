package com.github.bryx.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskModifyEvent implements WorkflowEvent{

    private String workflowInstanceId;
    private String workflowTaskInstanceId;
    private String executorId;

    @Override
    public Type getType() {
        return Type.USER_TASK_MODIFY;
    }
}

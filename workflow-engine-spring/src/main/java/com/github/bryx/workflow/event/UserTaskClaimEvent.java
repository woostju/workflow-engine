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
public class UserTaskClaimEvent implements WorkflowEvent{

    private String workflowInstanceId;
    private String workflowTaskInstanceId;
    private String executorId;
    private List<String> sourceAssigneeUserIds;
    private List<String> sourceAssigneeGroupIds;

    @Override
    public Type getType() {
        return Type.USER_TASK_CLAIM;
    }
}

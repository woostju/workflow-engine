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
public class WorkflowStartEvent implements WorkflowEvent{

    private String workflowInstanceId;
    private List<String> newWorkflowTaskInstanceIds;
    private String executorId;

    @Override
    public Type getType() {
        return Type.WORKFLOW_START;
    }

}

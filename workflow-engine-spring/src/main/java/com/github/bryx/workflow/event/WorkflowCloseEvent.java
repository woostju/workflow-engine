package com.github.bryx.workflow.event;

import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
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
public class WorkflowCloseEvent implements WorkflowEvent{

    private WorkflowInstance workflowInstance;
    private List<WorkflowTaskInstance> interruptedTaskInstances;
    private String executorId;

    @Override
    public Type getType() {
        return Type.WORKFLOW_CLOSE;
    }

}

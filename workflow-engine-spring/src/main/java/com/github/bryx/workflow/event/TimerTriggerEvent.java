package com.github.bryx.workflow.event;

import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
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
public class TimerTriggerEvent implements WorkflowEvent{

    private WorkflowInstance workflowInstance;
    private WorkflowTaskInstance workflowTaskInstance;
    private WorkflowTimerInstance timerInstance;

    @Override
    public Type getType() {
        return Type.TIMER_TRIGGER;
    }

    @Override
    public String getExecutorId() {
        return null;
    }


}

package com.github.bryx.workflow.event;

/**
 * @Author jameswu
 * @Date 2021/5/25
 **/
public interface WorkflowEvent {
    public enum Type{
        USER_TASK_CLAIM,
        USER_TASK_SUBMIT,
        USER_TASK_MODIFY,
        USER_TASK_REJECT_BACK,
        USER_TASK_TRANSFER,
        WORKFLOW_CLOSE,
        WORKFLOW_START,
        TIMER_TRIGGER
    }

    Type getType();
    
    public String getExecutorId();
}

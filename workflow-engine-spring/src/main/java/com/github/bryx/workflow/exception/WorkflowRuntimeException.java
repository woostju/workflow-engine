package com.github.bryx.workflow.exception;

/**
 * @Author jameswu
 * @Date 2021/5/19
 **/
public class WorkflowRuntimeException extends RuntimeException{
    public WorkflowRuntimeException(String message){
        super(message);
    }
}

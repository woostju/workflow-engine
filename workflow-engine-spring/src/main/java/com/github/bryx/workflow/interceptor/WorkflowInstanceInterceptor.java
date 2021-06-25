package com.github.bryx.workflow.interceptor;

import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.event.WorkflowEvent;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
public interface WorkflowInstanceInterceptor {

    /**
     * 处理事件
     * @param event
     */
    public void handleEvent(WorkflowEvent event);

    /**
     *
     * 自定义任务的受理人设置，当用户不指定受理人
     * @param workflowInstance
     * @param taskObject
     * @param workflowDef
     * @return
     */
    public List<TaskObjectAssignee> userTaskAssign(WorkflowInstance workflowInstance, TaskObject taskObject, WorkflowDef workflowDef);

    /**
     * timerconfig配置了extension手动创建timer
     * @param workflowInstance
     * @param timerConfig
     */
    public void createTimerJobWhenExtensionConfigured(WorkflowInstance workflowInstance, WorkflowTaskInstance taskInstance, WorkflowDefProcessConfig.TimerConfig timerConfig);
}

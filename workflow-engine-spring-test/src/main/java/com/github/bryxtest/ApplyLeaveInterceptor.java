package com.github.bryxtest;

import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.event.WorkflowEvent;
import com.github.bryx.workflow.interceptor.Interceptor;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
@Component
@Interceptor(processKey = "unittest-applyleave")
public class ApplyLeaveInterceptor implements WorkflowInstanceInterceptor {


    @Override
    public void handleEvent(WorkflowEvent event) {
        System.out.println(event);
    }

    @Override
    public  List<TaskObjectAssignee> userTaskAssign(WorkflowInstance workflowInstance, TaskObject taskObject, WorkflowDef workflowDef) {
        WorkflowDefProcessConfig.UserTaskConfig userTaskConfig = workflowDef.getRev().getProcessConfig().getUserTasks().get(taskObject.getDefinitionId());
        switch (userTaskConfig.getTaskDefId()){
            case "user_task3":
                return TaskObjectAssignee.createTaskObjectAssignees(userTaskConfig.getAssigneeUserIds(), userTaskConfig.getAssigneeGroupIds());
            case "user_task1":
                return TaskObjectAssignee.createTaskObjectAssignees(Lists.newArrayList(workflowInstance.getCreatorId()), null);
            case "user_task2":
                return TaskObjectAssignee.createTaskObjectAssignees(userTaskConfig.getAssigneeUserIds(), userTaskConfig.getAssigneeGroupIds());
        }
        return null;
    }

    @Override
    public void createTimerJobWhenExtensionConfigured(WorkflowInstance workflowInstance, WorkflowTaskInstance taskInstance, WorkflowDefProcessConfig.TimerConfig timerConfig) {

    }

}

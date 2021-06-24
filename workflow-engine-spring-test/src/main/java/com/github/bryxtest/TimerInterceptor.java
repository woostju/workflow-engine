package com.github.bryxtest;

import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowTimerInstanceDto;
import com.github.bryx.workflow.event.WorkflowEvent;
import com.github.bryx.workflow.interceptor.Interceptor;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
@Component
@Interceptor(processKey = "unittest-timer")
public class TimerInterceptor implements WorkflowInstanceInterceptor {

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Override
    public void handleEvent(WorkflowEvent event) {
        System.out.println(event);
    }

    @Override
    public  List<TaskObjectAssignee> userTaskAssign(WorkflowInstance workflowInstance, TaskObject taskObject, WorkflowDef workflowDef) {
        WorkflowDefProcessConfig.UserTaskConfig userTaskConfig = workflowDef.getRev().getProcessConfig().getUserTasks().get(taskObject.getDefinitionId());
        return TaskObjectAssignee.createTaskObjectAssignees(userTaskConfig.getAssigneeUserIds(), userTaskConfig.getAssigneeGroupIds());
    }

    @Override
    public void createTimerWhenExtensionConfigured(WorkflowInstance workflowInstance, TaskObject taskObject, WorkflowDefProcessConfig.TimerConfig timerConfig) {
            if (timerConfig.getTimerDefinitionId().equals("timer2")){
                workflowRuntimeService.createWorkflowTimerInstance(CreateWorkflowTimerInstanceDto.builder()
                        .processTaskId(taskObject.getId())
                        .timerDefinitionId(timerConfig.getTimerDefinitionId())
                        .duration(30)
                        .timeUnit(TimeUnit.SECONDS)
                        .build());
                workflowRuntimeService.createWorkflowTimerInstance(CreateWorkflowTimerInstanceDto.builder()
                        .processTaskId(taskObject.getId())
                        .timerDefinitionId(timerConfig.getTimerDefinitionId())
                        .duration(60)
                        .timeUnit(TimeUnit.SECONDS)
                        .build());
            }
    }

}

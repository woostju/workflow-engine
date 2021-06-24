package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowTimerInstanceDto;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/15
 **/
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CommandExecutorHelper {

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ProcessService processService;

    public List<String> createWorkflowTaskInstances(String workflowInstanceId, WorkflowDefRev workflowDefRev, List<TaskObject> tasks){
        // 创建workflow task instances
        List<String> workflowTaskInstanceIds = Lists.newArrayList();
        tasks.forEach(task->{
            String workflowTaskInstanceId = workflowRuntimeService.createWorkflowTaskInstance(CreateWorkflowTaskInstanceDto.builder()
                    .name(task.getName())
                    .processTaskDefId(task.getDefinitionId())
                    .processTaskId(task.getId())
                    .workflowInstanceId(workflowInstanceId).build());
            workflowTaskInstanceIds.add(workflowTaskInstanceId);
        });
        return workflowTaskInstanceIds;
    }

    /**
     * 检测taskObjects上是否有timer的配置，如果配置了extension则要求interceptor创建timer,如果有配置了cron和duration则创建
     * @param tasks
     * @param workflowInstance
     * @param processConfig
     * @param workflowInstanceInterceptor
     */
    public void createWorkflowTimerInstancesIfNecessary(List<TaskObject> tasks, WorkflowInstance workflowInstance, WorkflowDefProcessConfig processConfig, WorkflowInstanceInterceptor workflowInstanceInterceptor){
        tasks.forEach(taskObject->{
            Map<String, WorkflowDefProcessConfig.TimerConfig> timerConfigs = processConfig.getUserTasks().get(taskObject.getDefinitionId()).getTimer();
            if (CollectionsUtil.mapNotEmpty(timerConfigs)){
                timerConfigs.forEach((timerDefinitionId, timerConfig)->{
                    if(StringUtil.isNotEmpty(timerConfig.getExtension())){
                        timerConfig.setTimerDefinitionId(timerDefinitionId);
                        // interceptor负责创建timer
                       workflowInstanceInterceptor.createTimerWhenExtensionConfigured(workflowInstance, taskObject, timerConfig);
                    }else{
                        CreateWorkflowTimerInstanceDto createWorkflowTimerInstanceDto = new CreateWorkflowTimerInstanceDto();
                        createWorkflowTimerInstanceDto.setProcessTaskId(taskObject.getId());
                        createWorkflowTimerInstanceDto.setTimerDefinitionId(timerDefinitionId);
                        if (StringUtil.isNotEmpty(timerConfig.getCron())){
                            // create cron timer
                            createWorkflowTimerInstanceDto.setCron(timerConfig.getCron());
                        }else if(timerConfig.getDuration()!=null && timerConfig.getTimeUnit()!=null){
                            // create duration timer
                            createWorkflowTimerInstanceDto.setDuration(timerConfig.getDuration());
                            createWorkflowTimerInstanceDto.setTimeUnit(timerConfig.getTimeUnit());
                        }
                        workflowRuntimeService.createWorkflowTimerInstance(createWorkflowTimerInstanceDto);
                    }
                });
            }
        });
    }

}

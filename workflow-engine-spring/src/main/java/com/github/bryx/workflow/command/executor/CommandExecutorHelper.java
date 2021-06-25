package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.config.WorkflowEngineProperties;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowTaskInstanceDto;
import com.github.bryx.workflow.dto.runtime.CreateWorkflowTimerJobDto;
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

    @Autowired
    WorkflowEngineProperties workflowEngineProperties;

    public List<WorkflowTaskInstance> createWorkflowTaskInstances(String workflowInstanceId, WorkflowDefRev workflowDefRev, List<TaskObject> tasks){
        // 创建workflow task instances
        List<WorkflowTaskInstance> workflowTaskInstancess = Lists.newArrayList();
        tasks.forEach(task->{
            WorkflowTaskInstance workflowTaskInstance = workflowRuntimeService.createWorkflowTaskInstance(CreateWorkflowTaskInstanceDto.builder()
                    .name(task.getName())
                    .processTaskDefId(task.getDefinitionId())
                    .processTaskId(task.getId())
                    .workflowInstanceId(workflowInstanceId).build());
            workflowTaskInstancess.add(workflowTaskInstance);
        });
        return workflowTaskInstancess;
    }

    /**
     * 检测taskObjects上是否有timer的配置，如果配置了extension则要求interceptor创建timer,如果有配置了cron和duration则创建
     * @param taskInstances
     * @param workflowInstance
     * @param processConfig
     * @param workflowInstanceInterceptor
     */
    public void createWorkflowTimerJobsIfNecessary(WorkflowInstance workflowInstance, List<WorkflowTaskInstance> taskInstances, WorkflowDefProcessConfig processConfig, WorkflowInstanceInterceptor workflowInstanceInterceptor){
        if (!workflowEngineProperties.isTimerEnable()){
            return;
        }
        taskInstances.forEach(taskInstance->{
            Map<String, WorkflowDefProcessConfig.TimerConfig> timerConfigs = processConfig.getUserTasks().get(taskInstance.getProcessTaskDefId()).getTimer();
            if (CollectionsUtil.mapNotEmpty(timerConfigs)){
                timerConfigs.forEach((timerDefinitionId, timerConfig)->{
                    if(StringUtil.isNotEmpty(timerConfig.getExtension())){
                        timerConfig.setTimerDefinitionId(timerDefinitionId);
                        // interceptor负责创建timer
                       workflowInstanceInterceptor.createTimerJobWhenExtensionConfigured(workflowInstance, taskInstance, timerConfig);
                    }else{
                        if(timerConfig.getDuration() == null || timerConfig.getTimeUnit() == null){
                            throw new WorkflowRuntimeException("can not create timer,duration and timeunit required");
                        }
                        CreateWorkflowTimerJobDto createWorkflowTimerJobDto = null;
                        if (timerConfig.getRepeat()!=null && timerConfig.getRepeat()>0){
                            // create cron timer
                            createWorkflowTimerJobDto = CreateWorkflowTimerJobDto.ofTypeCycle(taskInstance.getId(), timerDefinitionId, timerConfig.getRepeat(), timerConfig.getDuration(), timerConfig.getTimeUnit(), null);
                        }else{
                            // create duration timer
                            createWorkflowTimerJobDto = CreateWorkflowTimerJobDto.ofTypeDuration(taskInstance.getId(), timerDefinitionId, timerConfig.getDuration(), timerConfig.getTimeUnit());
                        }
                        workflowRuntimeService.createWorkflowTimerJob(createWorkflowTimerJobDto);
                    }
                });
            }
        });
    }

}

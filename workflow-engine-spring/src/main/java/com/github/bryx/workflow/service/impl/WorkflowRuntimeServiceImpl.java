package com.github.bryx.workflow.service.impl;

import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.*;
import com.github.bryx.workflow.service.dao.WorkflowInstanceDao;
import com.github.bryx.workflow.command.executor.BaseCommandExecutor;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
import com.github.bryx.workflow.dto.runtime.*;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeQuery;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.github.bryx.workflow.service.dao.WorkflowTaskInstanceDao;
import com.github.bryx.workflow.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Service
@Slf4j
public class WorkflowRuntimeServiceImpl implements WorkflowRuntimeService, InitializingBean {

    @Autowired
    WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    WorkflowTaskInstanceDao workflowTaskInstanceDao;

    @Autowired
    ProcessService processService;

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    WorkflowRuntimeQuery workflowRuntimeQuery;

    @Override
    public String start(String workflowDefId, Map<String, Object> formData, String executorId) {
        StartCommand startCommand = StartCommand.builder()
                .workflowDefId(workflowDefId)
                .formData(formData)
                .executorId(executorId).build();
        return BaseCommandExecutor.<String, StartCommand>execute(startCommand);
    }

    @Override
    public List<String> rejectBack(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        RejectBackCommand command = RejectBackCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        return BaseCommandExecutor.<List<String>, RejectBackCommand>execute(command);
    }

    @Override
    public List<String> rejectBackAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        RejectBackCommand command = RejectBackCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        return BaseCommandExecutor.<List<String>, RejectBackCommand>execute(command);
    }

    @Override
    public void claim(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        ClaimCommand command = ClaimCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .build();
        BaseCommandExecutor.<Void, ClaimCommand>execute(command);
    }

    @Override
    public void transfer(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        TransferCommand command = TransferCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        BaseCommandExecutor.<Void, TransferCommand>execute(command);
    }

    @Override
    public void modify(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        ModifyCommand command = ModifyCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        BaseCommandExecutor.<Void, ModifyCommand>execute(command);
    }

    @Override
    public List<String> submit(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        SubmitCommand command = SubmitCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        return BaseCommandExecutor.<List<String>, SubmitCommand>execute(command);
    }

    @Override
    public List<String> submitAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        SubmitCommand command = SubmitCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        return BaseCommandExecutor.<List<String>, SubmitCommand>execute(command);
    }

    @Override
    public void close(String workflowInstanceId, String executorId) {
        CloseCommand command = CloseCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .executorId(executorId)
                .build();
        BaseCommandExecutor.<Void, CloseCommand>execute(command);
    }


    @Override
    public String createWorkflowInstance(CreateWorkflowInstanceDto dto) {
        Validate.notNull(dto.getDefId(), "must provide workflow def id");
        Validate.notNull(dto.getDefRevId(), "must provide workflow def rev id");
        Validate.notNull(dto.getProcessId(), "must provide activiti process id");
        Validate.notNull(dto.getCreatorId(), "must provide creator id");
        WorkflowInstance workflowInstance = new WorkflowInstance();
        BeanUtils.copyProperties(dto, workflowInstance);
        workflowInstance.setCreateTime(new Date());
        workflowInstance.setDeleted(false);
        workflowInstance.setStatus(WorkflowInstance.WorkflowInstanceStatus.ONGOING);
        // TODO set seq
        workflowInstanceDao.save(workflowInstance);
        return workflowInstance.getId();
    }

    @Override
    public String createWorkflowTaskInstance(CreateWorkflowTaskInstanceDto dto) {
        Validate.notNull(dto.getName(), "must provide task name");
        Validate.notNull(dto.getProcessTaskId(), "must provide actitivi process task id");
        Validate.notNull(dto.getWorkflowInstanceId(), "must provide workflow instance id");
        WorkflowTaskInstance taskInstance = new WorkflowTaskInstance();
        BeanUtils.copyProperties(dto, taskInstance);
        taskInstance.setStartTime(new Date());
        taskInstance.setStatus(WorkflowTaskInstance.WorkflowTaskInstanceStatus.ONGOING);
        workflowTaskInstanceDao.save(taskInstance);
        return taskInstance.getId();
    }

    @Override
    public void createWorkflowTimerInstance(CreateWorkflowTimerInstanceDto createWorkflowTimerInstanceDto){
        String processTaskId = null;
        if (StringUtil.isNotEmpty(createWorkflowTimerInstanceDto.getWorkflowTaskInstanceId())){
            WorkflowTaskInstance workflowTaskInstance = workflowTaskInstanceDao.getById(createWorkflowTimerInstanceDto.getWorkflowTaskInstanceId());
            processTaskId = workflowTaskInstance.getProcessTaskId();
        }else{
            processTaskId = createWorkflowTimerInstanceDto.getProcessTaskId();
        }
        Validate.notNull(processTaskId, "please specify valid workflowTaskInstanceId or processTaskId");
        Validate.notNull(createWorkflowTimerInstanceDto.getTimerDefinitionId(), "please specify timeDefinitionId");
        if (StringUtil.isNotEmpty(createWorkflowTimerInstanceDto.getCron())){
            processService.addTimerToTask(processTaskId, createWorkflowTimerInstanceDto.getTimerDefinitionId(), createWorkflowTimerInstanceDto.getCron());
        }else if(createWorkflowTimerInstanceDto.getDuration()!=null && createWorkflowTimerInstanceDto.getTimeUnit()!=null){
            processService.addTimerToTask(processTaskId, createWorkflowTimerInstanceDto.getTimerDefinitionId(), createWorkflowTimerInstanceDto.getDuration(), createWorkflowTimerInstanceDto.getTimeUnit());
        }else{
            throw new WorkflowRuntimeException("could not create timer instance, either cron or duration timeunit specified");
        }
    }

    @Override
    public void delete(String workflowInstanceId, String executorId) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(workflowInstanceId);
        workflowInstance.setDeleted(true);
        workflowInstance.setLastModifierId(executorId);
        workflowInstanceDao.updateById(workflowInstance);
    }

    @Override
    public void updateWorkflowInstance(UpdateWorkflowInstanceDto dto) {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(dto.getId());
        workflowInstance.setFormData(dto.getFormData());
        workflowInstance.setStatus(dto.getStatus());
        workflowInstance.setLastModifierId(dto.getExecutorId());
        workflowInstance.setLastModifyTime(new Date());
        workflowInstanceDao.updateById(workflowInstance);
    }

    @Override
    public void updateWorkflowTaskInstance(UpdateWorkflowTaskInstanceDto dto) {
        WorkflowTaskInstance workflowTaskInstance = new WorkflowTaskInstance();
        workflowTaskInstance.setId(dto.getId());
        workflowTaskInstance.setStatus(dto.getStatus());
        if (workflowTaskInstance.getStatus() != null
                && WorkflowTaskInstance.WorkflowTaskInstanceStatus.COMPLETED.equals(workflowTaskInstance.getStatus())){
            workflowTaskInstance.setEndTime(new Date());
        }
        workflowTaskInstance.setExecutorId(dto.getExecutorId());
        workflowTaskInstance.setFormData(dto.getFormData());
        workflowTaskInstanceDao.updateById(workflowTaskInstance);
    }

    @Override
    public WorkflowRuntimeQuery query() {
        if (this.workflowRuntimeQuery == null){
            WorkflowRuntimeQueryImpl workflowRuntimeQueryImpl = new WorkflowRuntimeQueryImpl();
            workflowRuntimeQueryImpl.setProcessService(this.processService);
            workflowRuntimeQueryImpl.setWorkflowInstanceDao(this.workflowInstanceDao);
            workflowRuntimeQueryImpl.setWorkflowTaskInstanceDao(this.workflowTaskInstanceDao);
            this.workflowRuntimeQuery = workflowRuntimeQueryImpl;
        }
        return this.workflowRuntimeQuery;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        processService.setTimerTriggerHandler(taskTimer->{
            try{
                String processId = taskTimer.getTask().getProcessId();
                WorkflowInstance workflowInstance = workflowInstanceDao.lambdaQuery().eq(WorkflowInstance::getProcessId, processId).one();
                WorkflowTaskInstance workflowTaskInstance = workflowTaskInstanceDao.lambdaQuery().eq(WorkflowTaskInstance::getProcessTaskId, taskTimer.getTask().getId()).one();
                WorkflowTimerInstance timerInstance = new WorkflowTimerInstance();
                timerInstance.setDefinitionId(taskTimer.getDefinitionId());
                timerInstance.setTriggerTime(taskTimer.getTriggerTime());
                timerInstance.setTask(taskTimer.getTask());
                if (workflowTaskInstance!=null){
                    TimerTriggerCommand command = TimerTriggerCommand.builder()
                            .workflowInstance(workflowInstance)
                            .workflowTaskInstance(workflowTaskInstance)
                            .workflowTimerInstance(timerInstance)
                            .build();
                    BaseCommandExecutor.<Void, TimerTriggerCommand>execute(command);
                }
            }catch (Exception e){
                log.error("timer trigger error", e);
            }
        });
    }
}

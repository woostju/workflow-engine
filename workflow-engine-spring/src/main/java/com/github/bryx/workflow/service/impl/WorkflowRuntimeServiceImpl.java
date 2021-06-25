package com.github.bryx.workflow.service.impl;

import com.github.bryx.workflow.domain.WorkflowTimerJob;
import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.github.bryx.workflow.service.dao.WorkflowTimerJobDao;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.command.*;
import com.github.bryx.workflow.service.dao.WorkflowInstanceDao;
import com.github.bryx.workflow.command.executor.CommandExecutor;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.domain.WorkflowTimerInstance;
import com.github.bryx.workflow.dto.runtime.*;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeQuery;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.github.bryx.workflow.service.dao.WorkflowTaskInstanceDao;
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
    WorkflowTimerJobDao workflowTimerJobDao;

    @Autowired
    ProcessService processService;

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeQuery workflowRuntimeQuery;

    @Override
    public String start(String workflowDefId, Map<String, Object> formData, String executorId) {
        StartWorkflowInstanceCommand startWorkflowInstanceCommand = StartWorkflowInstanceCommand.builder()
                .workflowDefId(workflowDefId)
                .formData(formData)
                .executorId(executorId).build();
        return CommandExecutor.<String, StartWorkflowInstanceCommand>execute(startWorkflowInstanceCommand);
    }

    @Override
    public List<String> rejectBack(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        RejectBackUserTaskCommand command = RejectBackUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        return CommandExecutor.<List<String>, RejectBackUserTaskCommand>execute(command);
    }

    @Override
    public List<String> rejectBackAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        RejectBackUserTaskCommand command = RejectBackUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeUserIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        return CommandExecutor.<List<String>, RejectBackUserTaskCommand>execute(command);
    }

    @Override
    public void claim(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        ClaimUserTaskCommand command = ClaimUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .build();
        CommandExecutor.<Void, ClaimUserTaskCommand>execute(command);
    }

    @Override
    public void transfer(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        TransferUserTaskCommand command = TransferUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeUserIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        CommandExecutor.<Void, TransferUserTaskCommand>execute(command);
    }

    @Override
    public void modify(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        ModifyUserTaskCommand command = ModifyUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        CommandExecutor.<Void, ModifyUserTaskCommand>execute(command);
    }

    @Override
    public List<String> submit(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, String executorId) {
        SubmitUserTaskCommand command = SubmitUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .build();
        return CommandExecutor.<List<String>, SubmitUserTaskCommand>execute(command);
    }

    @Override
    public List<String> submitAndAssign(String workflowInstanceId, String workflowInstanceTaskId, Map<String, Object> formData, List<String> assignUserIds, List<String> assigneeGroupIds, String executorId) {
        SubmitUserTaskCommand command = SubmitUserTaskCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .workflowTaskInstanceId(workflowInstanceTaskId)
                .executorId(executorId)
                .formData(formData)
                .assigneeUserIds(assignUserIds)
                .assigneeGroupIds(assigneeGroupIds)
                .build();
        return CommandExecutor.<List<String>, SubmitUserTaskCommand>execute(command);
    }

    @Override
    public void close(String workflowInstanceId, String executorId) {
        CloseWorkflowInstanceCommand command = CloseWorkflowInstanceCommand.builder()
                .workflowInstanceId(workflowInstanceId)
                .executorId(executorId)
                .build();
        CommandExecutor.<Void, CloseWorkflowInstanceCommand>execute(command);
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
    public WorkflowTaskInstance createWorkflowTaskInstance(CreateWorkflowTaskInstanceDto dto) {
        Validate.notNull(dto.getName(), "must provide task name");
        Validate.notNull(dto.getProcessTaskId(), "must provide actitivi process task id");
        Validate.notNull(dto.getWorkflowInstanceId(), "must provide workflow instance id");
        WorkflowTaskInstance taskInstance = new WorkflowTaskInstance();
        BeanUtils.copyProperties(dto, taskInstance);
        taskInstance.setStartTime(new Date());
        taskInstance.setStatus(WorkflowTaskInstance.WorkflowTaskInstanceStatus.ONGOING);
        workflowTaskInstanceDao.save(taskInstance);
        return taskInstance;
    }

    @Override
    public String createWorkflowTimerJob(CreateWorkflowTimerJobDto createWorkflowTimerJobDto){
        Validate.notNull(createWorkflowTimerJobDto.getWorkflowTaskInstanceId(), "please specify valid processTaskId");
        WorkflowTaskInstance workflowTaskInstance = workflowTaskInstanceDao.getById(createWorkflowTimerJobDto.getWorkflowTaskInstanceId());
        Validate.notNull(workflowTaskInstance, "Workflow task instance does not exist");
        Validate.notNull(createWorkflowTimerJobDto.getTimerDefinitionId(), "please specify timeDefinitionId");
        TaskTimer timer = null;
        switch (createWorkflowTimerJobDto.getType()){
            case CRON:
                timer = processService.addTimerOnTask(workflowTaskInstance.getProcessTaskId(), createWorkflowTimerJobDto.getTimerDefinitionId() , createWorkflowTimerJobDto.getCron(), createWorkflowTimerJobDto.getEndDate());
                break;
            case CYCLE:
                timer = processService.addTimerOnTask(workflowTaskInstance.getProcessTaskId(), createWorkflowTimerJobDto.getTimerDefinitionId(), createWorkflowTimerJobDto.getRepeat(), createWorkflowTimerJobDto.getDuration(), createWorkflowTimerJobDto.getTimeUnit(), createWorkflowTimerJobDto.getEndDate());
                break;
            case DURATION:
                timer = processService.addTimerOnTask(workflowTaskInstance.getProcessTaskId(), createWorkflowTimerJobDto.getTimerDefinitionId(), createWorkflowTimerJobDto.getDuration(), createWorkflowTimerJobDto.getTimeUnit());
                break;
            case FIXED_DATE:
                timer = processService.addTimerOnTask(workflowTaskInstance.getProcessTaskId(), createWorkflowTimerJobDto.getTimerDefinitionId(), createWorkflowTimerJobDto.getFixedDate());
                break;
        }
        Validate.notNull(timer, "fail to create timer");
        WorkflowTimerJob workflowTimerJob = WorkflowTimerJob.builder()
                .addon(createWorkflowTimerJobDto.getAddon())
                .workflowInstanceId(workflowTaskInstance.getWorkflowInstanceId())
                .workflowTaskInstanceId(workflowTaskInstance.getId())
                .endDate(createWorkflowTimerJobDto.getEndDate())
                .status(WorkflowTimerJob.Status.CREATED)
                .processTimerDefId(createWorkflowTimerJobDto.getTimerDefinitionId())
                .processTimerJobId(timer.getJobId())
                .nextTriggerTime(timer.getTriggerTime())
                .build();
        workflowTimerJobDao.save(workflowTimerJob);
        return workflowTimerJob.getId();
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
        return workflowRuntimeQuery;
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
                    CommandExecutor.<Void, TimerTriggerCommand>execute(command);
                }
            }catch (Exception e){
                log.error("timer trigger error", e);
            }
        });
    }
}

package com.github.bryx.workflow.command.process;


import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.task.Comment;

public class CreateTimerCmd implements Command<Comment> {
	public enum TimerEventDefinitionType{
		TIME_DURATION,
		TIME_CYCLE,
		TIME_DATE
	}
	protected String timeValue;
	protected TimerEventDefinitionType timerEventDefinitionType;
	protected String taskId;
	protected BoundaryEvent timerBoundaryEvent;

	public CreateTimerCmd(BoundaryEvent timerBoundaryEvent, String taskId , TimerEventDefinitionType type, String timeValue) {
		this.taskId = taskId;
		this.timerBoundaryEvent = timerBoundaryEvent;
		this.timerEventDefinitionType = type;
		this.timeValue = timeValue;
	}

	/**
	 * 创建timer job
	 * 需要为当前task的execution上创建child execution，并且创建timer job，并且将execution绑定到timerboudryevent上
	 */
	public Comment execute(CommandContext commandContext) {
		TaskEntity task = commandContext.getTaskEntityManager().findById(taskId);
		ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(task.getExecutionId());
		
		ExecutionEntity childExecutionEntity = commandContext.getExecutionEntityManager().createChildExecution((ExecutionEntity) execution);
	    childExecutionEntity.setParentId(execution.getId());
	    childExecutionEntity.setCurrentFlowElement(timerBoundaryEvent);
	    childExecutionEntity.setScope(false);
		
		JobManager jobManager = Context.getCommandContext().getJobManager();
	    TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
	    switch (this.timerEventDefinitionType){
			case TIME_CYCLE:
				timerEventDefinition.setTimeCycle(this.timeValue);
				break;
			case TIME_DATE:
				timerEventDefinition.setTimeDate(this.timeValue);
				break;
			case TIME_DURATION:
				timerEventDefinition.setTimeDuration(this.timeValue);
				break;
		}
		TimerJobEntity timerJob = jobManager.createTimerJob(timerEventDefinition, false, childExecutionEntity, TriggerTimerEventJobHandler.TYPE,
	        TimerEventHandler.createConfiguration(childExecutionEntity.getCurrentActivityId(), timerEventDefinition.getEndDate(), timerEventDefinition.getCalendarName()));
	    if (timerJob != null) {
	      jobManager.scheduleTimerJob(timerJob);
	    }
		return null;
	}
}
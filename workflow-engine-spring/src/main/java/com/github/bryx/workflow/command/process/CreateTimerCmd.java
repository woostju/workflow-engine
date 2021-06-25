package com.github.bryx.workflow.command.process;


import com.github.bryx.workflow.exception.ProcessRuntimeException;
import com.github.bryx.workflow.util.DateTimeUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * timer支持三种类型
 * 1. TIME_DURATION
 * 	PT2M 表示2分钟后
 * 	PT1H 表示1小时后
 * 2. TIME_CYCLE
 * 	2.1 支持cron和ISO 8601格式
 * 	2.2 ISO 8601格式：R2/PT1M 重复两次，每次1分钟
 * 3. TIME_DATE
 *  指定时间触发,
 *  格式2019-07-13T16:42:11
 *
 */
@Data
@Builder
public class CreateTimerCmd implements Command<TimerJobEntity> {
	public enum TimerEventDefinitionType{
		TIME_DURATION,
		TIME_CYCLE,
		TIME_CRON,
		TIME_FIXED_DATE
	}


	private String timeValue;

	/**
	 * endDate指的是某个时间让重复触发的失效
	 */
	private String endDate;
	/**
	 * 计时类型
	 */
	private TimerEventDefinitionType timerEventDefinitionType;
	/**
	 * 任务节点
	 */
	protected String taskId;
	protected BoundaryEvent timerBoundaryEvent;

	private static String formatInISO8601(Date date){
		return DateTimeUtils.formatDate(date, "yyyy-MM-dd'T'HH:mm:ssZZ");
	}

	private static String durationInISO8601(int interval, TimeUnit timeUnit){
		String timeDuration = "PT%d%s";
		switch (timeUnit){
			case DAYS:
				timeDuration = String.format(timeDuration, interval, "D");
				break;
			case MINUTES:
				timeDuration = String.format(timeDuration, interval, "M");
				break;
			case SECONDS:
				timeDuration = String.format(timeDuration, interval, "S");
				break;
			case HOURS:
				timeDuration = String.format(timeDuration, interval, "H");
				break;
			default:
				throw new ProcessRuntimeException("only support Days, MINUTES, HOURS, SECONDS for timer");
		}
		return timeDuration;
	}

	/**
	 *
	 * TIME_CYCLE ISO 8601格式：R2/PT1M 重复两次，每次1分钟
	 *
	 * @param taskId
	 * @param repeat 重复次数
	 * @param interval 间隔
	 * @param timeUnit 间隔单位
	 * @param endDate 结束重复
	 * @return
	 */
	public static CreateTimerCmd ofTypeCycle(String taskId, int repeat, int interval, TimeUnit timeUnit, Date endDate){
		CreateTimerCmd createTimerCmd = CreateTimerCmd.builder()
				.timerEventDefinitionType(TimerEventDefinitionType.TIME_CYCLE)
				.taskId(taskId)
				.build();
		if (endDate!=null){
			createTimerCmd.setEndDate(formatInISO8601(endDate));
			createTimerCmd.setTimeValue(String.format("R%d/%s/%s", repeat, durationInISO8601(interval, timeUnit), createTimerCmd.getEndDate()));
		}else{
			createTimerCmd.setTimeValue(String.format("R%s/PT%s", repeat, durationInISO8601(interval, timeUnit)));
		}
		return createTimerCmd;
	}

	/**
	 * TIME_DATE 指定时间触发, 格式2019-07-13T16:42:11
	 * @param taskId
	 * @param date 指定时间执行
	 * @return
	 */
	public static CreateTimerCmd ofTypeFixedDate(String taskId, Date date){
		CreateTimerCmd createTimerCmd = CreateTimerCmd.builder()
				.timerEventDefinitionType(TimerEventDefinitionType.TIME_FIXED_DATE)
				.taskId(taskId)
				.build();
		createTimerCmd.setTimeValue(DateTimeUtils.formatDate(date, "yyyy-MM-dd'T'HH:mm:ssZZ"));
		return createTimerCmd;
	}

	/**
	 * TIME_CYCLE， 支持cron
	 * @param taskId
	 * @param cron cron表达式
	 * @param endDate 结束重复
	 * @return
	 */
	public static CreateTimerCmd ofTypeCron(String taskId, String cron, Date endDate){
		CreateTimerCmd createTimerCmd = CreateTimerCmd.builder()
				.timerEventDefinitionType(TimerEventDefinitionType.TIME_CRON)
				.taskId(taskId)
				.timeValue(cron)
				.build();
		if (endDate!=null) {
			createTimerCmd.setEndDate(formatInISO8601(endDate));
		}
		return createTimerCmd;
	}

	/**
	 *
	 * TIME_DURATION 延时执行，PT2M 表示2分钟后执行
	 * @param taskId
	 * @param duration 间隔
	 * @param timeUnit
	 * @return
	 */
	public static CreateTimerCmd ofTypeDuration(String taskId, int duration, TimeUnit timeUnit){
		CreateTimerCmd createTimerCmd = CreateTimerCmd.builder()
				.timerEventDefinitionType(TimerEventDefinitionType.TIME_DURATION)
				.taskId(taskId)
				.timeValue(durationInISO8601(duration, timeUnit))
				.build();
		return createTimerCmd;
	}

	/**
	 * 创建timer job
	 * 需要为当前task的execution上创建child execution，并且创建timer job，并且将execution绑定到timerboudryevent上
	 */
	public TimerJobEntity execute(CommandContext commandContext) {
		TaskEntity task = commandContext.getTaskEntityManager().findById(taskId);
		ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(task.getExecutionId());
		
		ExecutionEntity childExecutionEntity = commandContext.getExecutionEntityManager().createChildExecution((ExecutionEntity) execution);
	    childExecutionEntity.setParentId(execution.getId());
	    childExecutionEntity.setCurrentFlowElement(timerBoundaryEvent);
	    childExecutionEntity.setScope(false);
		
		JobManager jobManager = Context.getCommandContext().getJobManager();
		TimerEventDefinition timerEventDefinition = createTimerEventDefinition();
		TimerJobEntity timerJob = jobManager.createTimerJob(timerEventDefinition, false, childExecutionEntity, TriggerTimerEventJobHandler.TYPE,
	        TimerEventHandler.createConfiguration(childExecutionEntity.getCurrentActivityId(), timerEventDefinition.getEndDate(), timerEventDefinition.getCalendarName()));
	    if (timerJob != null) {
	      jobManager.scheduleTimerJob(timerJob);
	    }
		return timerJob;
	}

	private TimerEventDefinition createTimerEventDefinition() {
		TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
		switch (this.timerEventDefinitionType){
			case TIME_CYCLE:
				timerEventDefinition.setTimeCycle(this.timeValue);
				break;
			case TIME_CRON:
				timerEventDefinition.setTimeCycle(this.timeValue);
				break;
			case TIME_FIXED_DATE:
				timerEventDefinition.setTimeDate(this.timeValue);
				break;
			case TIME_DURATION:
				timerEventDefinition.setTimeDuration(this.timeValue);
				break;
		}
		if (this.endDate != null){
			timerEventDefinition.setEndDate(this.getEndDate());
		}
		return timerEventDefinition;
	}


}
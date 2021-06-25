package com.github.bryx.workflow.service.process;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.bryx.workflow.functions.ToObjBiFunction;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDefElement;
import com.github.bryx.workflow.domain.process.runtime.ProcessActivityObject;
import com.github.bryx.workflow.domain.process.runtime.ProcessObject;
import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activiti.engine.delegate.event.ActivitiEvent;

import lombok.NonNull;

/**
 * 
 * @author jameswu
 *
 */
public interface ProcessService {

	/**
	 * @param processDefinitionId processDefinitionId
	 * @param executorId          executor
	 * @param taskAssign          assignee setter, dynamic set the current tasks' assignee
	 * @return process id
	 * <p>
	 * 发起流程实例
	 */
	public String startProcess(String processDefinitionId, String executorId, Map variables, ToObjBiFunction<List<TaskObject>, ProcessObject, Map<String, List<TaskObjectAssignee>>> taskAssign);

	/**
	 * @param processId
	 * @return 获取当前任务, 任务中不包含timers，包含受理人候选人信息
	 */
	public List<TaskObject> getTasks(String processId);

	/**
	 * @param taskIds
	 * @return get tasks by ids
	 */
	public List<TaskObject> getTasks(List<String> taskIds);

	/**
	 * @param taskId
	 * @return get task by id
	 */
	default public Optional<TaskObject> getTaskById(String taskId) {
		List<TaskObject> tasks = this.getTasks(Lists.newArrayList(taskId));
		if (CollectionsUtil.isNotEmpty(tasks) && tasks.size() == 1) {
			return Optional.of(tasks.get(0));
		} else {
			return Optional.ofNullable(null);
		}
	}

	/**
	 * @param taskExecutionId
	 * @return 获取任务上的超时任务
	 */
	public List<TaskTimer> getTaskTimers(String processId, String taskExecutionId);

	/**
	 * 返回processid上所有的timertask
	 *
	 * @param processId
	 * @return
	 */
	public List<TaskTimer> getTaskTimers(String processId);

	public TaskTimer getTaskTimerByJobId(String timerJobId);

	public void deleteProcess(String processId);

	/**
	 * @param processId
	 * @param taskId
	 * @param variables
	 * @return 新创建的任务ids
	 * <p>
	 * 在流程特定节点上执行任务
	 */
	public List<String> execute(String processId, String taskId, String executorId, Map variables, ToObjBiFunction<List<TaskObject>, ProcessObject, Map<String, List<TaskObjectAssignee>>> newTaskAssign);


	/**
	 * 认领任务
	 *
	 * @param taskId
	 * @param assignee
	 */
	public void claimTask(String taskId, String assignee);

	/**
	 * @param processId
	 * @param taskId
	 * @param executorId
	 * @param variables
	 * @param taskAssign null, assignee of the tasks reject back to will be executor
	 * @return
	 */
	public List<String> rejectBack(String processId, String taskId, String executorId, Map variables, ToObjBiFunction<List<TaskObject>, ProcessObject, Map<String, List<TaskObjectAssignee>>> taskAssign);

	/**
	 * @param processId
	 * @param executorId 直接关闭
	 */
	public void closeProcess(@NonNull String processId, @NonNull String executorId);

	/**
	 * @param processId
	 * @return 获取流程定义详情, 定义中的taskObjects不包含timers信息
	 */

	public ProcessObject getProcess(String processId);

	/**
	 * @param taskId
	 * @param assignees 设置流程节点上的受理人及受理组
	 */
	public void updateAssignees(String taskId, List<TaskObjectAssignee> assignees);

	/**
	 * @param processId
	 * @return get all historic activities on process, you can display historic on web site
	 */
	public List<ProcessActivityObject> getHistoricActivities(String processId);


	/**
	 * @param processId
	 * @return get all historic tasks on process
	 */
	public List<TaskObject> getHistoricTasks(String processId);

	/**
	 * @param processId
	 * @return get historic tasks on process, there may be multiple taskInst on the same taskDef
	 */
	default public Map<String, List<TaskObject>> getHistoricTasksMapKeyTaskDefId(String processId) {
		return this.getHistoricTasks(processId).stream().collect(Collectors.groupingBy(TaskObject::getDefinitionId));
	}

	default public Map<String, TaskObject> getHistoricTasksMapKeyTaskId(String processId) {
		return this.getHistoricTasks(processId).stream().collect(Collectors.toMap(TaskObject::getId, Function.identity()));
	}

	default public Map<String, TaskObject> getHistoricRecentTasksMapKeyTaskDef(String processId) {
		Map<String, List<TaskObject>> historicTasksMapKeyTaskDefId = this.getHistoricTasksMapKeyTaskDefId(processId);
		Map<String, TaskObject> tasks = Maps.newHashMap();
		historicTasksMapKeyTaskDefId.forEach((key, value) -> {
			value.stream().sorted(new Comparator<TaskObject>() {
				@Override
				public int compare(TaskObject o1, TaskObject o2) {
					return (int) (o1.getEndTime().getTime() - o2.getEndTime().getTime());
				}
			});
			tasks.put(key, value.get(0));
		});
		return tasks;
	}

	public String getIncomingFlowDocumentation(String task_id, Map<String, Object> variables);

	public void walkToTargetUserTasks(String task_id, String process_def_deploymentId, Map<String, Object> variables, Consumer<ProcessDefElement> activityObject);

	/**
	 * @param taskId 任务id
	 * @return 获取任务的上一个历史执行节点
	 */
	public TaskObject getRuntimePreviousUserTask(String taskId);


	/**
	 * 延时触发
	 *
	 * @param taskId               任务id
	 * @param timerBoudryElementId activiti bpmn中的timer id
	 * @param duration
	 * @param timeUnit             support DAYS, MINUTES, HOURS, SECONDS
	 */
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, int duration, TimeUnit timeUnit);

	/**
	 * 重复执行
	 *
	 * @param taskId               任务id
	 * @param timerBoudryElementId activiti bpmn中的timer id
	 * @param repeat               重复次数
	 * @param interval             重复间隔
	 * @param intervalTimeUnit     重复间隔单位
	 * @param endDate              结束日期
	 */
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, int repeat, int interval, TimeUnit intervalTimeUnit, Date endDate);

	/**
	 * 指定日期单次执行
	 *
	 * @param taskId               任务id
	 * @param timerBoudryElementId activiti bpmn中的timer id
	 * @param fixedDate            指定日期
	 */
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, Date fixedDate);

	/**
	 * cron表达式
	 *
	 * @param taskId               任务id
	 * @param timerBoudryElementId activiti bpmn中的timer id
	 * @param cron                 cron表达式
	 * @param endDate              结束日期
	 */
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, String cron, Date endDate);

	public void timerEventTriggered(ActivitiEvent event);

	public void setTimerTriggerHandler(Consumer<TaskTimer> timerTriggeredHandler);

}


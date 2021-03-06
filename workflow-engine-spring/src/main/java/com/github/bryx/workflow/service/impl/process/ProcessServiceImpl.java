package com.github.bryx.workflow.service.impl.process;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import com.alibaba.fastjson.JSON;
import com.github.bryx.workflow.exception.ProcessRuntimeException;
import com.github.bryx.workflow.functions.ToObjBiFunction;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDefElement;
import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.github.bryx.workflow.mapper.process.ProcessMapper;
import com.github.bryx.workflow.command.process.CloseProcessCmd;
import com.github.bryx.workflow.command.process.CreateTimerCmd;
import com.github.bryx.workflow.command.process.DeleteExecutionCmd;
import com.github.bryx.workflow.command.process.JumpTaskCmd;
import com.github.bryx.workflow.domain.process.runtime.ProcessActivityObject;
import com.github.bryx.workflow.domain.process.runtime.ProcessObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.service.dao.process.TaskObjectAssigneeDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * 
 * @author James Wu
 * 
 * ?????????????????????????????????????????????timer??????
 * ???????????????????????????addtimertotask??????timer
 *
 */

@Service
@Slf4j
public class ProcessServiceImpl implements ProcessService{

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	TaskObjectAssigneeDao taskObjectAssigneeDao;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	private TaskService taskService;

	@Autowired
	ManagementService managementService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private ProcessDefinitionService processDefinitionService;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private ProcessEngineConfigurationImpl processEngineConfiguration;

	Consumer<TaskTimer> timerTriggeredHandler;

	@Override
	public List<TaskObject> getTasks(String processId) {
		// TODO should return the task on parrall gateway
		List<TaskObject> tasks = processMapper.getTasksByProcessId(processId);
		tasks.forEach(task->{
			task.setState(TaskObject.TaskObjectState.NORMAL);
		});
		return tasks;
	}

	@Override
	public List<TaskObject> getTasks(List<String> taskIds) {
		// TODO should return the task on parrall gateway
		List<TaskObject> tasks = processMapper.getTasksByIds(taskIds);
		tasks.forEach(task->{
			task.setState(TaskObject.TaskObjectState.NORMAL);
		});
		return tasks;
	}

	@Override
	public ProcessObject getProcess(String processId) {
		ProcessObject object = new ProcessObject(processId);
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processId).singleResult();
		if (processInstance != null){
			object.setStatus(ProcessObject.Status.RUNTIME);
			BeanUtils.copyProperties(processInstance, object);
			object.setVersion(processInstance.getProcessDefinitionVersion());
			object.setDefinitionId(processInstance.getProcessDefinitionId());
		}else{
			HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
					.processInstanceId(processId).singleResult();
			object.setStatus(ProcessObject.Status.HISTORIC);
			BeanUtils.copyProperties(historicProcessInstance, object);
			object.setVersion(historicProcessInstance.getProcessDefinitionVersion());
			object.setDefinitionId(historicProcessInstance.getProcessDefinitionId());
		}
		return object;
	}

	@Override
	public List<TaskObject> getHistoricTasks(String processId) {
		List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId)
				.orderByHistoricTaskInstanceEndTime().desc().list();
		List<TaskObject> historyTasks = Lists.newArrayList();
		tasks.forEach(task -> {
			if (task.getEndTime() != null) {
				if (task.getDeleteReason() != null && task.getDeleteReason().contains("boundary event")) {

				} else {
					TaskObject taskObject = TaskObject.builder().definitionId(task.getTaskDefinitionKey())
							.documentation(task.getDescription())
							.executionTime(task.getEndTime())
							.executorId(task.getAssignee())
							.processId(processId)
							.build();
					BeanUtils.copyProperties(task, taskObject);
					historyTasks.add(taskObject);
				}
			}
		});
		return historyTasks;
	}

	@Override
	public List<ProcessActivityObject> getHistoricActivities(String processId) {
		List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();
		List<ProcessActivityObject> historyActivities = Lists.newArrayList();
		activities.forEach(activity -> {
			ProcessActivityObject processInstActivity;
			ProcessActivityObject.ProcessInstActivityType type = null;
			if ("startEvent".equals(activity.getActivityType())) {
				type = ProcessActivityObject.ProcessInstActivityType.START_NODE;
			} else if ("userTask".equals(activity.getActivityType())) {
				type = ProcessActivityObject.ProcessInstActivityType.USER_TASK;
			} else if ("exclusiveGateway".equals(activity.getActivityType())) {
				type = ProcessActivityObject.ProcessInstActivityType.EXCLUSIVE_GATEWAY;
			} else if ("endEvent".equals(activity.getActivityType())) {
				type = ProcessActivityObject.ProcessInstActivityType.END_NODE;
			}
			if (type != null) {
				if (activity.getTaskId() != null) {
					TaskObject taskobject = new TaskObject();
					taskobject.setId(activity.getTaskId());
					// TODO set executorId and assignee
//					if (activity.getAssignee() == null || activity.getAssignee().startsWith(ASSIGNEE_GROUP_PREX)
//							|| activity.getAssignee().startsWith(ASSIGNEE_USER_PREX)) {
//						taskobject.setExecutorId(null);
//					} else {
//						taskobject.setExecutorId(activity.getAssignee());
//					}
					taskobject.setName(activity.getActivityName());
					processInstActivity = taskobject;
				} else {
					processInstActivity = new ProcessActivityObject();
				}
				processInstActivity.setType(type);
				processInstActivity.setDefinitionId(activity.getActivityId());
				processInstActivity.setName(activity.getActivityName());
				processInstActivity.setStartTime(activity.getStartTime());
				processInstActivity.setExecutionTime(activity.getEndTime());
				if (activity.getEndTime() == null) {
					// ????????????
					 HistoricTaskInstance histask = historyService.createHistoricTaskInstanceQuery().taskId(activity.getTaskId()).singleResult();
					 if(histask!=null){
						 processInstActivity.setExecutionTime(histask.getEndTime());
					}
				}
				historyActivities.add(processInstActivity);
			}
		});
		return historyActivities;
	}

    @Transactional
    @Override
	public String startProcess(String processDefinitionId, String executorId, Map variables, @NonNull ToObjBiFunction<List<TaskObject>, ProcessObject,Map<String, List<TaskObjectAssignee>>> taskAssign)
	{
		try {
			ProcessInstance processInstance =runtimeService.startProcessInstanceById(processDefinitionId, variables);
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
			// clear all timer job for new tasks
			// since we create timer job programmatically
			this.updateTasksAssignees_(ProcessObject.of(processInstance), processMapper.getTasksByProcessId(processInstance.getId()), taskAssign);
			this.clearTimerJobs(processInstance.getId(), tasks);

			log.info("start process:{},processDefinitionId:{},executor id:{}", processInstance.getId(), processDefinitionId, executorId);
			return processInstance.getId();
		} catch (Exception e) {
			log.error("start process fail", e);
			if(e.getMessage().contains("Unknown property used in expression")){
				// ??????exclusive gateway??????expression???????????????variables??????????????????property
				// ??????????????????getnexttask??????????????????????????????
				//this.walkToTargetUserTasks(null,deployment_id , variables, element->{});
				throw new ProcessRuntimeException(e.getMessage());
			}else if (e.getMessage().contains("No outgoing sequence flow of the exclusive gateway")) {
				throw new ProcessRuntimeException("No outgoing sequence flow of the exclusive gateway");
			}else if(e instanceof ActivitiOptimisticLockingException){
				throw new ProcessRuntimeException(e.getMessage());
			} else {
				throw new ProcessRuntimeException(e.getMessage());
			}
		}
    }

	/**
	 * update task assignee with ToObjBiFunction
	 * @param processInstance
	 * @param tasks
	 * @param taskAssign
	 */
    private void updateTasksAssignees_(ProcessObject processInstance, List<TaskObject> tasks, ToObjBiFunction<List<TaskObject>, ProcessObject,Map<String, List<TaskObjectAssignee>>> taskAssign){
    	if (taskAssign != null){
			Map<String, List<TaskObjectAssignee>> taskAssignees = taskAssign.apply(tasks, processInstance);
			taskAssignees.forEach((key, value)->{
				this.updateAssignees(key, value);
			});
		}
	}

	@Override
	@Transactional
	public void deleteProcess(String processId) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

		if(processInstance!=null){
			// clear task_object_assignee table
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
			taskObjectAssigneeDao.removeByTaskIds(tasks.stream().map(Task::getId).collect(Collectors.toList()));
			runtimeService.deleteProcessInstance(processId, "");
		}
		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
		if(historicProcessInstance!=null){
			historyService.deleteHistoricProcessInstance(processId);
		}
		
	}

	@Transactional
	@Override
	public List<String> execute(String processId, String taskId, String executorId, Map variables, @NonNull ToObjBiFunction<List<TaskObject>, ProcessObject, Map<String, List<TaskObjectAssignee>>> taskAssign) {
		log.info("execute task:{} on process:{},executor:{}" , taskId,processId, executorId);
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
		Set<String> oldTaskIds = tasks.stream().map(item->item.getId()).collect(Collectors.toSet());
		// ??????executor?????????????????????????????????????????????
		try {
			taskService.setAssignee(taskId, executorId);
			taskService.complete(taskId, variables);
			// remove taskObjectAssignee
			taskObjectAssigneeDao.removeByTaskIds(Lists.newArrayList(taskId));
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			if(e.getMessage().contains("Unknown property used in expression")){
				// ??????exclusive gateway??????expression???????????????variables??????????????????property
				// ??????????????????getnexttask??????????????????????????????
				//this.walkToTargetUserTasks(taskId, null, variables,module_attrs, element->{});
				throw new ProcessRuntimeException(e.getMessage());
			}else if (e.getMessage().contains("No outgoing sequence flow of the exclusive gateway")) {
				throw new ProcessRuntimeException("???????????????????????????????????????????????????????????????");
			}else if(e instanceof ActivitiOptimisticLockingException){
				throw new ProcessRuntimeException("????????????????????????????????????");
			} else {
				throw new ProcessRuntimeException(e.getMessage());
			}
		}
		{
			// clear all timer job for new tasks
			// since we create timer job programmatically
			List<Task> currentTasks = taskService.createTaskQuery().processInstanceId(processId).list();
			List<Task> newTasks = Lists.newArrayList();
			for(Task currentTask : currentTasks){
				if(!oldTaskIds.contains(currentTask.getId())){
					newTasks.add(currentTask);
				}
			}
			this.updateTasksAssignees_(this.getProcess(processId), newTasks.stream().map(item->TaskObject.of(item)).collect(Collectors.toList()), taskAssign);
			this.clearTimerJobs(processId, newTasks);
			return newTasks.stream().map(item->item.getId()).collect(Collectors.toList());
		}
	}


	@Override
	@Transactional
	public void closeProcess(@NonNull String processId, @NonNull String executorId){
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
		Set<TaskEntity> taskEntities = Sets.newHashSet(tasks.stream().map(item->(TaskEntity)item).collect(Collectors.toList()));
		ProcessObject processObject = this.getProcess(processId);
		Process processModel = processDefinitionService.getProcessModel(processObject.getDefinitionId());
		List<EndEvent> endFlowElement = processModel.findFlowElementsOfType(EndEvent.class);
		CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
		commandExecutor.execute(new CloseProcessCmd(executorId, taskEntities, endFlowElement.get(0)));
		if(!tasks.isEmpty()){
			runtimeService.deleteProcessInstance(processId, "??????");
			tasks.forEach(task->{
				taskObjectAssigneeDao.removeByTaskIds(Lists.newArrayList(task.getId()));
			});
		}
	}

	private void clearTimerJobs(String processId, List<Task> newTasks) {
		newTasks.forEach(item->{
			List<Execution> executions = runtimeService.createExecutionQuery()
					.processInstanceId(processId).parentId(item.getExecutionId()).list();
			executions.forEach(exec -> {
				List<Job> timeJobs = managementService.createTimerJobQuery().processInstanceId(processId)
						.executionId(exec.getId()).list();
				if(!timeJobs.isEmpty()){
					// ??????execution
					timeJobs.forEach(job->{
						managementService.deleteTimerJob(job.getId());
					});
					CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
					commandExecutor.execute(new DeleteExecutionCmd(exec.getId()));
				}
			});
		});
	}


	@Override
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, int duration, TimeUnit timeUnit) {
		return this.addTimerOnTask_(taskId, timerBoudryElementId, CreateTimerCmd.ofTypeDuration(taskId, duration, timeUnit));
	}

	@Override
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, int repeat, int interval, TimeUnit intervalTimeUnit, Date endDate) {
		return this.addTimerOnTask_(taskId, timerBoudryElementId, CreateTimerCmd.ofTypeCycle(taskId, repeat, interval, intervalTimeUnit, endDate));
	}

	@Override
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, Date fixedDate) {
		return this.addTimerOnTask_(taskId, timerBoudryElementId, CreateTimerCmd.ofTypeFixedDate(taskId, fixedDate));
	}

	@Override
	public TaskTimer addTimerOnTask(String taskId, String timerBoudryElementId, String cron, Date endDate) {
		return this.addTimerOnTask_(taskId, timerBoudryElementId, CreateTimerCmd.ofTypeCron(taskId, cron, endDate));
	}

	private TaskTimer addTimerOnTask_(String taskId, String timerBoudryElementId, CreateTimerCmd createTimerCmd){
		TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(createTimerCmd.getTaskId()).singleResult();
		Process process = processDefinitionService.getProcessModel(task.getProcessDefinitionId());
		// ??????????????????????????????task??????
		BoundaryEvent timerBoudry = (BoundaryEvent)process.getFlowElement(timerBoudryElementId, true);
		CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
		createTimerCmd.setTimerBoundaryEvent(timerBoudry);
		createTimerCmd.setTaskId(taskId);
		TimerJobEntity timerJobEntity = commandExecutor.execute(createTimerCmd);
		TaskTimer timer= new TaskTimer();
		timer.setJobId(timerJobEntity.getId());
		timer.setTriggerTime(timerJobEntity.getDuedate());
		log.info("Success create timer on jobId:{} taskId:{} timerBoundryElementId:{} createTimerCmd: {}",timerJobEntity.getId(), taskId, timerBoudryElementId, createTimerCmd);
		return timer;
	}

	@Override
	public List<TaskTimer> getTaskTimers(String processId, String taskExecutionId) {
		List<TaskTimer> timers = Lists.newArrayList();
		// ????????????????????????????????????,??????????????????
		List<Execution> executions = runtimeService.createExecutionQuery().parentId(taskExecutionId).list();
		for(Execution exec : executions){
			List<Job> timeJobs = managementService.createTimerJobQuery().processInstanceId(processId)
					.executionId(exec.getId()).list();
			// ?????????????????????????????????
			for(Job job : timeJobs){
				TaskTimer timer = new TaskTimer();
				Map config = JSON.parseObject(job.getJobHandlerConfiguration(), HashMap.class);
				timer.setDefinitionId((String) config.get("activityId"));
				timer.setTriggerTime(job.getDuedate());
				timers.add(timer);
			}
		}
		timers = timers.stream()
				.sorted((timer1, timer2) -> timer1.getTriggerTime().compareTo(timer2.getTriggerTime()))
				.collect(Collectors.toList());
		return timers;
	}

	@Override
	public TaskTimer getTaskTimerByJobId(String timerJobId) {
		List<Job> timeJobs = managementService.createTimerJobQuery().jobId(timerJobId).list();
		if (CollectionUtil.isNotEmpty(timeJobs)){
			TaskTimer timer = new TaskTimer();
			Job job = timeJobs.get(0);
			Map config = JSON.parseObject(job.getJobHandlerConfiguration(), HashMap.class);
			timer.setDefinitionId((String) config.get("activityId"));
			timer.setTriggerTime(job.getDuedate());
			timer.setJobId(timerJobId);
			return timer;
		}
		return null;
	}

	@Override
	public List<TaskTimer> getTaskTimers(String processId) {
		List<TaskTimer> timers = Lists.newArrayList();
		// ????????????????????????????????????,??????????????????
		List<Job> timeJobs = managementService.createTimerJobQuery()
				.processInstanceId(processId)
				.list();
		// ?????????????????????????????????
		for(Job job : timeJobs){
			TaskTimer timer = new TaskTimer();
			Map config = JSON.parseObject(job.getJobHandlerConfiguration(), HashMap.class);
			timer.setDefinitionId((String) config.get("activityId"));
			timer.setTriggerTime(job.getDuedate());
			timers.add(timer);
		}
		timers = timers.stream()
				.sorted((timer1, timer2) -> timer1.getTriggerTime().compareTo(timer2.getTriggerTime()))
				.collect(Collectors.toList());
		return timers;
	}

	@Override
	@Transactional
	public void updateAssignees(@NonNull String taskId, List<TaskObjectAssignee> assignees) {
		try{
			assignees.forEach(assignee->{assignee.setTaskId(taskId);});
			taskObjectAssigneeDao.removeByTaskIds(Lists.newArrayList(taskId));
			taskObjectAssigneeDao.saveBatch(assignees);
			log.info("update assignee on task:{}, assignee:{}",taskId, assignees);
		}catch(Exception e){
			if(e instanceof ActivitiOptimisticLockingException){
				throw new ProcessRuntimeException("????????????????????????????????????");
			}
		}
	}

	@Transactional
	@Override
	public void claimTask(String taskId, String assignee) {
		try{
			taskService.claim(taskId, assignee);
			this.updateAssignees(taskId, Lists.newArrayList(TaskObjectAssignee.builder().taskId(taskId).assigneeId(assignee).assigneeType(TaskObjectAssignee.AssigneeType.USER).build()));
			log.info("claim on task:{}, with assignee:{}",taskId, assignee);
		}catch(Exception e){
			if(e instanceof ActivitiOptimisticLockingException){
				throw new ProcessRuntimeException("????????????????????????????????????");
			}
		}
	}


	@Override
	public List<String> rejectBack(String processId, String taskId, String executorId, Map variables, ToObjBiFunction<List<TaskObject>, ProcessObject, Map<String, List<TaskObjectAssignee>>> taskAssign) {
		/**
		 * ?????????????????????
		 * ???????????????task????????????????????????????????????????????????????????????
		 * ??????????????????????????????????????????????????????????????? ???????????????????????????????????????????????????????????????????????????????????????
		 * ?????????????????????????????????task???execution????????????????????????????????????????????????execution
		 */
		log.info("reject back process:{},taskId:{},executor:{}", processId, taskId, executorId);
		TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
		// ?????????????????????????????????
		List<UserTask> targetTasks = this.getUserTasksRejectBackTo(processId, task, variables);
		if (targetTasks.isEmpty()) {
			throw new ProcessRuntimeException("????????????????????????");
		}
		List<HistoricActivityInstance> historyActivities = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processId).orderByHistoricActivityInstanceStartTime().desc().list();
		List<TaskObject> historicTasks = this.getHistoricTasks(processId);
		// ?????????????????????????????????????????????????????????????????????????????????targetTasks?????????
		List<TaskObject> allTasks = this.getTasks(processId);
		// oldTaskIds??????????????????????????????ids??????????????????????????????
		Set<String> oldTaskIds = allTasks.stream().map(item->item.getId()).collect(Collectors.toSet());
		Set<String> taskIdsToBeDeleted = new HashSet<>();
		Set<String> executionToBeDeleted = new HashSet<>();
		// ??????????????????????????????????????????????????????task???execution
		Process processModel = processDefinitionService.getProcessModel(task.getProcessDefinitionId());
		for (UserTask targetTask : targetTasks) {
			for (TaskObject item : allTasks) {
				if (this.findTargetElementBackwardFromElement(targetTask,
						processModel.getFlowElement(item.getDefinitionId(), true), historyActivities,variables)) {
					// ????????????????????????????????????????????????
					if(item.getState().equals(TaskObject.TaskObjectState.NORMAL)){
						taskIdsToBeDeleted.add(item.getId());
						executionToBeDeleted.add(item.getExecutionId());
					}else{
						executionToBeDeleted.add(item.getExecutionId());
					}
				}
			}
		}

		// ???????????????
		CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
		commandExecutor.execute(new JumpTaskCmd(taskId, executorId, taskIdsToBeDeleted, executionToBeDeleted, targetTasks));
		taskObjectAssigneeDao.removeByTaskIds(Lists.newArrayList(taskIdsToBeDeleted));
		{
			// ?????????????????????timer jobs
			List<Task> currentTasks = taskService.createTaskQuery().processInstanceId(processId).list();
			List<Task> newTasks = new ArrayList<>();
			for(Task currentTask : currentTasks){
				if(!oldTaskIds.contains(currentTask.getId())){
					newTasks.add(currentTask);
				}
			}
			List<String> newTaskIds = newTasks.stream().map(item -> item.getId()).collect(Collectors.toList());
			if (taskAssign != null){
				this.updateTasksAssignees_(this.getProcess(processId), this.getTasks(newTaskIds), taskAssign);
			}else{
				// set the executor to assignee
				newTasks.forEach(newTask->{
					String historicExecutorId = historicTasks.stream().filter(item -> item.getDefinitionId().equals(newTask.getTaskDefinitionKey())).sorted(new Comparator<TaskObject>() {
						@Override
						public int compare(TaskObject o1, TaskObject o2) {
							return (int) (o1.getEndTime().getTime() - o2.getEndTime().getTime());
						}
					}).findFirst().get().getExecutorId();
					this.updateAssignees(newTask.getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId(historicExecutorId).taskId(newTask.getId()).build()));
				});
			}

			this.clearTimerJobs(processId, newTasks);
			return newTaskIds;
		}

	}

	/**
	 * 
	 * @param flowHandler return true to continue walking, else return false to stop walking
	 * 
	 */
	public void walkToTargetUserTasks(String task_id,String process_def_deploymentId, Map<String, Object> variables, Consumer<ProcessDefElement> flowHandler){
//		String taskDefinitionKey;
//		String definitionId;
//		if (task_id == null) {
//			ProcessDef definition = processDefinitionService.getProcessDefinition(process_def_deploymentId);
//			taskDefinitionKey = processDefinitionService.getElementsOnDefinition(definition.getId()).get(ProcessDefElement.ProcessDefElementType.START_NODE).get(0).getId();
//			definitionId = definition.getId();
//		} else {
//			Task task = taskService.createTaskQuery().taskId(task_id).singleResult();
//			definitionId = task.getProcessDefinitionId();
//			taskDefinitionKey = task.getTaskDefinitionKey();
//		}
//		BpmnModel model = repositoryService.getBpmnModel(definitionId);
//		if (model.getProcesses() != null && model.getProcesses().size() > 0) {
//		     	FlowElement element = model.getProcesses().get(0).getFlowElement(taskDefinitionKey, true);
//				this.walkToTargetUserTasks(element, element, variables, flowHandler);
//		}
	}

	private void walkToTargetUserTasks(FlowElement startElement, FlowElement currentElement, Map<String, Object> variables, Consumer<ProcessActivityObject> activityHandler){
//		if (currentElement == null) {
//			return;
//		}
//		if(!startElement.getId().equals(currentElement.getId())){
//			// accept the current element first
//			ProcessDefElement activityObject = null;
//			if(currentElement instanceof UserTask){
//				UserTask nextUserTask = (UserTask) currentElement;
//				activityObject = new TaskDef();
//				TaskDef taskWrapper = (TaskDef)activityObject;
//				taskWrapper.setType(ProcessElementType.USER_TASK);
//				taskWrapper.setCandidateUserIds(nextUserTask.getCandidateUsers());
//				taskWrapper.setCandidateGroupIds(nextUserTask.getCandidateGroups());
//			}else if(currentElement instanceof ExclusiveGateway){
//				activityObject = new ProcessDefElement();
//				activityObject.setType(ProcessElementType.EXCLUSIVE_GATEWAY);
//			}else if(currentElement instanceof ParallelGateway){
//				activityObject = new ProcessDefElement();
//				activityObject.setType(ProcessElementType.PARALLEL_GATEWAY);
//			}else if(currentElement instanceof EndEvent){
//				activityObject = new ProcessDefElement();
//				activityObject.setType(ProcessElementType.END_NODE);
//			}
//            else if(currentElement instanceof StartEvent){
//                activityObject = new ProcessDefElement();
//                activityObject.setType(ProcessElementType.START_NODE);
//            }
//			activityObject.setDefinitionId(currentElement.getId());
//			activityObject.setDocumentation(currentElement.getDocumentation());
//			activityObject.setName(currentElement.getName());
//			flowHandler.accept(activityObject);
//		}
//		//  ?????????????????????????????????
//		if (currentElement instanceof StartEvent) {
//			StartEvent startEvent = (StartEvent) currentElement;
//			if (startElement.getId().equals(currentElement.getId())) {
//				startEvent.getOutgoingFlows().forEach(flow -> {
//					this.walkToTargetUserTasks(startElement, flow.getTargetFlowElement(), variables, flowHandler);
//				});
//			}
//		}
//		if (currentElement instanceof UserTask){
//			UserTask userTask = (UserTask) currentElement;
//			if(startElement.getId().equals(currentElement.getId())){
//				userTask.getOutgoingFlows().forEach(flow -> {
//					this.walkToTargetUserTasks(startElement, flow.getTargetFlowElement(), variables, flowHandler);
//				});
//			}
//			// ???????????????usertask?????????????????????startelement????????????????????????????????????
//		}else if (currentElement instanceof ParallelGateway) {
//			ParallelGateway gateway = (ParallelGateway) currentElement;
//			gateway.getOutgoingFlows().forEach(flow -> {
//				this.walkToTargetUserTasks(startElement, flow.getTargetFlowElement(), variables,module_attrs, flowHandler);
//			});
//		}else if (currentElement instanceof ExclusiveGateway) {
//			// ????????????????????????????????????????????????????????????????????????
//			// ?????????????????????????????????
//			ExclusiveGateway gateway = (ExclusiveGateway) currentElement;
//			SequenceFlow targetFlow = null;
//			SequenceFlow defaultFlow = null;
//			for (SequenceFlow flow : gateway.getOutgoingFlows()) {
//				if (flow.getId().equals(gateway.getDefaultFlow())) {
//					defaultFlow = flow;
//				}
//				if (targetFlow == null && flow.getConditionExpression() != null
//						&& flow.getConditionExpression().length() > 0) {
//					ExpressionFactory factory = new ExpressionFactoryImpl();
//					SimpleContext context = new SimpleContext();
//					if (variables != null && !variables.isEmpty()) {
//						variables.forEach((key, value) -> {
//							context.setVariable(key, factory.createValueExpression(value, value.getClass()));
//						});
//					}
//					ValueExpression e = factory.createValueExpression(context, flow.getConditionExpression(),
//							boolean.class);
//					try {
//						if ((Boolean) e.getValue(context)) {
//							targetFlow = flow;
//						} else {
//						}
//					} catch (PropertyNotFoundException exception) {
//                        //Cannot find property
////						String message = exception.getMessage().trim().replaceAll(" ", "");
////						ArrayList<JsonNode> lists = Lists.newArrayList(module_attrs.elements());
////						WorkflowDefModuleAttr attr = new WorkflowDefModuleAttr();
////						for (JsonNode jsonNode : lists) {
////							String name = jsonNode.get("name").asText();
////							if (name.equals(message.substring(18, message.length()))) {
////								attr = JsonUtil.toObject(jsonNode.toString(), WorkflowDefModuleAttr.class);
////								break;
////							}
////						}
////						if (null != attr.getDisplayname() && attr.getDisplayname().length() > 0) {
////							throw new ProcessRuntimeException("???????????????????????????????????????????????????" + attr.getDisplayname() + "???????????????");
////						} else {
////							throw new ProcessRuntimeException("???????????????????????????????????????????????????" + attr.getName() + "???????????????");
////						}
//					}
//				}
//			}
//			if (targetFlow == null) {
//				// ?????????????????????
//				if (defaultFlow != null) {
//					// ??????????????????
//					this.walkToTargetUserTasks(startElement, defaultFlow.getTargetFlowElement(), variables,flowHandler);
//				}
//			} else {
//				this.walkToTargetUserTasks(startElement, targetFlow.getTargetFlowElement(), variables,flowHandler);
//			}
//		}
	}

	private List<UserTask> getPreviousUserTaskFromElement(boolean recursiveCall, FlowElement element) {
		List<UserTask> previousElements = new ArrayList<>();
		if (element.getClass().equals(UserTask.class)) {
			UserTask userTask = (UserTask) element;
			if (!recursiveCall) {
				// ????????????????????????,??????????????????
				userTask.getIncomingFlows().forEach(flow -> {
					previousElements.addAll(this.getPreviousUserTaskFromElement(true, flow.getSourceFlowElement()));
				});
			} else {
				previousElements.add(userTask);
			}
		} else if (element.getClass().equals(EndEvent.class)) {
		} else if (element.getClass().equals(StartEvent.class)) {
			// ????????????????????????
		} else if (element.getClass().equals(ParallelGateway.class)) {
			ParallelGateway gateway = (ParallelGateway) element;
			gateway.getIncomingFlows().forEach(flow -> {
				previousElements.addAll(this.getPreviousUserTaskFromElement(true, flow.getSourceFlowElement()));
			});
		} else if (element.getClass().equals(ExclusiveGateway.class)) {
			// ????????????????????????????????????????????????????????????????????????
			// ?????????????????????????????????
			ExclusiveGateway gateway = (ExclusiveGateway) element;
			gateway.getIncomingFlows().forEach(flow -> {
				previousElements.addAll(this.getPreviousUserTaskFromElement(true, flow.getSourceFlowElement()));
			});
		}
		return previousElements;
	}

	private @Nullable SequenceFlow decideWhichFlowPassedThru(List<SequenceFlow> incomingFlows,
			List<HistoricActivityInstance> historyActivities,Map<String,Object> variables) {
		// ?????????????????????????????????????????????????????????
		for (HistoricActivityInstance activity : historyActivities) {
			List<SequenceFlow> sourceElements = incomingFlows.stream()
					.filter(item -> item.getSourceFlowElement().getId().equals(activity.getActivityId()))
					.collect(Collectors.toList());
			if (!sourceElements.isEmpty()) {
                if (sourceElements.size() > 1) {
                    for (SequenceFlow seq : sourceElements) {
                        if (seq.getSourceRef().equals("start")) {
                            return seq;
                        }
                        else {
                            ExpressionFactory factory = new ExpressionFactoryImpl();
                            SimpleContext context = new SimpleContext();
                            if (variables != null && !variables.isEmpty()) {
                                variables.forEach((key, value) -> {
                                    context.setVariable(key, factory.createValueExpression(value, value.getClass()));
                                });
                                ValueExpression e = factory.createValueExpression(context, seq.getConditionExpression(),
                                    boolean.class);
                                if ((Boolean) e.getValue(context)) {
                                    return seq;
                                }
                            }
                        }
                    }
                } else {
                    return sourceElements.get(0);
                }
			}
		}
		return null;
	}

	private List<UserTask> getUserTasksRejectBackTo(boolean recursiveCall, FlowElement element,
			List<HistoricActivityInstance> historyActivities,Map variables) {
		List<UserTask> previousElements = new ArrayList<>();
		if (element.getClass().equals(UserTask.class)) {
			UserTask userTask = (UserTask) element;
			if (!recursiveCall) {
				// ?????????
				SequenceFlow passedFlow = this.decideWhichFlowPassedThru(userTask.getIncomingFlows(),
						historyActivities,variables);
				if (passedFlow!=null) {
					previousElements.addAll(this.getUserTasksRejectBackTo(true,
							passedFlow.getSourceFlowElement(), historyActivities,variables));
				}
			} else {
				previousElements.add(userTask);
			}
		} else if (element.getClass().equals(EndEvent.class)) {
		} else if (element.getClass().equals(StartEvent.class)) {
			// ????????????????????????
		} else if (element.getClass().equals(ParallelGateway.class)) {
			ParallelGateway gateway = (ParallelGateway) element;
			gateway.getIncomingFlows().forEach(flow -> {
				previousElements.addAll(this.getPreviousUserTaskFromElement(true, flow.getSourceFlowElement()));
			});
		} else if (element.getClass().equals(ExclusiveGateway.class)) {
			// ????????????????????????????????????????????????????????????????????????
			// ?????????????????????????????????
			ExclusiveGateway gateway = (ExclusiveGateway) element;
			SequenceFlow passedFlow = this.decideWhichFlowPassedThru(gateway.getIncomingFlows(), historyActivities,variables);
			if (passedFlow!=null) {
				previousElements.addAll(this.getUserTasksRejectBackTo(true, passedFlow.getSourceFlowElement(),
						historyActivities,variables));
			}
		}
		return previousElements;
	}

	private List<UserTask> getUserTasksRejectBackTo(String processId, Task task,Map variables) {
		Process processModel = processDefinitionService.getProcessModel(task.getProcessDefinitionId());
		// ??????????????????????????????task??????
		FlowElement userTask = processModel.getFlowElement(task.getTaskDefinitionKey(), true);
		List<HistoricActivityInstance> historyActivities = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processId).orderByHistoricActivityInstanceStartTime().desc().list();
		// ??????task?????????????????????
		return this.getUserTasksRejectBackTo(false, userTask, historyActivities,variables);
	}

	private boolean findTargetElementBackwardFromElement(FlowElement targetElement, FlowElement element,
			List<HistoricActivityInstance> historyActivities, Map variables) {
		if (element.getClass().equals(UserTask.class)) {
			UserTask userTask = (UserTask) element;
			if (userTask.getId().equals(targetElement.getId())) {
				return true;
			} else {
				SequenceFlow passedFlow = this.decideWhichFlowPassedThru(userTask.getIncomingFlows(),
						historyActivities,variables);
				if (passedFlow!=null) {
					return this.findTargetElementBackwardFromElement(targetElement, passedFlow.getSourceFlowElement(),
							historyActivities,variables);
				}
			}
		} else if (element.getClass().equals(EndEvent.class)) {

		} else if (element.getClass().equals(StartEvent.class)) {
			// ????????????????????????
			return false;
		} else if (element.getClass().equals(ParallelGateway.class)) {
			ParallelGateway gateway = (ParallelGateway) element;
			if (!gateway.getIncomingFlows().isEmpty()) {
				return this.findTargetElementBackwardFromElement(targetElement, gateway.getIncomingFlows().get(0).getSourceFlowElement(),
						historyActivities,variables);
			}
		} else if (element.getClass().equals(ExclusiveGateway.class)) {
			// ????????????????????????????????????????????????????????????????????????
			// ?????????????????????????????????
			ExclusiveGateway gateway = (ExclusiveGateway) element;
			SequenceFlow passedFlow = this.decideWhichFlowPassedThru(gateway.getIncomingFlows(), historyActivities,variables);
			if (passedFlow!=null) {
				return this.findTargetElementBackwardFromElement(targetElement, passedFlow.getSourceFlowElement(),
						historyActivities,variables);
			}
		}
		return false;
	}

	@Override
	public TaskObject getRuntimePreviousUserTask(String taskId) {
		// ???act_hi_taskinst?????????????????????execution?????????????????????task???????????????taskid????????????????????????
 	  	// ?????????????????????????????????????????????task??????????????????execution???????????????????????????????????????execution
		TaskObject task = processMapper.getRuntimePreviousExecutedTaskOnSameExecution(taskId);
		if(task == null){
			task = processMapper.getRuntimePreviousExecutedTaskOnIgnoreExecution(taskId);
		}
		/*if(task == null){
			throw new ProcessRuntimeException("?????????????????????????????????");
		}*/
		return task;
	}

	@Override
	public void setTimerTriggerHandler(Consumer<TaskTimer> timerTriggeredHandler) {
		this.timerTriggeredHandler = timerTriggeredHandler;
	}

	@Override
	public void timerEventTriggered(ActivitiEvent event) {
		ActivitiEntityEventImpl activitiEntityEvent = (ActivitiEntityEventImpl) event;
		JobEntity jobEntity = (JobEntity) activitiEntityEvent.getEntity();
		log.info("timer event triggered timerJobId: {}", jobEntity.getId());
		if (this.timerTriggeredHandler == null) {
			throw new ProcessRuntimeException("Timer Trigger handler does not set");
		}
		ExecutionEntity job = (ExecutionEntity) runtimeService.createExecutionQuery()
				.executionId(event.getExecutionId()).singleResult();

		BoundaryEvent element = (BoundaryEvent) job.getCurrentFlowElement();

		List<Task> tasks = taskService.createTaskQuery().processInstanceId(event.getProcessInstanceId()).list();
		log.info("searching for owner task for timer event: {}" + event);
		if (CollectionUtil.isNotEmpty(tasks)) {
			List<Task> targetTasks = tasks.stream()
					.filter(task -> task.getTaskDefinitionKey().equals(element.getAttachedToRefId()))
					.collect(Collectors.toList());
			if (CollectionUtil.isNotEmpty(targetTasks)) {
				// ??????????????????????????????????????????????????????????????????
				TaskObject taskObject = TaskObject.of(targetTasks.get(0));
				TaskTimer timer = new TaskTimer();
				timer.setDefinitionId(element.getId());
				timer.setTask(taskObject);
				timer.setJobId(jobEntity.getId());
				log.info("found owner task???{} for timer event: {}", taskObject, event);
				this.timerTriggeredHandler.accept(timer);
			} else {
				log.error("there is no active task {} on process {}", event.getProcessInstanceId(), element.getAttachedToRefId());
			}
		} else {
			log.error("there is no active task on process {}", event.getProcessInstanceId());
		}
	}
	
	@Override
	public @Nullable String getIncomingFlowDocumentation(String task_id,Map<String,Object> variables) {
		TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(task_id).singleResult();
		Process processModel = processDefinitionService.getProcessModel(task.getProcessDefinitionId());
		// ??????????????????????????????task??????
		UserTask userTask = (UserTask)processModel.getFlowElement(task.getTaskDefinitionKey(), true);
		List<HistoricActivityInstance> historyActivities = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).orderByHistoricActivityInstanceStartTime().desc().list();
		// ???????????????
		SequenceFlow passedFlow = this.decideWhichFlowPassedThru(userTask.getIncomingFlows(),
				historyActivities,variables);
		if(passedFlow==null){
			return null;
		}
		return passedFlow.getDocumentation();
	}

}

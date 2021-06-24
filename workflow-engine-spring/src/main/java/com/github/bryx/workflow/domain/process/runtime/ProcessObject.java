package com.github.bryx.workflow.domain.process.runtime;

import lombok.Data;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class ProcessObject {

	public enum Status{
		RUNTIME,
		HISTORIC
	}

	public ProcessObject(){
	}

	public static ProcessObject of(ProcessInstance processInstance){
		ProcessObject processObject = new ProcessObject();
		BeanUtils.copyProperties(processInstance, processObject);
		processObject.setDefinitionId(processInstance.getProcessDefinitionId());
		return processObject;
	}
	
	public ProcessObject(String id){
		this.id = id;
	}
	private String id;
	private String businessKey;
	private String definitionId;
	private String deploymentId;
	private Date startTime;
	private Date endTime;
	private String name;
	private Integer version;
	private Status status;

	/**
	 * current tasks
	 */
	private List<TaskObject> tasks;

	/**
	 * @return tasks on parralel gateway
	 */
	public List<TaskObject> getParralelGateWayTasks() {
		return tasks.stream().filter(task -> TaskObject.TaskObjectState.WAITING_ON_PARALLEL_GATEWAY.equals(task.getState()))
				.collect(Collectors.toList());
	}

	/**
	 *
	 * @param taskName
	 * @return tasks on parralel gateway and taskname
	 */
	public List<TaskObject> getParralelGateWayTasks(String taskName) {
		return tasks.stream().filter(task -> TaskObject.TaskObjectState.WAITING_ON_PARALLEL_GATEWAY.equals(task.getState())
				&& task.getName().equals(taskName)).collect(Collectors.toList());
	}

	/**
	 *
	 * @return tasks not on parralel gateway
	 */
	public List<TaskObject> getNormalTasks() {
		return tasks.stream().filter(task -> TaskObject.TaskObjectState.NORMAL.equals(task.getState())).collect(Collectors.toList());
	}

	public List<TaskObject> getNormalTasks(String taskName) {
		return tasks.stream()
				.filter(task -> TaskObject.TaskObjectState.NORMAL.equals(task.getState()) && task.getName().equals(taskName))
				.collect(Collectors.toList());
	}
	
}

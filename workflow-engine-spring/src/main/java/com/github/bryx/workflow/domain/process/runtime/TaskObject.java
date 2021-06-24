package com.github.bryx.workflow.domain.process.runtime;

import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.activiti.engine.task.Task;

import java.util.*;
import java.util.stream.Collectors;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskObject extends ProcessActivityObject {
	public final static String STATUS_TASK_FINISH = "结束";

	public enum TaskObjectState {
		WAITING_ON_PARALLEL_GATEWAY,
		NORMAL
	}
	List<TaskObjectAssignee> assignees = Lists.newArrayList();

	public List<String> getAssignedUserIds(){
		return this.assignees.stream().filter(item->item.getAssigneeType().equals(TaskObjectAssignee.AssigneeType.USER)).map(TaskObjectAssignee::getAssigneeId).collect(Collectors.toList());
	}

	public List<String> getAssignedGroupIds(){
		return this.assignees.stream().filter(item->item.getAssigneeType().equals(TaskObjectAssignee.AssigneeType.GROUP)).map(TaskObjectAssignee::getAssigneeId).collect(Collectors.toList());
	}

	private Date claimTime;

	private String executorId;
	private TaskObjectState state;
	private Long durationInMillis;
	
	private List<TaskTimer> timers;

	public static TaskObject of(Task task){
		return TaskObject.builder().type(ProcessActivityObject.ProcessInstActivityType.USER_TASK)
				.id(task.getId())
				.definitionId(task.getTaskDefinitionKey())
				.documentation(task.getDescription())
				.name(task.getName())
				.startTime(task.getCreateTime())
				.executionId(task.getExecutionId())
				.processId(task.getProcessInstanceId())
				.processDefinitionId(task.getProcessDefinitionId()).build();
	}

	public boolean userExecutableOnTask(String userId, List<String> groups){
		Set<String> assignedUsersAndGroups = new HashSet<String>(this.getAssignedUserIds());
		assignedUsersAndGroups.addAll(this.getAssignedGroupIds());
		if (!assignedUsersAndGroups.isEmpty()) {
			// 如果任务上有受理
			int size = assignedUsersAndGroups.size();
			assignedUsersAndGroups.remove(userId);
			if(groups!=null){
				assignedUsersAndGroups.removeAll(groups);
			}
			if (assignedUsersAndGroups.size() != size) {
				// 说明用户的id或者组在受理中
				return true;
			}
		}
		return false;
	}

    public boolean groupExecutableOnTask(List<String> assignedGroups, List<String> departmentIds) {
        if (!assignedGroups.isEmpty()) {
            assignedGroups.retainAll(departmentIds);
            if (!assignedGroups.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
    	return this.getId() + " name:" +this.getName() ;
    }


}

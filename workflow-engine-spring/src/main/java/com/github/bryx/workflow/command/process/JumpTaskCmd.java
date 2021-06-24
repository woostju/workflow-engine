package com.github.bryx.workflow.command.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JumpTaskCmd extends NeedsActiveTaskCmd<Void> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Collection<String> deletedTaskIds;
	protected Collection<String> deletedExecutionIds;
	protected List<UserTask> targetUserTasks;
	protected String executorId;
	
	private final static Logger logger = LoggerFactory.getLogger(JumpTaskCmd.class);

	public JumpTaskCmd(String taskId, String executorId, Collection<String> deletedTaskIds, Collection<String> deletedExecutionIds, List<UserTask> targetUserTasks) {
		super(taskId);
		this.taskId = taskId;
		this.deletedTaskIds = deletedTaskIds;
		this.deletedExecutionIds = deletedExecutionIds;
		this.targetUserTasks = targetUserTasks;
		this.executorId = executorId;
	}

	@Override
	protected Void execute(CommandContext commandContext, TaskEntity task) {
		commandContext.getTaskEntityManager().changeTaskAssignee(task, executorId);
		// leave task first
		// Delete all child executions
		ExecutionEntity execution = commandContext.getExecutionEntityManager()
				.findById(task.getExecutionId());
		ExecutionEntity parentExecution = execution.getParentId() != null ? execution.getParent() : execution;
		
		deletedExecutionIds.forEach(executionIds->{
			Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager()
					.findChildExecutionsByParentExecutionId(executionIds);
			for (ExecutionEntity childExecution : childExecutions) {
				commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(childExecution, null, false);
			}
			commandContext.getExecutionEntityManager().delete(executionIds);
		});
		// 删除task前，对删除的task进行排序，确保最后一个删除的当前节点
		// 这样在获取上一个执行节点时，是当前驳回的这个节点
		List<String> taskIdToDelete = new ArrayList<>(deletedTaskIds);
		taskIdToDelete.remove(taskId);
		taskIdToDelete.add(taskId);
		taskIdToDelete.forEach(taskId -> {
			// 删除历史
			TaskEntity taskEntity = commandContext.getTaskEntityManager().findById(taskId);
			commandContext.getTaskEntityManager().deleteTask(taskEntity, "驳回", false, false);
			logger.debug("delete task :"+taskId+" name:"+taskEntity.getName());
		});

		// create task and execution
		this.targetUserTasks.forEach(targetUserTask -> {
			ExecutionEntity createdExecution = commandContext.getExecutionEntityManager()
					.createChildExecution(parentExecution);
			createdExecution.setCurrentFlowElement(targetUserTask);
			Context.getAgenda().planContinueProcessOperation(createdExecution);
		});
		return null;
	}
}
package com.github.bryx.workflow.command.process;

import java.util.Collection;
import java.util.Date;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Comment;

public class CloseProcessCmd implements Command<Comment> {
	protected Collection<TaskEntity> deletedTasks;
	protected String executorId;
	protected EndEvent endEvent;

	public CloseProcessCmd(String executorId, Collection<TaskEntity> deletedTasks, EndEvent endEvent) {
		this.deletedTasks = deletedTasks;
		this.executorId = executorId;
		this.endEvent = endEvent;
	}

	@Override
	public Comment execute(CommandContext commandContext) {
		
		
		IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
		
		ExecutionEntity execution = deletedTasks.iterator().next().getExecution();
	    
	    String processDefinitionId = execution.getProcessDefinitionId();
	    String processInstanceId = execution.getProcessInstanceId();
	      
	    HistoricActivityInstanceEntity historicActivityInstance = commandContext.getHistoricActivityInstanceEntityManager().create();
	    historicActivityInstance.setId(idGenerator.getNextId());
	    historicActivityInstance.setProcessDefinitionId(processDefinitionId);
	    historicActivityInstance.setProcessInstanceId(processInstanceId);
	    historicActivityInstance.setExecutionId(execution.getId());
	    historicActivityInstance.setActivityId(this.endEvent.getId());
	    if (execution.getCurrentFlowElement() != null) {
	      historicActivityInstance.setActivityName(this.endEvent.getName());
	      historicActivityInstance.setActivityType("endEvent");
	    }
	    historicActivityInstance.setStartTime(new Date());
	    historicActivityInstance.setEndTime(new Date());
	 
	    // Inherit tenant id (if applicable)
	    if (execution.getTenantId() != null) {
	       historicActivityInstance.setTenantId(execution.getTenantId());
	    }
	    
	    commandContext.getHistoricActivityInstanceEntityManager().insert(historicActivityInstance);
		
		
		// leave task first
		// Delete all child executions
		deletedTasks.forEach(deletedTask -> {
			// Delete all child executions
			commandContext.getTaskEntityManager().changeTaskAssignee(deletedTask, this.executorId);
			
			Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager()
					.findChildExecutionsByParentExecutionId(deletedTask.getExecutionId());
			for (ExecutionEntity childExecution : childExecutions) {
				commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(childExecution, null, false);
			}
			commandContext.getExecutionEntityManager().delete(deletedTask.getExecutionId());
			// 删除历史
			commandContext.getTaskEntityManager().deleteTask(deletedTask, "关闭", false, false);
		});

		return null;
	}
}
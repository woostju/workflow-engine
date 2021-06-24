package com.github.bryx.workflow.command.process;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.task.Comment;

public class DeleteExecutionCmd implements Command<Comment> {
	protected String executorId;

	public DeleteExecutionCmd(String executorId) {
		this.executorId = executorId;
	}

	@Override
	public Comment execute(CommandContext commandContext) {
		commandContext.getExecutionEntityManager().delete(executorId);
		return null;
	}
}
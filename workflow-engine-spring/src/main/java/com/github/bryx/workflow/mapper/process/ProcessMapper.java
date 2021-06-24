package com.github.bryx.workflow.mapper.process;


import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;



@Mapper
public interface ProcessMapper {

	List<TaskObject> getTasksByProcessId(String processId);

	List<TaskObject> getTasksByIds(List<String> taskIds);

	TaskObject getRuntimePreviousExecutedTaskOnSameExecution(String taskId);

	TaskObject getRuntimePreviousExecutedTaskOnIgnoreExecution(String taskId);


}

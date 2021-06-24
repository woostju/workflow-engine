package com.github.bryx.workflow.service.process;

import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDefElement;
import com.github.bryx.workflow.domain.process.buildtime.SequenceDef;
import com.github.bryx.workflow.domain.process.buildtime.TaskDef;
import org.activiti.bpmn.model.Process;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 
 * @author James Wu
 *
 * manage build-time process
 *
 */
public interface ProcessDefinitionService {

	/**
	 * @param deploymentId
	 * @return
	 */
	public ProcessDef getProcessDefinitionByDeploymentId(String deploymentId);

	/**
	 * @param definitionId
	 * @return
	 */
	public ProcessDef getProcessDefinitionById(String definitionId);

	/**
	 * get all the elements in process definition, as task def, start node
	 * @param definitionId
	 * @return
	 */
	public Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> getElementsOnDefinition(String definitionId);

	/**
	 *
	 * @param processDefinitionId
	 * @param sequenceDefHandler
	 * @param userTaskHandler
	 *
	 * 验证流程定义
	 * 1个开始节点
	 * 1个结束节点
	 * 至少2个任务节点
	 * 一个任务必须是1个出口
	 *
	 * 任务上的配置由sequenceFlowHandler负责
	 * 线上的配置由userTaskHandler负责
	 *
	 */
	public Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> getElementsOnDefinition(String processDefinitionId, Consumer<SequenceDef> sequenceDefHandler, Consumer<TaskDef> userTaskHandler);

	/**
	 * delete process def
	 * @param deploymentId
	 */
	public void deleteProcessDefinition(String deploymentId);

	/**
	 * deploy process definition with file input stream
	 * @param resourceName
	 * @param stream
	 * @return
	 */
	public ProcessDef deployProcessDefinition(String resourceName, InputStream stream);

	/**
	 * deploy process definition with file content
	 * @param resourceName
	 * @param bpmnFileContent
	 * @return
	 */
	public ProcessDef deployProcessDefinition(String resourceName, String bpmnFileContent);

	public Process getProcessModel(String processDefinitionId);

}

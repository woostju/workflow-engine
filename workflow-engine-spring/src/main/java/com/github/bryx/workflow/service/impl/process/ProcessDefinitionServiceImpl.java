package com.github.bryx.workflow.service.impl.process;

import com.github.bryx.workflow.exception.ProcessRuntimeException;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDefElement;
import com.github.bryx.workflow.domain.process.buildtime.SequenceDef;
import com.github.bryx.workflow.domain.process.buildtime.TaskDef;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
import com.github.bryx.workflow.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @author jameswu
 *
 *
 */

@Service
@Slf4j
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

	@Autowired
	RepositoryService repositoryService;

	@Override
	public ProcessDef getProcessDefinitionByDeploymentId(String deploymentId) {
		ProcessDef definition = new ProcessDef();
		ProcessDefinition result = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
		BeanUtils.copyProperties(result, definition);
		return definition;
	}

	@Override
	public ProcessDef getProcessDefinitionById(String definitionId) {
		ProcessDef definition = new ProcessDef();
		ProcessDefinition result = repositoryService.createProcessDefinitionQuery().processDefinitionId(definitionId).singleResult();
		BeanUtils.copyProperties(result, definition);
		return definition;
	}

	@Override
	public Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> getElementsOnDefinition(String definitionId){
		Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> result = Maps.newHashMap();
		Process processModel = this.getProcessModel(definitionId);
		for(FlowElement element : processModel.getFlowElements()){
			ProcessDefElement processElement = ProcessDefElement.of(element);
			if (processElement == null){
				continue;
			}
			processElement.setProcessDefId(definitionId);
			if (!result.containsKey(processElement.getType())){
				result.put(processElement.getType(), Lists.newArrayList());
			}
			result.get(processElement.getType()).add(processElement);
			if (processElement.getType().equals(ProcessDefElement.ProcessDefElementType.START_NODE)){
				StartEvent event = (StartEvent)element;
				if(!event.getOutgoingFlows().isEmpty()){
					result.put(ProcessDefElement.ProcessDefElementType.FIRST_USER_TASK, Lists.newArrayList(ProcessDefElement.of(event.getOutgoingFlows().get(0).getTargetFlowElement())));
				}
			}
		}
		return result;
	}


	@Override
	@Transactional
	public ProcessDef deployProcessDefinition(String resourceName, InputStream stream) {
		return this.deployProcessDefinition(resourceName, null, stream);
	}

	@Override
	@Transactional
	public ProcessDef deployProcessDefinition(String resourceName, String bpmnFileContent) {
		return this.deployProcessDefinition(resourceName, bpmnFileContent, null);
	}

	private ProcessDef deployProcessDefinition(String resourceName, String bpmnFileContent,  InputStream stream) {
		if (!StringUtil.endsWith(resourceName, ".bpmn20.xml")){
			throw new ProcessRuntimeException("resource name must end with .bpmn20.xml");
		}
		DeploymentBuilder builder = repositoryService.createDeployment().name(resourceName);
		builder.disableBpmnValidation();
		if (StringUtils.isNotEmpty(bpmnFileContent)){
			builder = builder.addString(resourceName, bpmnFileContent);
		}else{
			builder = builder.addInputStream(resourceName, stream);
		}
		Deployment deployment = builder.deploy();
		Validate.notNull(deployment, "部署流程定义出错");
		ProcessDefinition processDef = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		Validate.notNull(processDef, "部署流程定义出错");
		ProcessDef def = new ProcessDef();
		BeanUtils.copyProperties(processDef, def);
		return def;
	}

	@Override
	public Process getProcessModel(String processDefinitionId){
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
		if (CollectionUtil.isNotEmpty(bpmnModel.getProcesses())){
			return bpmnModel.getProcesses().get(0);
		}else{
			throw new ProcessRuntimeException("unable to find process model");
		}
	}

	@Override
	public Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> getElementsOnDefinition(String processDefinitionId, Consumer<SequenceDef> sequenceDefHandler, Consumer<TaskDef> userTaskHandler) {
		// track all element id and count
		// avoid handle one element twice
		ProcessModelSummary moduleSummary =new ProcessModelSummary();
		// save all elements in this map
		Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> elements = Maps.newHashMap();
		Process processModel = this.getProcessModel(processDefinitionId);
		FlowElement start = processModel.getInitialFlowElement();
		// handle flow element recursively
		this.handleFlowElment(start, moduleSummary, elements,  sequenceDefHandler, userTaskHandler);
		// validate process definition element
		if(moduleSummary.getStartEventCount()!=1){
			throw new RuntimeException("只能有一个开始元素");
		}else if(moduleSummary.getUserTaskCount()<2){
			throw new RuntimeException("至少有两个节点元素");
		}
		return elements;
	}
	
	private void handleFlowElment(FlowElement element, ProcessModelSummary moduleSummary, Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> elementsTracker, Consumer<SequenceDef> sequenceDefHandler, Consumer<TaskDef> userTaskHandler){
		if(moduleSummary.getElementIds().contains(element.getId())){
			// 跳过已经验证过的元素
			return;
		}else{
			moduleSummary.getElementIds().add(element.getId());
			ProcessDefElement defElement = ProcessDefElement.of(element);
			if (defElement == null){
				return;
			}
			if (!elementsTracker.containsKey(defElement.getType())){
				elementsTracker.put(defElement.getType(), Lists.newArrayList());
			}
			elementsTracker.get(defElement.getType()).add(defElement);
			if (ProcessDefElement.ProcessDefElementType.SEQUENCE_FLOW.equals(defElement.getType()) && sequenceDefHandler!=null){
				sequenceDefHandler.accept((SequenceDef) defElement);
			}else if(ProcessDefElement.ProcessDefElementType.USER_TASK.equals(defElement.getType()) && userTaskHandler!=null){
				userTaskHandler.accept((TaskDef)defElement);
			}
		}
		if(element instanceof StartEvent){
			moduleSummary.setStartEventCount(moduleSummary.getStartEventCount()+1);
			StartEvent startEvent = (StartEvent)element;
			Validate.notEmpty(startEvent.getOutgoingFlows(), "开始元素必须有目标节点");
			if(startEvent.getOutgoingFlows().size()!=1){
				throw new RuntimeException("开始元素不能有两条出线");
			}
			startEvent.getOutgoingFlows().forEach(flow->{
				this.handleFlowElment(flow, moduleSummary, elementsTracker, sequenceDefHandler, userTaskHandler);
			});
		}else if(element instanceof ExclusiveGateway){
			ExclusiveGateway exclusiveGateway = (ExclusiveGateway)element;
			Validate.notEmpty(exclusiveGateway.getOutgoingFlows(), "判断元素【"+exclusiveGateway.getName()+"】必须有目标节点");
			if(exclusiveGateway.getOutgoingFlows().size()<2){
				throw new RuntimeException("判断元素【"+exclusiveGateway.getName()+"】必须有两根出线");
			}
			exclusiveGateway.getOutgoingFlows().forEach(flow->{
				this.handleFlowElment(flow, moduleSummary, elementsTracker, sequenceDefHandler, userTaskHandler );
			});
		}else if(element instanceof ParallelGateway){
			ParallelGateway gateway = (ParallelGateway)element;
			Validate.notEmpty(gateway.getOutgoingFlows(), "并行网关元素【"+gateway.getName()+"】必须有目标节点");
			gateway.getOutgoingFlows().forEach(flow->{
				this.handleFlowElment(flow, moduleSummary, elementsTracker, sequenceDefHandler, userTaskHandler );
			});
		}else if(element instanceof UserTask){
			moduleSummary.setUserTaskCount(moduleSummary.getUserTaskCount()+1);
			UserTask task = (UserTask)element;
			Validate.notEmpty(task.getOutgoingFlows(), "节点元素【"+task.getName()+"】必须有目标节点");
			if(task.getOutgoingFlows().size()!=1){
				throw new RuntimeException("节点元素【"+task.getName()+"】不能有两条出线");
			}
			task.getOutgoingFlows().forEach(flow->{
				this.handleFlowElment(flow, moduleSummary, elementsTracker, sequenceDefHandler, userTaskHandler);
			});
		}else if(element instanceof EndEvent){
			moduleSummary.setEndEventCount(moduleSummary.getEndEventCount()+1);
		}else if(element instanceof SequenceFlow){
			SequenceFlow flow = (SequenceFlow)element;
			this.handleFlowElment(flow.getTargetFlowElement(), moduleSummary, elementsTracker, sequenceDefHandler, userTaskHandler);
		}else{
			throw new RuntimeException("流程定义中有未知类型元素");
		}
	}
	
	@Override
	public void deleteProcessDefinition(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId);
	}

}

@Data
class ProcessModelSummary {
	private int userTaskCount = 0;
	private int startEventCount = 0;
	private int endEventCount = 0;
	private Set<String> elementIds = new HashSet<>();
}
package com.github.bryx.workflow.service;

import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.dto.buildtime.*;

/**
 *
 * 先创建一个流程定义草稿，然后做编辑，编辑完成后，发布流程定义
 * 1：每次发布流程定义都会创建新的流程定义版本
 * 2：新发起的流程实例将使用最新的流程定义版本
 * 3：已经发起的流程实例继续在其对应的流程定义版本上运行
 *
 * 不同版本共享一个流程定义的id，seq，name，createTime等基础属性
 * 不同版本有各自的流程模型及表单
 *
 *  @Author jameswu
 *  @Date 2021/6/2
 **/
public interface WorkflowBuildTimeService {

    /**
     * 创建流程定义草稿
     * 如果没有指定版本，则创建流程定义及版本
     * @param createWorkflowDefDraftDto
     */
    public WorkflowDef createWorkflowDefDraft(CreateWorkflowDefDraftDto createWorkflowDefDraftDto);

    /**
     * 修改流程定义版本草稿
     * @param updateWorkflowDefDraftDto
     */
    public void updateWorkflowDefDraft(UpdateWorkflowDefDraftDto updateWorkflowDefDraftDto);

    /**
     * 修改流程定义版本草稿
     * @param updateWorkflowDefRevDto
     */
    public void updateWorkflowDefRev(UpdateWorkflowDefRevDto updateWorkflowDefRevDto);

    /**
     * 修改流程定义的基础信息
     * @param updateWorkflowDefDto
     */
    public void updateWorkflowDef(UpdateWorkflowDefDto updateWorkflowDefDto);

    /**
     * 通过草稿发布流程定义到最新版本
     * @param publishWorkflowDefDraftDto
     */
    public void publishWorkflowDefDraft(PublishWorkflowDefDraftDto publishWorkflowDefDraftDto);

    /**
     * 修改流程定义版本状态
     * @param changeWorkflowDefRevStatusDto
     */
    public void changeWorkflowDefRevStatus(ChangeWorkflowDefRevStatusDto changeWorkflowDefRevStatusDto);

    /**
     * 删除流程定义
     * @param workflowDefId
     */
    public void deleteWorkflowDef(String workflowDefId);

    /**
     * 彻底删除流程定义
     * @param workflowDefId
     */
    public void forceDeleteWorkflowDef(String workflowDefId);

    /**
     * 删除流程定义版本
     * @param workflowDefRevId
     */
    public void deleteWorkflowDefRev(String workflowDefRevId);



    public WorkflowBuildTimeQuery query();
}

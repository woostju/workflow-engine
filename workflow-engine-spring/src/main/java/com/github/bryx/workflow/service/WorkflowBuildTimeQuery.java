package com.github.bryx.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.dto.buildtime.QueryWorkflowDefDto;
import com.github.bryx.workflow.dto.buildtime.QueryWorkflowDefRevDto;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
public interface WorkflowBuildTimeQuery {
    /**
     * 查询流程定义下的版本列表
     * @param queryWorkflowDefDto
     * @return
     */
    public Page<WorkflowDef> queryWorkflowDefs(QueryWorkflowDefDto<WorkflowDef> queryWorkflowDefDto);


    /**
     * 查询流程定义下的版本列表
     * @param queryWorkflowDefRevDto
     * @return
     */
    public Page<WorkflowDefRev> queryWorkflowDefRevs(QueryWorkflowDefRevDto<WorkflowDefRev> queryWorkflowDefRevDto);

    /**
     * 获取最新流程定义
     */
    public WorkflowDef getWorkflowDefById(String workflowDefId);

    /**
     * 获取最新流程定义
     */
    public WorkflowDef getWorkflowDefByName(String workflowDefName);


    /**
     * 获取流程定义指定版本
     * @param workflowDefRevId
     * @return
     */
    public WorkflowDefRev getWorkflowDefRevById(String workflowDefRevId);


    /**
     * 获取最新的激活的流程定义版本
     * @param workflowDefId
     * @return
     */
    public WorkflowDefRev getLatestEnabledWorkflowDefRev(String workflowDefId);

    /**
     * 获取流程定义上的草稿
     * @param workflowDefId
     * @return
     */
    public WorkflowDefRev getWorkflowDefRevDraft(String workflowDefId);

}

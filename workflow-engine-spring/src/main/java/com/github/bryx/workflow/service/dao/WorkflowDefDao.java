package com.github.bryx.workflow.service.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.mapper.WorkflowDefMapper;
import org.springframework.stereotype.Service;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Service
public class WorkflowDefDao extends ServiceImpl<WorkflowDefMapper, WorkflowDef> {

    public void forceDeleteWorkflowDef(String workflowDefId) {
        this.baseMapper.forceDeleteWorkflowDef(workflowDefId);
    }
}

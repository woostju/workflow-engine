package com.github.bryx.workflow.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.domain.WorkflowObjectEntityRelation;
import com.github.bryx.workflow.dto.buildtime.QueryWorkflowDefDto;
import com.github.bryx.workflow.dto.buildtime.QueryWorkflowDefRevDto;
import com.github.bryx.workflow.service.WorkflowBuildTimeQuery;
import com.github.bryx.workflow.service.dao.WorkflowDefDao;
import com.github.bryx.workflow.service.dao.WorkflowDefRevDao;
import com.github.bryx.workflow.service.dao.WorkflowObjectEntityRelationDao;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author jameswu
 * @Date 2021/6/17
 **/
@Service
public class WorkflowBuildTimeQueryImpl implements WorkflowBuildTimeQuery {

    @Autowired
    WorkflowDefRevDao workflowDefRevDao;

    @Autowired
    WorkflowObjectEntityRelationDao workflowObjectEntityRelationDao;

    @Autowired
    WorkflowDefDao workflowDefDao;

    @Override
    public WorkflowDef getWorkflowDefById(String workflowDefId) {
        WorkflowDef workflowDef = workflowDefDao.getById(workflowDefId);
        Validate.notNull(workflowDef, "workflow def does not exist");
        return workflowDef;
    }

    @Override
    public WorkflowDef getWorkflowDefByName(String workflowDefName) {
        WorkflowDef workflowDef = workflowDefDao.lambdaQuery().eq(WorkflowDef::getName, workflowDefName).one();
        Validate.notNull(workflowDef, "workflow def [%s] does not exist", workflowDefName);
        return workflowDef;
    }

    @Override
    public WorkflowDefRev getLatestEnabledWorkflowDefRev(String workflowDefId) {
        List<WorkflowDefRev> workflowDefRevs = workflowDefRevDao.lambdaQuery()
                .and(object -> object.eq(WorkflowDefRev::getDefId, workflowDefId)
                        .eq(WorkflowDefRev::getStatus, WorkflowDefRev.WorkflowDefRevStatus.ENABLE)
                        .eq(WorkflowDefRev::getDeleted, false))
                .orderByDesc(WorkflowDefRev::getCreateTime)
                .page(new Page<WorkflowDefRev>(1, 1)).getRecords();
        if (CollectionsUtil.isNotEmpty(workflowDefRevs)){
            return workflowDefRevs.get(0);
        }
        return null;
    }

    @Override
    public WorkflowDefRev getWorkflowDefRevDraft(String workflowDefId) {
        List<WorkflowDefRev> drafts = workflowDefRevDao.lambdaQuery()
                .and(object -> object.eq(WorkflowDefRev::getDefId, workflowDefId)
                        .eq(WorkflowDefRev::getStatus, WorkflowDefRev.WorkflowDefRevStatus.DRAFT))
                .orderByDesc(WorkflowDefRev::getCreateTime).list();
        if (drafts.size() > 1) {
            throw new WorkflowRuntimeException("only one draft allow, please contact admin to fix db data");
        }
        Validate.notEmpty(drafts, "no draft on workflow def [%s]", workflowDefId);
        return drafts.get(0);
    }

    @Override
    public WorkflowDefRev getWorkflowDefRevById(String workflowDefRevId) {
        return workflowDefRevDao.getById(workflowDefRevId);
    }

    @Override
    public Page<WorkflowDefRev> queryWorkflowDefRevs(QueryWorkflowDefRevDto<WorkflowDefRev> queryWorkflowDefRevDto) {
        LambdaQueryChainWrapper<WorkflowDefRev> workflowDefRevLambdaQueryChainWrapper = workflowDefRevDao.lambdaQuery();
        if (queryWorkflowDefRevDto.getDefId() != null) {
            workflowDefRevLambdaQueryChainWrapper = workflowDefRevLambdaQueryChainWrapper.eq(WorkflowDefRev::getDefId, queryWorkflowDefRevDto.getDefId());
        }
        if (queryWorkflowDefRevDto.getDefRevId() != null) {
            workflowDefRevLambdaQueryChainWrapper = workflowDefRevLambdaQueryChainWrapper.eq(WorkflowDefRev::getId, queryWorkflowDefRevDto.getDefRevId());
        }
        if (CollectionsUtil.isNotEmpty(queryWorkflowDefRevDto.getStatuses())) {
            workflowDefRevLambdaQueryChainWrapper = workflowDefRevLambdaQueryChainWrapper.in(WorkflowDefRev::getStatus, queryWorkflowDefRevDto.getStatuses());
        }
        workflowDefRevLambdaQueryChainWrapper = workflowDefRevLambdaQueryChainWrapper.eq(WorkflowDefRev::getDeleted, queryWorkflowDefRevDto.getDeleted());
        workflowDefRevLambdaQueryChainWrapper = workflowDefRevLambdaQueryChainWrapper.orderByDesc(WorkflowDefRev::getCreateTime);
        Page<WorkflowDefRev> page = workflowDefRevLambdaQueryChainWrapper.page(queryWorkflowDefRevDto.page());
        return page;
    }

    @Override
    public Page<WorkflowDef> queryWorkflowDefs(QueryWorkflowDefDto<WorkflowDef> queryWorkflowDefDto) {
        // 查询发起权限
        if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getInitiatorIds()) || CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getInitiatorGroupIds())){
            Set<String> workflowDefIds = Sets.newHashSet();
            if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getInitiatorIds())){
                Set<String> workflowDefIdsByUserIds = workflowObjectEntityRelationDao.lambdaQuery().eq(WorkflowObjectEntityRelation::getRelationType, WorkflowObjectEntityRelation.RelationType.WORKFLOW_INST_INIT_PERMISSION)
                        .eq(WorkflowObjectEntityRelation::getObjectType, WorkflowObjectEntityRelation.ObjectType.WORKFLOW_DEF)
                        .eq(WorkflowObjectEntityRelation::getEntityType, WorkflowObjectEntityRelation.EntityType.USER)
                        .in(WorkflowObjectEntityRelation::getEntityId, queryWorkflowDefDto.getInitiatorIds()).select(WorkflowObjectEntityRelation::getObjectId)
                        .list().stream().map(WorkflowObjectEntityRelation::getObjectId).collect(Collectors.toSet());
                workflowDefIds.addAll(workflowDefIdsByUserIds);
            }
            if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getInitiatorGroupIds())){
                Set<String> workflowDefIdsByGroupIds = workflowObjectEntityRelationDao.lambdaQuery().eq(WorkflowObjectEntityRelation::getRelationType, WorkflowObjectEntityRelation.RelationType.WORKFLOW_INST_INIT_PERMISSION)
                        .eq(WorkflowObjectEntityRelation::getObjectType, WorkflowObjectEntityRelation.ObjectType.WORKFLOW_DEF)
                        .eq(WorkflowObjectEntityRelation::getEntityType, WorkflowObjectEntityRelation.EntityType.GROUP)
                        .in(WorkflowObjectEntityRelation::getEntityId, queryWorkflowDefDto.getInitiatorGroupIds()).select(WorkflowObjectEntityRelation::getObjectId)
                        .list().stream().map(WorkflowObjectEntityRelation::getObjectId).collect(Collectors.toSet());
                workflowDefIds.addAll(workflowDefIdsByGroupIds);
            }
            if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getDefIds())){
                // 如果查询条件中有模型ids，那么我们只需要保留有权限发起的模型ids
                queryWorkflowDefDto.getDefIds().retainAll(workflowDefIds);
            }else{
                queryWorkflowDefDto.setDefIds(workflowDefIds);
            }
        }
        LambdaQueryChainWrapper<WorkflowDef> queryChainWrapper = workflowDefDao.lambdaQuery();
        if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getStatuses())) {
            queryChainWrapper = queryChainWrapper.in(WorkflowDef::getStatus, queryWorkflowDefDto.getStatuses());
        }
        if (CollectionsUtil.isNotEmpty(queryWorkflowDefDto.getDefIds())) {
            queryChainWrapper = queryChainWrapper.in(WorkflowDef::getId, queryWorkflowDefDto.getDefIds());
        }
        if (StringUtil.isNotEmpty(queryWorkflowDefDto.getKeyword()))
        {
            queryChainWrapper = queryChainWrapper.like(WorkflowDef::getName, queryWorkflowDefDto.getKeyword());
        }
        queryChainWrapper = queryChainWrapper.eq(WorkflowDef::getDeleted, queryWorkflowDefDto.getDeleted());
        Page<WorkflowDef> page = queryChainWrapper.page(queryWorkflowDefDto.page());
        return page;
    }
}

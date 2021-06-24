package com.github.bryx.workflow.service.impl;

import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.domain.WorkflowObjectEntityRelation;
import com.github.bryx.workflow.dto.buildtime.*;
import com.github.bryx.workflow.service.WorkflowBuildTimeQuery;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.dao.WorkflowDefDao;
import com.github.bryx.workflow.service.dao.WorkflowDefRevDao;
import com.github.bryx.workflow.service.dao.WorkflowObjectEntityRelationDao;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Service
public class WorkflowBuildTimeServiceImpl implements WorkflowBuildTimeService {

    @Autowired
    WorkflowDefDao workflowDefDao;

    @Autowired
    WorkflowDefRevDao workflowDefRevDao;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    WorkflowObjectEntityRelationDao workflowObjectEntityRelationDao;

    WorkflowBuildTimeQuery workflowBuildTimeQuery;

    private void updateWorkflowDefInitiators_(String workflowDefId, List<String> initiatorIds, List<String> initiatorGroupIds) {
        // 清楚流程定义上的发起人设置
        workflowObjectEntityRelationDao.lambdaUpdate().eq(WorkflowObjectEntityRelation::getObjectId, workflowDefId)
                .eq(WorkflowObjectEntityRelation::getObjectType, WorkflowObjectEntityRelation.ObjectType.WORKFLOW_DEF)
                .eq(WorkflowObjectEntityRelation::getEntityType, WorkflowObjectEntityRelation.RelationType.WORKFLOW_INST_INIT_PERMISSION).remove();
        // 插入流程定义上的发起人及发起组
        List<WorkflowObjectEntityRelation> relations = Lists.newArrayList();
        if (CollectionsUtil.isNotEmpty(initiatorIds)) {
            initiatorIds.forEach(userId -> {
                relations.add(WorkflowObjectEntityRelation.builder()
                        .objectId(workflowDefId)
                        .objectType(WorkflowObjectEntityRelation.ObjectType.WORKFLOW_DEF)
                        .entityType(WorkflowObjectEntityRelation.EntityType.USER)
                        .entityId(userId).relationType(WorkflowObjectEntityRelation.RelationType.WORKFLOW_INST_INIT_PERMISSION)
                        .build());
            });
        }
        if (CollectionsUtil.isNotEmpty(initiatorGroupIds)) {
            initiatorGroupIds.forEach(groupId -> {
                relations.add(WorkflowObjectEntityRelation.builder()
                        .objectId(workflowDefId)
                        .objectType(WorkflowObjectEntityRelation.ObjectType.WORKFLOW_DEF)
                        .entityType(WorkflowObjectEntityRelation.EntityType.GROUP)
                        .entityId(groupId).relationType(WorkflowObjectEntityRelation.RelationType.WORKFLOW_INST_INIT_PERMISSION)
                        .build());
            });
        }
        workflowObjectEntityRelationDao.saveBatch(relations);
    }

    @Override
    @Transactional
    public WorkflowDef createWorkflowDefDraft(CreateWorkflowDefDraftDto createWorkflowDefDraftDto) {
        String workflowDefId = createWorkflowDefDraftDto.getId();
        WorkflowDef workflowDef = null;
        if (workflowDefId == null) {
            Validate.notNull(createWorkflowDefDraftDto.getName(), "must provide workflow def name");
            Validate.notNull(createWorkflowDefDraftDto.getProcessDefType(), "must provide process def type");
            Validate.notNull(createWorkflowDefDraftDto.getCreatorId(), "must provide creator id");
            // 名字不能重复
            int count = workflowDefDao.lambdaQuery().eq(WorkflowDef::getName, createWorkflowDefDraftDto.getName()).count();
            if (count > 0) {
                throw new WorkflowRuntimeException(String.format("workflow def name %s occupied", createWorkflowDefDraftDto.getName()));
            }
            // 新的流程定义，创建定义
            workflowDef = new WorkflowDef();
            workflowDef.setStatus(WorkflowDef.WorkflowDefStatus.DISABLE);
            workflowDef.setCreateTime(new Date());
            workflowDef.setLastModifyTime(new Date());
            workflowDef.setCreatorId(createWorkflowDefDraftDto.getCreatorId());
            workflowDef.setLastModifierId(createWorkflowDefDraftDto.getCreatorId());
            workflowDef.setName(createWorkflowDefDraftDto.getName());
            workflowDef.setProcessDefType(createWorkflowDefDraftDto.getProcessDefType());
            workflowDefDao.save(workflowDef);
            workflowDefId = workflowDef.getId();
            // 更新流程定义的发起人权限
            this.updateWorkflowDefInitiators_(workflowDefId, createWorkflowDefDraftDto.getInitUserIds(), createWorkflowDefDraftDto.getInitGroupIds());
        } else {
            workflowDef = workflowDefDao.getById(workflowDefId);
            if (workflowDef == null) {
                throw new WorkflowRuntimeException("invalid workflow def id");
            }
            Integer countOfDrafts = workflowDefRevDao.lambdaQuery().eq(WorkflowDefRev::getStatus, WorkflowDefRev.WorkflowDefRevStatus.DRAFT).count();
            if (countOfDrafts >= 1) {
                throw new WorkflowRuntimeException("draft already exists");
            }
        }
        // 创建版本
        WorkflowDefRev workflowDefRev = new WorkflowDefRev();
        workflowDefRev.setCreateTime(new Date());
        workflowDefRev.setLastModifyTime(new Date());
        workflowDefRev.setCreatorId(createWorkflowDefDraftDto.getCreatorId());
        workflowDefRev.setLastModifierId(createWorkflowDefDraftDto.getCreatorId());
        workflowDefRev.setDefId(workflowDefId);
        workflowDefRev.setStatus(WorkflowDefRev.WorkflowDefRevStatus.DRAFT);
        workflowDefRev.setModuleUILayout(createWorkflowDefDraftDto.getModuleUILayout());
        workflowDefRev.setProcessConfig(createWorkflowDefDraftDto.getProcessConfig());
        workflowDefRev.setProcessFlowFileString(createWorkflowDefDraftDto.getProcessFlowFileString());
        workflowDefRevDao.save(workflowDefRev);

        workflowDef.setRev(workflowDefRev);
        return workflowDef;
    }

    @Override
    @Transactional
    public void updateWorkflowDefDraft(UpdateWorkflowDefDraftDto updateWorkflowDefDraftDto) {
        Validate.notNull(updateWorkflowDefDraftDto.getDefId(), "must provide workflow def id");
        WorkflowDefRev draft = this.query().getWorkflowDefRevDraft(updateWorkflowDefDraftDto.getDefId());
        WorkflowDefRev workflowDefRev = new WorkflowDefRev();
        workflowDefRev.setId(draft.getId());
        workflowDefRev.setProcessFlowFileString(updateWorkflowDefDraftDto.getProcessFlowFileString());
        workflowDefRev.setProcessConfig(updateWorkflowDefDraftDto.getProcessConfig());
        workflowDefRev.setLastModifierId(updateWorkflowDefDraftDto.getModifierId());
        workflowDefRev.setModuleUILayout(updateWorkflowDefDraftDto.getModuleUILayout());
        workflowDefRev.setLastModifyTime(new Date());
        workflowDefRevDao.updateById(workflowDefRev);
    }

    @Override
    @Transactional
    public void updateWorkflowDefRev(UpdateWorkflowDefRevDto updateWorkflowDefRevDto) {
        Validate.notNull(updateWorkflowDefRevDto.getRevId(), "must provide workflow def rev id");
        WorkflowDefRev workflowDefRevExists = workflowDefRevDao.getById(updateWorkflowDefRevDto.getRevId());
        Validate.notNull(workflowDefRevExists, "invalid workflow def rev id");

        WorkflowDefRev workflowDefRev = new WorkflowDefRev();
        workflowDefRev.setId(updateWorkflowDefRevDto.getRevId());
        workflowDefRev.setProcessConfig(updateWorkflowDefRevDto.getProcessConfig());
        workflowDefRev.setLastModifierId(updateWorkflowDefRevDto.getModifierId());
        workflowDefRev.setModuleUILayout(updateWorkflowDefRevDto.getModuleUILayout());
        workflowDefRev.setLastModifyTime(new Date());
        workflowDefRevDao.updateById(workflowDefRev);
    }


    @Override
    @Transactional
    public void updateWorkflowDef(UpdateWorkflowDefDto updateWorkflowDefDto) {
        Validate.notNull(updateWorkflowDefDto.getId(), "must provide workflow def id");
        Validate.notNull(updateWorkflowDefDto.getModifierId(), "must provide modifier id");
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setId(updateWorkflowDefDto.getId());
        if (StringUtil.isNotEmpty(updateWorkflowDefDto.getName())) {
            // 检查名称是否被占用
            int count = workflowDefDao.count(workflowDefDao.lambdaQuery().eq(WorkflowDef::getName, updateWorkflowDefDto.getName()).ne(WorkflowDef::getId, updateWorkflowDefDto.getId()));
            if (count > 0) {
                throw new WorkflowRuntimeException("workflow def name occupied");
            }
            workflowDef.setName(updateWorkflowDefDto.getName());
        }
        workflowDef.setLastModifierId(updateWorkflowDefDto.getModifierId());
        workflowDef.setStatus(updateWorkflowDefDto.getStatus());
        workflowDef.setLastModifyTime(new Date());
        workflowDefDao.updateById(workflowDef);
        // 更新流程定义的发起人权限
        this.updateWorkflowDefInitiators_(updateWorkflowDefDto.getId(), updateWorkflowDefDto.getInitiatorIds(), updateWorkflowDefDto.getInitiatorGroupIds());
    }

    @Override
    @Transactional
    public void publishWorkflowDefDraft(PublishWorkflowDefDraftDto publishWorkflowDefDraftDto) {
        Validate.notNull(publishWorkflowDefDraftDto.getId(), "must provide workflow def id");
        WorkflowDef workflowDef = workflowDefDao.getById(publishWorkflowDefDraftDto.getId());
        if (workflowDef == null) {
            throw new WorkflowRuntimeException("invalid workflow def id");
        }
        // 获取最新的定义版本
        WorkflowDefRev lastestDefRev = workflowDefRevDao.lambdaQuery()
                .select(WorkflowDefRev::getId, WorkflowDefRev::getVersion)
                .eq(WorkflowDefRev::getDefId, publishWorkflowDefDraftDto.getId())
                .ne(WorkflowDefRev::getStatus, WorkflowDefRev.WorkflowDefRevStatus.DRAFT)
                .orderByDesc(WorkflowDefRev::getVersion).one();

        // 发布流程到activiti，并且更新流程版本相关信息
        WorkflowDefRev workflowDefRevDraft = this.query().getWorkflowDefRevDraft(publishWorkflowDefDraftDto.getId());
        String processFileContent = StringUtil.base64Decode(workflowDefRevDraft.getProcessFlowFileString());
        ProcessDef processDef = processDefinitionService.deployProcessDefinition(workflowDef.getProcessDefType()+".bpmn20.xml", processFileContent);
        workflowDefRevDraft.setStatus(WorkflowDefRev.WorkflowDefRevStatus.ENABLE);
        workflowDefRevDraft.setProcessDefDeploymentId(processDef.getDeploymentId());
        workflowDefRevDraft.setProcessDefId(processDef.getId());
        workflowDefRevDraft.setProcessDefVersion(processDef.getVersion());
        workflowDefRevDraft.setLastModifierId(publishWorkflowDefDraftDto.getOperatorId());
        workflowDefRevDraft.setLastModifyTime(new Date());
        if (lastestDefRev == null) {
            workflowDefRevDraft.setVersion(1);
        } else {
            workflowDefRevDraft.setVersion(lastestDefRev.getVersion() + 1);
        }
        workflowDefRevDao.updateById(workflowDefRevDraft);
        // 更新流程定义
        UpdateWorkflowDefDto updateWorkflowDefDto = new UpdateWorkflowDefDto();
        updateWorkflowDefDto.setId(publishWorkflowDefDraftDto.getId());
        updateWorkflowDefDto.setModifierId(publishWorkflowDefDraftDto.getOperatorId());
        updateWorkflowDefDto.setStatus(WorkflowDef.WorkflowDefStatus.ENABLE);
        this.updateWorkflowDef(updateWorkflowDefDto);
    }

    @Override
    @Transactional
    public void changeWorkflowDefRevStatus(ChangeWorkflowDefRevStatusDto changeWorkflowDefRevStatusDto) {
        Validate.notNull(changeWorkflowDefRevStatusDto.getDefRevId(), "must provide workflow def rev id");
        Validate.notNull(changeWorkflowDefRevStatusDto.getModifierId(), "must provide modifier id");
        WorkflowDefRev workflowDefRev = new WorkflowDefRev();
        workflowDefRev.setId(changeWorkflowDefRevStatusDto.getDefRevId());
        workflowDefRev.setStatus(changeWorkflowDefRevStatusDto.getStatus());
        workflowDefRev.setLastModifierId(changeWorkflowDefRevStatusDto.getModifierId());
        workflowDefRev.setLastModifyTime(new Date());
        workflowDefRevDao.updateById(workflowDefRev);
    }

    @Override
    @Transactional
    public void deleteWorkflowDef(String workflowDefId) {
        // TODO delete all workflow instances and process related data
        List<WorkflowDefRev> revs = workflowDefRevDao.lambdaQuery().eq(WorkflowDefRev::getDefId, workflowDefId).select(WorkflowDefRev::getId).list();
        revs.forEach(item->{
            item.setDeleted(true);
        });
        workflowDefRevDao.updateBatchById(revs);

        WorkflowDef def = new WorkflowDef();
        def.setId(workflowDefId);
        def.setDeleted(true);
        workflowDefDao.updateById(def);
    }

    @Override
    public void forceDeleteWorkflowDef(String workflowDefId) {
        workflowDefDao.forceDeleteWorkflowDef(workflowDefId);
    }

    @Override
    @Transactional
    public void deleteWorkflowDefRev(String workflowDefRevId) {
        WorkflowDefRev rev = new WorkflowDefRev();
        rev.setId(workflowDefRevId);
        rev.setDeleted(true);
        workflowDefRevDao.updateById(rev);
    }

    @Override
    public WorkflowBuildTimeQuery query() {
        if (workflowBuildTimeQuery == null){
            WorkflowBuildTimeQueryImpl workflowBuildTimeQueryImpl = new WorkflowBuildTimeQueryImpl();
            workflowBuildTimeQueryImpl.setWorkflowDefDao(workflowDefDao);
            workflowBuildTimeQueryImpl.setWorkflowDefRevDao(workflowDefRevDao);
            workflowBuildTimeQueryImpl.setWorkflowObjectEntityRelationDao(workflowObjectEntityRelationDao);
            this.workflowBuildTimeQuery = workflowBuildTimeQueryImpl;
        }
        return this.workflowBuildTimeQuery;
    }
}

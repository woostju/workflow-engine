package com.github.bryxtest.workflow;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.bryx.workflow.mapper.WorkflowDefRevMapper;
import com.github.bryxtest.WorkflowApplication;
import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.github.bryx.workflow.util.FileUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.github.bryx.workflow.dto.buildtime.*;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
@Transactional
public class WorkflowBuildTimeTest {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    ResourceLoader resourceLoader;

    private String loadBpmnFileContent(boolean applyleaveOrTimer) throws Exception {
        File file = resourceLoader.getResource(applyleaveOrTimer? "unittest-applyleave.bpmn20.xml" :"unittest-timer.bpmn20.xml").getFile();
        String content = FileUtil.readFileToString(file);
        return  StringUtil.base64Encode(content);
    }

    private WorkflowDefProcessConfig createWorkflowDefProcessConfig() throws Exception {
        File file = resourceLoader.getResource("unittest-applyleave.config.json").getFile();
        String content = FileUtil.readFileToString(file);
        return JSON.parseObject(content, WorkflowDefProcessConfig.class);

    }

    @Test
    public void testWorkflowDefCreateSuccess() throws Exception {
        // 创建新模型，及草稿
        CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
        createWorkflowDefDraftDto.setCreatorId("james");
        createWorkflowDefDraftDto.setInitUserIds(Lists.newArrayList("james"));
        createWorkflowDefDraftDto.setName("请假流程");
        createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
        createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
        createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
        WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);
        WorkflowDef workflowDef = workflowBuildTimeService.query().getWorkflowDefByName(workflowDefCreated.getName());
        WorkflowDefRev draft = workflowBuildTimeService.query().getWorkflowDefRevDraft(workflowDef.getId());
        assertArrayEquals(new Object[]{
                        true, true,true,true
                }
                , new Object[]{
                        workflowDef.getStatus().equals( WorkflowDef.WorkflowDefStatus.DISABLE),
                        workflowDef.getCreateTime()!=null,
                        workflowDef.getName().equals(createWorkflowDefDraftDto.getName()),
                        workflowDef.getProcessDefType().equals(createWorkflowDefDraftDto.getProcessDefType()),
                });
        // 发布草稿
        PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
        publishWorkflowDefDraftDto.setId(workflowDef.getId());
        publishWorkflowDefDraftDto.setOperatorId("james");
        workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        WorkflowDefRev latestEnabledWorkflowDefRev = workflowBuildTimeService.query().getLatestEnabledWorkflowDefRev(workflowDef.getId());
        WorkflowDef enabledWorkflowDef = workflowBuildTimeService.query().getWorkflowDefById(workflowDef.getId());
        assertArrayEquals(new Object[]{
                        true, true, true, true, true
                }
                , new Object[]{
                        enabledWorkflowDef.getStatus().equals(WorkflowDef.WorkflowDefStatus.ENABLE),
                        latestEnabledWorkflowDefRev.getStatus().equals(WorkflowDefRev.WorkflowDefRevStatus.ENABLE),
                        latestEnabledWorkflowDefRev.getProcessDefId() != null,
                        latestEnabledWorkflowDefRev.getProcessDefDeploymentId() != null,
                        latestEnabledWorkflowDefRev.getProcessDefVersion() != null
                });
    }

    @Autowired
    WorkflowDefRevMapper workflowDefRevMapper;

    @Test
    public void testMultipleWorkflowDefRevsSuccess() throws Exception {
        // 创建新模型，及草稿
        CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
        createWorkflowDefDraftDto.setCreatorId("james");
        createWorkflowDefDraftDto.setName("请假流程");
        createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
        createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
        createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
        WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

        PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
        publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
        publishWorkflowDefDraftDto.setOperatorId("james");
        workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);

        {
            // create one version
            WorkflowDefProcessConfig workflowDefProcessConfig = this.createWorkflowDefProcessConfig();
            createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            createWorkflowDefDraftDto.setCreatorId("james");
            workflowDefProcessConfig.getUserTasks().get("user_task1").setAssigneeUserIds(Lists.newArrayList("smith"));
            createWorkflowDefDraftDto.setProcessConfig(workflowDefProcessConfig);
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("james");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        }

        List<WorkflowDefRev> records = workflowDefRevMapper.selectPage(new Page<>(1, 2), null).getRecords();

        {
            // 此时有两个激活的版本
            QueryWorkflowDefRevDto<WorkflowDefRev> queryWorkflowDefRevsDto = new QueryWorkflowDefRevDto<>();
            queryWorkflowDefRevsDto.setDefId(workflowDefCreated.getId());
            queryWorkflowDefRevsDto.setCurrent(1l);
            queryWorkflowDefRevsDto.setSize(1l);
            Page<WorkflowDefRev> workflowDefRevPage = workflowBuildTimeService.query().queryWorkflowDefRevs(queryWorkflowDefRevsDto);
            WorkflowDefRev latestEnabledWorkflowDefRev = workflowBuildTimeService.query().getLatestEnabledWorkflowDefRev(workflowDefCreated.getId());
            assertArrayEquals(new Object[]{
                           1, false
                    }
                    , new Object[]{
                            workflowDefRevPage.getRecords().size(),
                            latestEnabledWorkflowDefRev.getProcessConfig().getUserTasks().get("user_task1").getAssigneeUserIds().contains("smith")
                    });
        }
    }

    @Test
    public void testWorkflowDefQuerySuccess() throws Exception {
        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("james");
            createWorkflowDefDraftDto.setName("请假流程");
            createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("james");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        }

        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("smith");
            createWorkflowDefDraftDto.setName("超时流程测试");
            createWorkflowDefDraftDto.setProcessDefType("unittest-timer");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(false));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("smith");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
            UpdateWorkflowDefDto updateWorkflowDefDto = new UpdateWorkflowDefDto();
            updateWorkflowDefDto.setId(workflowDefCreated.getId());
            updateWorkflowDefDto.setModifierId("smith");
            updateWorkflowDefDto.setStatus(WorkflowDef.WorkflowDefStatus.DISABLE);
            workflowBuildTimeService.updateWorkflowDef(updateWorkflowDefDto);
        }

        QueryWorkflowDefDto<WorkflowDef> queryDefDto = new QueryWorkflowDefDto<>();
        queryDefDto.setStatuses(Lists.newArrayList(WorkflowDef.WorkflowDefStatus.ENABLE));
        Page<WorkflowDef> workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryDefDto);
        assertArrayEquals(new Object[]{
                        1, 1l,0l,1l
                }
                , new Object[]{
                        workflowDefPage.getRecords().size(),
                        workflowDefPage.getTotal(),
                        workflowDefPage.getRecords().stream().filter(item->item.getName().equals("超时流程测试")).count(),
                        workflowDefPage.getRecords().stream().filter(item->item.getName().equals("请假流程")).count()
                });

        WorkflowDef workflowDefTimer = workflowBuildTimeService.query().getWorkflowDefByName("超时流程测试");
        queryDefDto = new QueryWorkflowDefDto<>();
        queryDefDto.setDefIds(Lists.newArrayList(workflowDefTimer.getId()));
        workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryDefDto);
        assertArrayEquals(new Object[]{
                        1, 1l,1l
                }
                , new Object[]{
                        workflowDefPage.getRecords().size(),
                        workflowDefPage.getTotal(),
                        workflowDefPage.getRecords().stream().filter(item->item.getName().equals("超时流程测试")).count(),
                });

    }

    @Test
    public void testDeleteWorkflowDefSuccess() throws Exception {
        String firstWorkflowDefId = null;
        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("james");
            createWorkflowDefDraftDto.setName("请假流程");
            createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);
            firstWorkflowDefId = workflowDefCreated.getId();

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("james");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        }

        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("smith");
            createWorkflowDefDraftDto.setName("超时流程测试");
            createWorkflowDefDraftDto.setProcessDefType("unittest-timer");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(false));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("smith");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
            UpdateWorkflowDefDto updateWorkflowDefDto = new UpdateWorkflowDefDto();
            updateWorkflowDefDto.setId(workflowDefCreated.getId());
            updateWorkflowDefDto.setModifierId("smith");
            updateWorkflowDefDto.setStatus(WorkflowDef.WorkflowDefStatus.DISABLE);
            workflowBuildTimeService.updateWorkflowDef(updateWorkflowDefDto);
        }

        workflowBuildTimeService.deleteWorkflowDef(firstWorkflowDefId);
        QueryWorkflowDefDto<WorkflowDef> queryDto = new QueryWorkflowDefDto();
        Page<WorkflowDef> workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryDto);
        assertArrayEquals(new Object[]{
                        1, 1l,1l
                }
                , new Object[]{
                        workflowDefPage.getRecords().size(),
                        workflowDefPage.getTotal(),
                        workflowDefPage.getRecords().stream().filter(item->item.getName().equals("超时流程测试")).count(),
                });
    }

    @Test(expected = WorkflowRuntimeException.class)
    public void testDuplicateNameFailed() throws Exception {
        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("james");
            createWorkflowDefDraftDto.setName("请假流程");
            createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);
        }

        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("smith");
            createWorkflowDefDraftDto.setName("请假流程");
            createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave2");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);
        }
    }

    @Test
    public void queryWorkflowDefsPermissionSuccess() throws Exception{
        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("james");
            createWorkflowDefDraftDto.setName("请假流程");
            createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(true));
            createWorkflowDefDraftDto.setInitUserIds(Lists.newArrayList("smith"));
            createWorkflowDefDraftDto.setInitGroupIds(Lists.newArrayList("dev"));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("james");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        }

        {
            // 创建新模型，及草稿
            CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
            createWorkflowDefDraftDto.setCreatorId("smith");
            createWorkflowDefDraftDto.setName("超时流程测试");
            createWorkflowDefDraftDto.setProcessDefType("unittest-timer");
            createWorkflowDefDraftDto.setInitGroupIds(Lists.newArrayList("test","dev"));
            createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
            createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent(false));
            WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

            PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
            publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
            publishWorkflowDefDraftDto.setOperatorId("smith");
            workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
            UpdateWorkflowDefDto updateWorkflowDefDto = new UpdateWorkflowDefDto();
            updateWorkflowDefDto.setId(workflowDefCreated.getId());
            updateWorkflowDefDto.setModifierId("smith");
            updateWorkflowDefDto.setStatus(WorkflowDef.WorkflowDefStatus.DISABLE);
            workflowBuildTimeService.updateWorkflowDef(updateWorkflowDefDto);
        }

        // smith只有请假流程
        QueryWorkflowDefDto<WorkflowDef> queryWorkflowDefDto = new QueryWorkflowDefDto();
        queryWorkflowDefDto.setInitiatorIds(Lists.newArrayList("smith"));
        Page<WorkflowDef> workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryWorkflowDefDto);
        assertArrayEquals(new Object[]{
                        1, 1l, 1l
                }
                , new Object[]{
                        workflowDefPage.getRecords().size(),
                        workflowDefPage.getTotal(),
                        workflowDefPage.getRecords().stream().filter(item->item.getName().equals("请假流程")).count(),
                });

        // smith和test组有两个流程的权限
        queryWorkflowDefDto = new QueryWorkflowDefDto();
        queryWorkflowDefDto.setInitiatorIds(Lists.newArrayList("smith"));
        queryWorkflowDefDto.setInitiatorGroupIds(Lists.newArrayList("test"));
        workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryWorkflowDefDto);
        assertArrayEquals(new Object[]{
                        2, 2l
                }
                , new Object[]{
                        workflowDefPage.getRecords().size(),
                        workflowDefPage.getTotal()
                });
    }

}

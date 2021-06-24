package com.github.bryxtest.workflow;

import com.alibaba.fastjson.JSON;
import com.github.bryxtest.WorkflowApplication;
import com.github.bryx.workflow.util.DateTimeUtils;
import com.github.bryx.workflow.util.FileUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.github.bryx.workflow.dto.buildtime.CreateWorkflowDefDraftDto;
import com.github.bryx.workflow.dto.buildtime.PublishWorkflowDefDraftDto;
import com.github.bryx.workflow.domain.WorkflowDef;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import com.github.bryx.workflow.domain.WorkflowInstance;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
@Transactional
public class WorkflowRuntimeTest {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ResourceLoader resourceLoader;

    private String loadBpmnFileContent() throws Exception {
        File file = resourceLoader.getResource("unittest-applyleave.bpmn20.xml").getFile();
        String content = FileUtil.readFileToString(file);
        return  StringUtil.base64Encode(content);
    }

    private WorkflowDefProcessConfig createWorkflowDefProcessConfig() throws Exception {
        File file = resourceLoader.getResource("unittest-applyleave.config.json").getFile();
        String content = FileUtil.readFileToString(file);
        return JSON.parseObject(content, WorkflowDefProcessConfig.class);
    }

    private WorkflowDef createWorkflowDef()throws Exception {
        CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
        createWorkflowDefDraftDto.setCreatorId("james");
        createWorkflowDefDraftDto.setName("请假流程");
        createWorkflowDefDraftDto.setProcessDefType("unittest-applyleave");
        createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
        createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent());
        WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

        PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
        publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
        publishWorkflowDefDraftDto.setOperatorId("james");
        workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        return workflowDefCreated;
    }

    @Test
    public void testWorkflowInstanceRunningSuccess() throws Exception {
        // 创建新模型，及草稿
        WorkflowDef workflowDef = this.createWorkflowDef();
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("days", 3);
        formData.put("reason","tired");
        formData.put("startTime", DateTimeUtils.getDate());
        formData.put("endTime", DateTimeUtils.getDate());
        String workflowInstanceId = workflowRuntimeService.start(workflowDef.getId(), formData, "james");
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                    true, true, true
                }
                , new Object[]{
                        workflowInstance.getCreateTime() != null,
                        workflowInstance.getCreatorId().equals("james"),
                        workflowInstance.getStatus().equals(WorkflowInstance.WorkflowInstanceStatus.ONGOING)
                });
        formData.put("approve", false);
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "smith");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        1, true, true
                }
                , new Object[]{
                        workflowTaskInstances.size(),
                        workflowTaskInstances.get(0).getAssigneeUserIds().contains("james"),
                        workflowTaskInstances.get(0).getName().equals("请假")
                });

        formData.put("days", 1);
        formData.put("reason","not tired enough");
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "james");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        1, true, true
                }
                , new Object[]{
                        workflowTaskInstances.size(),
                        workflowTaskInstances.get(0).getAssigneeGroupIds().contains("hr"),
                        workflowTaskInstances.get(0).getName().equals("人事组审批")
                });

        formData.put("approve", true);
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "zhichao");
        workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        workflowTaskInstances  = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        0, true
                }
                , new Object[]{
                        workflowTaskInstances.size(),
                        workflowInstance.getStatus().equals(WorkflowInstance.WorkflowInstanceStatus.COMPLETED)
                });
    }

    @Test
    public void testWorkflowInstanceRejectBackSuccess() throws Exception {
        // 创建新模型，及草稿
        WorkflowDef workflowDef = this.createWorkflowDef();
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("days", 3);
        formData.put("reason","tired");
        formData.put("startTime", DateTimeUtils.getDate());
        formData.put("endTime", DateTimeUtils.getDate());
        String workflowInstanceId = workflowRuntimeService.start(workflowDef.getId(), formData, "james");
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // 总经理审核
        formData.put("approve", false);
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "smith");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // 再次提交
        formData.put("days", 1);
        formData.put("reason","not tired enough");
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "james");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // rejectback到个人提交
        workflowRuntimeService.rejectBack(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "smith");
        workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        workflowTaskInstances  = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        1, true
                }
                , new Object[]{
                        workflowTaskInstances.size(),
                        workflowTaskInstances.get(0).getName().equals("请假")
                });
    }

    @Test
    public void testWorkflowInstanceClaimSuccess() throws Exception {
        // 创建新模型，及草稿
        WorkflowDef workflowDef = this.createWorkflowDef();
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("days", 1);
        formData.put("reason","tired");
        formData.put("startTime", DateTimeUtils.getDate());
        formData.put("endTime", DateTimeUtils.getDate());
        String workflowInstanceId = workflowRuntimeService.start(workflowDef.getId(), formData, "james");
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // HR组中的tracy认领
        workflowRuntimeService.claim(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "tracy");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        1, true, 1, true
                }
                , new Object[]{
                        workflowTaskInstances.size(),
                        workflowTaskInstances.get(0).getAssigneeGroupIds().isEmpty(),
                        workflowTaskInstances.get(0).getAssigneeUserIds().size(),
                        workflowTaskInstances.get(0).getAssigneeUserIds().contains("tracy")
                });
    }

    @Test
    public void testWorkflowInstanceCloseSuccess() throws Exception {
        // 创建新模型，及草稿
        WorkflowDef workflowDef = this.createWorkflowDef();
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("days", 3);
        formData.put("reason","tired");
        formData.put("startTime", DateTimeUtils.getDate());
        formData.put("endTime", DateTimeUtils.getDate());
        String workflowInstanceId = workflowRuntimeService.start(workflowDef.getId(), formData, "james");
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // 总经理审核
        formData.put("approve", false);
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "smith");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // 再次提交
        formData.put("days", 1);
        formData.put("reason","not tired enough");
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "james");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        // 中途close掉
        workflowRuntimeService.close(workflowInstanceId, "admin");
        workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        workflowTaskInstances  = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        assertArrayEquals(new Object[]{
                        true, true
                }
                , new Object[]{
                        workflowInstance.getStatus().equals(WorkflowInstance.WorkflowInstanceStatus.CLOSED),
                        workflowTaskInstances.isEmpty()
                });
    }
}

package com.github.bryxtest.workflow;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.bryx.workflow.domain.*;
import com.github.bryx.workflow.dto.buildtime.CreateWorkflowDefDraftDto;
import com.github.bryx.workflow.dto.buildtime.PublishWorkflowDefDraftDto;
import com.github.bryx.workflow.dto.buildtime.QueryWorkflowDefDto;
import com.github.bryx.workflow.service.WorkflowBuildTimeService;
import com.github.bryx.workflow.service.WorkflowRuntimeService;
import com.github.bryx.workflow.util.FileUtil;
import com.github.bryx.workflow.util.StringUtil;
import com.github.bryxtest.WorkflowApplication;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
public class WorkflowRuntimeTimerTest {

    @Autowired
    WorkflowBuildTimeService workflowBuildTimeService;

    @Autowired
    WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    ResourceLoader resourceLoader;

    private String loadBpmnFileContent() throws Exception {
        File file = resourceLoader.getResource("unittest-timer.bpmn20.xml").getFile();
        String content = FileUtil.readFileToString(file);
        return  StringUtil.base64Encode(content);
    }

    private WorkflowDefProcessConfig createWorkflowDefProcessConfig() throws Exception {
        File file = resourceLoader.getResource("unittest-timer.config.json").getFile();
        String content = FileUtil.readFileToString(file);
        return JSON.parseObject(content, WorkflowDefProcessConfig.class);
    }

    private WorkflowDef createWorkflowDef()throws Exception {
        CreateWorkflowDefDraftDto createWorkflowDefDraftDto = new CreateWorkflowDefDraftDto();
        createWorkflowDefDraftDto.setCreatorId("james");
        createWorkflowDefDraftDto.setName("单元测试超时");
        createWorkflowDefDraftDto.setProcessDefType("unittest-timer");
        createWorkflowDefDraftDto.setProcessConfig(this.createWorkflowDefProcessConfig());
        createWorkflowDefDraftDto.setProcessFlowFileString(this.loadBpmnFileContent());
        WorkflowDef workflowDefCreated = workflowBuildTimeService.createWorkflowDefDraft(createWorkflowDefDraftDto);

        PublishWorkflowDefDraftDto publishWorkflowDefDraftDto = new PublishWorkflowDefDraftDto();
        publishWorkflowDefDraftDto.setId(workflowDefCreated.getId());
        publishWorkflowDefDraftDto.setOperatorId("james");
        workflowBuildTimeService.publishWorkflowDefDraft(publishWorkflowDefDraftDto);
        return workflowDefCreated;
    }

    @Before
    public void init(){
        this.destroy();
    }

    @After
    public void destroy(){
        QueryWorkflowDefDto<WorkflowDef> queryDto = new QueryWorkflowDefDto<>();
        queryDto.setKeyword("单元测试超时");
        Page<WorkflowDef> workflowDefPage = workflowBuildTimeService.query().queryWorkflowDefs(queryDto);
        if (!workflowDefPage.getRecords().isEmpty()){
            workflowBuildTimeService.forceDeleteWorkflowDef(workflowDefPage.getRecords().get(0).getId());
        }
    }

    @Test
    public void testTimerCreateSuccess() throws Exception {
        // 创建新模型，及草稿
        WorkflowDef workflowDef = this.createWorkflowDef();
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("level", 3);
        String workflowInstanceId = workflowRuntimeService.start(workflowDef.getId(), formData, "james");
        WorkflowInstance workflowInstance = workflowRuntimeService.query().getWorkflowInstanceById(workflowInstanceId);
        List<WorkflowTaskInstance> workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        List<WorkflowTimerInstance> workflowTimerInstancesOnTask = workflowRuntimeService.query().getWorkflowTimerInstancesOnTask(workflowTaskInstances.get(0).getId());
        assertArrayEquals(new Object[]{
                    1
                }
                , new Object[]{
                        workflowTimerInstancesOnTask.size()
                });
        formData.put("level", false);
        workflowRuntimeService.submit(workflowInstanceId, workflowTaskInstances.get(0).getId(), formData, "smith");
        workflowTaskInstances = workflowRuntimeService.query().getWorkflowTaskInstances(workflowInstanceId);
        workflowTimerInstancesOnTask = workflowRuntimeService.query().getWorkflowTimerInstancesOnTask(workflowTaskInstances.get(0).getId());
        assertArrayEquals(new Object[]{
                        2
                }
                , new Object[]{
                        workflowTimerInstancesOnTask.size()
                });
        TimeUnit.SECONDS.sleep(100);


    }

}

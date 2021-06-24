package com.github.bryxtest.workflow;

import com.github.bryxtest.WorkflowApplication;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.domain.process.buildtime.TaskTimer;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
import com.github.bryx.workflow.service.process.ProcessService;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
class ProcessServiceTimerTest {

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ResourceLoader resourceLoader;

    private ProcessDef deployProcessDef() throws IOException {
        File file = resourceLoader.getResource("unittest-timer.bpmn20.xml").getFile();
        ProcessDef processDef = processDefinitionService.deployProcessDefinition(file.getName(), new FileInputStream(file));
        return processDefinitionService.getProcessDefinitionById(processDef.getId());
    }



    @Test
    void testAddTimerDurationSuccess() throws IOException {
        ProcessDef processDef = null;
        String processId = null;
        try{
            processDef = deployProcessDef();
            // 开启流程
            processId = processService.startProcess(processDef.getId(), "james", Maps.newHashMap(),(tasks, process)->{
                Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
                maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId("james").build()));
                return maps;
            });
            List<TaskObject> tasks = processService.getTasks(processId);
            processService.addTimerToTask(tasks.get(0).getId(), "timer1", 10, TimeUnit.SECONDS);
            List<TaskTimer> taskTimers = processService.getTaskTimers(processId, tasks.get(0).getExecutionId());
            List<Date> successResult = Lists.newArrayList();
            processService.setTimerTriggerHandler(task->{
                successResult.add(task.getTriggerTime());
            });
            TimeUnit.SECONDS.sleep(20);
            assertArrayEquals(new Object[]{
                            1
                    }
                    ,new Object[]{
                            successResult.size()
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (processId!=null){
                processService.deleteProcess(processId);
            }
            if (processDef!=null){
                processDefinitionService.deleteProcessDefinition(processDef.getDeploymentId());
            }
        }
    }

    @Test
    void testAddTimerCronExpressionSuccess() throws IOException {
        ProcessDef processDef = null;
        String processId = null;
        try{
            processDef = deployProcessDef();
            // 开启流程
            processId = processService.startProcess(processDef.getId(), "james", Maps.newHashMap(),(tasks, process)->{
                Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
                maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId("james").build()));
                return maps;
            });
            List<TaskObject> tasks = processService.getTasks(processId);
            processService.addTimerToTask(tasks.get(0).getId(), "timer1", "20 * * * * ?");
            List<TaskTimer> taskTimers = processService.getTaskTimers(processId, tasks.get(0).getExecutionId());
            List<Date> successResult = Lists.newArrayList();
            processService.setTimerTriggerHandler(task->{
                successResult.add(task.getTriggerTime());
            });
            TimeUnit.SECONDS.sleep(30);
            assertArrayEquals(new Object[]{
                        1
                    }
                    ,new Object[]{
                            successResult.size()
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (processId!=null){
                processService.deleteProcess(processId);
            }
            if (processDef!=null){
                processDefinitionService.deleteProcessDefinition(processDef.getDeploymentId());
            }
        }
    }
}

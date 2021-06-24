package com.github.bryxtest.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.bryxtest.WorkflowApplication;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.domain.process.runtime.ProcessObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.service.dao.process.TaskObjectAssigneeDao;
import com.github.bryx.workflow.util.FileUtil;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
@Transactional
class ProcessServiceRuntimeTests {

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    TaskObjectAssigneeDao taskObjectAssigneeDao;

    @Autowired
    ResourceLoader resourceLoader;

    private ProcessDef deployProcessDef() throws IOException {
        File file = resourceLoader.getResource("unittest-applyleave.bpmn20.xml").getFile();
        ProcessDef processDef = processDefinitionService.deployProcessDefinition(file.getName(), new FileInputStream(file));
        return processDefinitionService.getProcessDefinitionById(processDef.getId());
    }

    private String loadBpmnFileContent() throws Exception {
        File file = resourceLoader.getResource("unittest-applyleave.bpmn20.xml").getFile();
        String content = FileUtil.readFileToString(file);
        return  content;
    }

    @Test
    void testDeployProcessDefWithString() throws Exception{
        String loadBpmnFileContent = this.loadBpmnFileContent();
        ProcessDef processDef = processDefinitionService.deployProcessDefinition("unittest-applyleave.bpmn20.xml", loadBpmnFileContent);
        assertArrayEquals(new Object[]{
                        true
                }
                ,new Object[]{
                        processDef != null
                });
    }


    @Test
    void testHrApproveSuccess() throws IOException {
        ProcessDef processDef = deployProcessDef();
        // 开启流程 请假，受理人是james
        HashMap<Object, Object> variables = Maps.newHashMap();
        variables.put("days", 2);
        String processId = processService.startProcess(processDef.getId(), "james", variables, (tasks, process)->{
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            if (tasks.get(0).getName().equals("人事组审批")) {
                maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("hr").assigneeType(TaskObjectAssignee.AssigneeType.GROUP).build()));
            } else {
                maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("smith").assigneeType(TaskObjectAssignee.AssigneeType.USER).build()));
            }
            return maps;
        });

        List<TaskObject> approveTasks =  processService.getTasks(processId);
        assertArrayEquals(new Object[]{
                        1, true
                }
                ,new Object[]{
                        approveTasks.size(),
                        approveTasks.get(0).getAssignedGroupIds().contains("hr")
                });
        // 人事组tracy认领任务
        processService.claimTask(approveTasks.get(0).getId(), "tracy");
        Optional<TaskObject> approveTasks2 = processService.getTaskById(approveTasks.get(0).getId());
        assertArrayEquals(new Object[]{
                        true,true
                }
                ,new Object[]{
                        approveTasks2.get().getAssignedUserIds().contains("tracy"),
                        approveTasks2.get().getClaimTime()!=null
                });
        // 人事同意后，流程结束
        variables.put("approve", true);
        List<String> newTasksIds = processService.execute(processId, approveTasks2.get().getId(), "tracy", variables, null);
        ProcessObject process = processService.getProcess(processId);
        // 查询历史记录
        List<TaskObject> historicTasks = processService.getHistoricTasks(processId);
        assertArrayEquals(new Object[]{
                        true, 0, 1,  1l,1l
                }
                ,new Object[]{
                        process.getStatus().equals(ProcessObject.Status.HISTORIC),
                        processService.getTasks(processId).size(),
                        historicTasks.size(),
                        historicTasks.stream().filter(item->item.getName().equals("人事组审批")).count(),
                        historicTasks.stream().filter(item->item.getName().equals("人事组审批")).filter(item->item.getExecutorId().equals("tracy")).count()
                });
    }

    @Test
    void testCeoDisApproveSuccess() throws IOException {
        ProcessDef processDef = deployProcessDef();
        HashMap<Object, Object> variables = Maps.newHashMap();
        variables.put("days", 5);
        // 提交2天的请假申请，走到人事组审批
        String processId = processService.startProcess(processDef.getId(), "james", variables, (tasks, process)->{
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("smith").assigneeType(TaskObjectAssignee.AssigneeType.USER).build()));
            return maps;
        });
        List<TaskObject> approveTasks = processService.getTasks(processId);
        assertArrayEquals(new Object[]{
                        1,true
                }
                ,new Object[]{
                        approveTasks.size(),
                        approveTasks.get(0).getAssignedUserIds().get(0).equals("smith")
                });
        // ceo不同意，退回到【请假节点】，受理人为原来的用户
        variables.put("approve", false);
        List<String> newTasksIds = processService.execute(processId, approveTasks.get(0).getId(), "smith", variables, (taskObjects, processObj)->{
            HashMap<String, List<TaskObjectAssignee>> assignees = Maps.newHashMap();
            assignees.put(taskObjects.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("james").assigneeType(TaskObjectAssignee.AssigneeType.USER).build()));
            return assignees;
        });
        List<TaskObject> newTasks = processService.getTasks(newTasksIds);
        assertArrayEquals(new Object[]{
                        1,true, true
                }
                ,new Object[]{
                        newTasks.size(),
                        newTasks.get(0).getName().equals("请假"),
                        newTasks.get(0).getAssignedUserIds().get(0).equals("james")
                });
    }

    @Test
    public void testCloseProcessInMiddle() throws IOException{
        ProcessDef processDef = deployProcessDef();
        // 开启流程 请假，受理人是james
        HashMap<Object, Object> variables = Maps.newHashMap();
        variables.put("days", 4);
        String processId = processService.startProcess(processDef.getId(), "james", variables, (tasks, process)->{
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId("james").build()));
            return maps;
        });
        List<TaskObject> tasks = processService.getTasks(processId);
        processService.closeProcess(processId, "admin");
        LambdaQueryWrapper<TaskObjectAssignee> taskObjectAssigneeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        taskObjectAssigneeLambdaQueryWrapper.eq(TaskObjectAssignee::getTaskId, tasks.get(0).getId());
        List<TaskObjectAssignee> list = taskObjectAssigneeDao.list(taskObjectAssigneeLambdaQueryWrapper);
        assertArrayEquals(new Object[]{
                        0
                }
                ,new Object[]{
                        list.size()
                });
    }

    @Test
    public void testHistoricTasksQuerySuccess() throws IOException{
        ProcessDef processDef = deployProcessDef();
        // 开启流程 请假，受理人是james
        // 人事审核(james->HR)
        HashMap<Object, Object> variables = Maps.newHashMap();
        variables.put("days", 2);
        String processId = processService.startProcess(processDef.getId(), "james", variables, (tasks, process)->{
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("hr").assigneeType(TaskObjectAssignee.AssigneeType.GROUP).build()));
            return maps;
        });
        List<TaskObject> tasks = processService.getTasks(processId);
        variables.put("approve", false);
        // 请假(Tracy->SMITH)
        List<String> newTaskIds = processService.execute(processId, tasks.get(0).getId(), "tracy", variables, (newTasks, processObject) -> {
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(newTasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("smith").assigneeType(TaskObjectAssignee.AssigneeType.USER).build()));
            return maps;
        });
        // 请假(SMITH->HR)
        newTaskIds = processService.execute(processId, newTaskIds.get(0), "smith", variables, (newTasks, processObject) -> {
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(newTasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeId("hr").assigneeType(TaskObjectAssignee.AssigneeType.GROUP).build()));
            return maps;
        });
        // 查询historicTasks
        Map<String, List<TaskObject>> historicTasksMapKeyTaskDefId = processService.getHistoricTasksMapKeyTaskDefId(processId);
        Map<String, TaskObject> historicRecentTasksMapKeyTaskDef = processService.getHistoricRecentTasksMapKeyTaskDef(processId);

        assertArrayEquals(new Object[]{
                        true
                }
                ,new Object[]{
                        historicRecentTasksMapKeyTaskDef.values().stream().filter(item->item.getName().equals("请假")).findFirst().get().getExecutorId().equals("smith")
                });
    }

    @Test
    public void testDeleteProcessInMiddle() throws IOException{
        ProcessDef processDef = deployProcessDef();
        // 开启流程 请假，受理人是james
        HashMap<Object, Object> variables = Maps.newHashMap();
        variables.put("days", 2);
        String processId = processService.startProcess(processDef.getId(), "james", variables, (tasks, process)->{
            Map<String, List<TaskObjectAssignee>> maps = Maps.newHashMap();
            maps.put(tasks.get(0).getId(), Lists.newArrayList(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId("james").build()));
            return maps;
        });
        List<TaskObject> tasks = processService.getTasks(processId);
        processService.deleteProcess(processId);
        LambdaQueryWrapper<TaskObjectAssignee> taskObjectAssigneeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        taskObjectAssigneeLambdaQueryWrapper.eq(TaskObjectAssignee::getTaskId, tasks.get(0).getId());
        List<TaskObjectAssignee> list = taskObjectAssigneeDao.list(taskObjectAssigneeLambdaQueryWrapper);
        assertArrayEquals(new Object[]{
                        0
                }
                ,new Object[]{
                        list.size()
                });
    }



}

package com.github.bryxtest.workflow;

import com.github.bryxtest.WorkflowApplication;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDef;
import com.github.bryx.workflow.domain.process.buildtime.ProcessDefElement;
import com.github.bryx.workflow.service.process.ProcessDefinitionService;
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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {WorkflowApplication.class})
@Transactional
class ProcessDefinitionTests {

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ResourceLoader resourceLoader;

    private File loadBpmnFile() throws IOException {
        return resourceLoader.getResource("unittest-applyleave.bpmn20.xml").getFile();
    }

    @Test
    void testDeployProcessDefinitionSuccess() throws IOException {
        File file = loadBpmnFile();
        ProcessDef processDef = processDefinitionService.deployProcessDefinition(file.getName(), new FileInputStream(file));
        ProcessDef processDefinition = processDefinitionService.getProcessDefinitionById(processDef.getId());
        Map<ProcessDefElement.ProcessDefElementType, List<ProcessDefElement>> elementsOnDefinition = processDefinitionService.getElementsOnDefinition(processDefinition.getId());
        List<ProcessDefElement> userTaskDefs = elementsOnDefinition.get(ProcessDefElement.ProcessDefElementType.USER_TASK);
        assertArrayEquals(new Object[]{
                        3,1l
                }
                ,new Object[]{
                        userTaskDefs.size(),
                        userTaskDefs.stream().filter(item->item.getName().equals("请假")).count(),
                });
    }

    @Test
    void testWalkThruProcessDefinitionSuccess() throws IOException {
        File file = loadBpmnFile();
        ProcessDef processDef = processDefinitionService.deployProcessDefinition(file.getName(), new FileInputStream(file));
        processDefinitionService.getElementsOnDefinition(processDef.getId(), flowElement->{
            System.out.println(flowElement.getName());
        }, taskDef->{
            System.out.println(taskDef.getName());
        });

        assertArrayEquals(new Object[]{

                }
                ,new Object[]{

                });
    }





}

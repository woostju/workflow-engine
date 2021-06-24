package com.github.bryx.workflow.config;

import com.github.bryx.workflow.service.process.ProcessService;
import com.github.bryx.workflow.service.process.TimerListener;
import com.github.bryx.workflow.util.SpringUtil;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 *
 *
 * 初始化：
 *     1. timer配置
 *     2. SpringUtil设置applicationContext
 *
 * @Author jameswu
 * @Date 2021/6/21
 **/
@Component
public class WorkflowEngineInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    WorkflowEngineProperties workflowEngineProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        SpringUtil.setApplicationContext(contextRefreshedEvent.getApplicationContext());
        if (workflowEngineProperties.isTimerEnable()){
            runtimeService.addEventListener(new TimerListener(SpringUtil.getApplicationContext().getBean(ProcessService.class)), ActivitiEventType.TIMER_FIRED,
                    ActivitiEventType.TIMER_SCHEDULED);
        }
    }
}

package com.github.bryxtest;

import com.github.bryx.workflow.config.WorkflowEngineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.github.bryxtest", "com.github.bryx.workflow"})
public class WorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }

    @Bean
    WorkflowEngineProperties workflowEngineProperties(){
        WorkflowEngineProperties workflowEngineProperties = new WorkflowEngineProperties();
        workflowEngineProperties.setDbType(WorkflowEngineProperties.DbType.MYSQL);
        workflowEngineProperties.setTimerEnable(true);
        return workflowEngineProperties;
    }
}

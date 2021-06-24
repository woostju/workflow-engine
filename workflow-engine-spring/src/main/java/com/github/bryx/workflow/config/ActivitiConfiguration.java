package com.github.bryx.workflow.config;

import org.activiti.engine.*;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringCallerRunsRejectedJobsHandler;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.SpringRejectedJobsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @Author jameswu
 * @Date 2021/6/21
 **/
@Configuration
public class ActivitiConfiguration {

    @Autowired
    WorkflowEngineProperties workflowEngineProperties;

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            PlatformTransactionManager transactionManager,
            DataSource datasource,
            SpringAsyncExecutor asyncExecutor) throws Exception {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setCreateDiagramOnDeploy(false);
        config.setDataSource(datasource);
        config.setTransactionManager(transactionManager);
        if (workflowEngineProperties.isTimerEnable()){
            config.setAsyncExecutorActivate(true);
            config.setAsyncExecutor(asyncExecutor);
        }
        config.setDatabaseType(workflowEngineProperties.getDbType().getActivitiDbType());
        //config.setDatabaseSchemaUpdate(SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE);
        config.setDatabaseSchemaUpdate(SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        return config;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public SpringAsyncExecutor springAsyncExecutor(TaskExecutor taskExecutor) {
        return new SpringAsyncExecutor(taskExecutor, springRejectedJobsHandler());
    }

    @Bean
    public SpringRejectedJobsHandler springRejectedJobsHandler() {
        return new SpringCallerRunsRejectedJobsHandler();
    }



    @Bean
    public ProcessEngine processEngine(SpringProcessEngineConfiguration configure){
        ProcessEngine engine = configure.buildProcessEngine();
        return engine;
    }

    @Bean
    public RuntimeService getRuntimeService(SpringProcessEngineConfiguration config){
        return config.getRuntimeService();
    }

    @Bean
    public TaskService getTaskService(SpringProcessEngineConfiguration config){
        return config.getTaskService();
    }

    @Bean
    public RepositoryService getRepositoryService(SpringProcessEngineConfiguration config){
        return config.getRepositoryService();
    }

    @Bean
    public ManagementService getManagementService(SpringProcessEngineConfiguration config){
        return config.getManagementService();
    }

    @Bean
    public HistoryService getHistoryService(SpringProcessEngineConfiguration config){
        return config.getHistoryService();
    }

    @Bean
    public FormService getFormService(SpringProcessEngineConfiguration config){
        return config.getFormService();
    }
    @Bean
    public IdentityService getIdentityService(SpringProcessEngineConfiguration config){
        return config.getIdentityService();
    }

}

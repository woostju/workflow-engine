##Usage
1. Add dependency in your maven pom:
```
<dependency>
<groupId>com.github.bryx</groupId>
<artifactId>workflow-engine-spring</artifactId>
<version>1.0.1</version>
</dependency>
```
2. Init db with scripts according to you database:
- workflow.mysql.create.sql
- workflow.oracle.create.sql

3. register Spring Bean
- configure WorkflowEngineProperties
```
@Bean
WorkflowEngineProperties workflowEngineProperties(){
    WorkflowEngineProperties workflowEngineProperties = new WorkflowEngineProperties();
    workflowEngineProperties.setDbType(WorkflowEngineProperties.DbType.ORACLE);
    workflowEngineProperties.setTimerEnable(true);
    return workflowEngineProperties;
}
```
- scan package com.github.bryx.workflow
```
@SpringBootApplication(scanBasePackages = {"com.github.bryx.workflow"})
```


##TO-DO: 
1. add comments for code
2. support datasource read and write
3. support cache
4. support parallel gateway
5. support multiple tasks on one task
6. enhance timer, for query
    - close timer after no trigger time
    - create timer instance after triggered


##Releases:
#### v1.0.1
release notes:
1. support database oracle type



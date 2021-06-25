package com.github.bryx.workflow.dto.runtime;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateWorkflowTimerJobDto {
    public enum WorkflowTimerType{
        DURATION,
        CYCLE,
        CRON,
        FIXED_DATE
    }

    @ApiModelProperty(value = "task instance id")
    String workflowTaskInstanceId;

    @ApiModelProperty(value = "activiti bpmn中timer的id", required = true)
    String timerDefinitionId;

    @ApiModelProperty(value = "类型")
    WorkflowTimerType type;

    @ApiModelProperty(value = "cron")
    String cron;

    @ApiModelProperty(value = "延迟时间")
    Integer duration;

    @ApiModelProperty(value = "重复次数")
    Integer repeat;

    @ApiModelProperty(value = "停止时间")
    Date endDate;

    @ApiModelProperty(value = "指定时间")
    Date fixedDate;

    @ApiModelProperty(value = "duration单位", notes = "DAYS, MINUTES, HOURS, SECONDS")
    TimeUnit timeUnit;

    @ApiModelProperty(value = "附属信息")
    Map<String, Object> addon;

    public static CreateWorkflowTimerJobDto ofTypeCron(String workflowTaskInstanceId, String timerDefinitionId, String cron, Date endDate){
        return CreateWorkflowTimerJobDto.builder()
                .workflowTaskInstanceId(workflowTaskInstanceId)
                .timerDefinitionId(timerDefinitionId)
                .cron(cron)
                .endDate(endDate)
                .type(WorkflowTimerType.CRON)
                .build();
    }

    public static CreateWorkflowTimerJobDto ofTypeFixedDate(String workflowTaskInstanceId, String timerDefinitionId, Date fixedDate){
        return CreateWorkflowTimerJobDto.builder()
                .workflowTaskInstanceId(workflowTaskInstanceId)
                .timerDefinitionId(timerDefinitionId)
                .fixedDate(fixedDate)
                .type(WorkflowTimerType.FIXED_DATE)
                .build();
    }

    public static CreateWorkflowTimerJobDto ofTypeCycle(String workflowTaskInstanceId, String timerDefinitionId, Integer repeat, Integer interval, TimeUnit intervalTimeUnit, Date endDate){
        return CreateWorkflowTimerJobDto.builder()
                .workflowTaskInstanceId(workflowTaskInstanceId)
                .timerDefinitionId(timerDefinitionId)
                .endDate(endDate)
                .repeat(repeat)
                .duration(interval)
                .timeUnit(intervalTimeUnit)
                .type(WorkflowTimerType.CYCLE)
                .build();
    }

    public static CreateWorkflowTimerJobDto ofTypeDuration(String workflowTaskInstanceId, String timerDefinitionId, Integer duration, TimeUnit timeUnit ){
        return CreateWorkflowTimerJobDto.builder()
                .workflowTaskInstanceId(workflowTaskInstanceId)
                .timerDefinitionId(timerDefinitionId)
                .duration(duration)
                .timeUnit(timeUnit)
                .type(WorkflowTimerType.DURATION)
                .build();
    }

}

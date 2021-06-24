package com.github.bryx.workflow.dto.runtime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowTimerInstanceDto {

    @ApiModelProperty(value = "activiti process task id", notes = "prefer this instead of workflowTaskInstanceId")
    String processTaskId;

    @ApiModelProperty(value = "workflow task instance id")
    String workflowTaskInstanceId;

    @ApiModelProperty(value = "activiti bpmn中timer的id", required = true)
    String timerDefinitionId;

    @ApiModelProperty(value = "cron")
    String cron;

    @ApiModelProperty(value = "duration")
    Integer duration;

    @ApiModelProperty(value = "duration单位", notes = "DAYS, MINUTES, HOURS, SECONDS")
    TimeUnit timeUnit;
}

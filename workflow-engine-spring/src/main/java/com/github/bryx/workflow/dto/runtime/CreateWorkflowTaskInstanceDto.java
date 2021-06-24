package com.github.bryx.workflow.dto.runtime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowTaskInstanceDto {

    @ApiModelProperty(value = "流程实例id")
    String workflowInstanceId;

    @ApiModelProperty(value = "任务名称")
    String name;

    @ApiModelProperty(value = "activiti流程任务id")
    String processTaskId;

    @ApiModelProperty(value = "activiti流程任务定义id")
    String processTaskDefId;

}

package com.github.bryx.workflow.dto.runtime;

import com.github.bryx.workflow.domain.WorkflowInstance;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkflowInstanceDto {

    @ApiModelProperty(value = "实例id")
    String id;

    @ApiModelProperty(value = "执行人id")
    String executorId;

    @ApiModelProperty(value = "表单数据")
    Map<String, Object> formData;

    @ApiModelProperty(value = "状态")
    WorkflowInstance.WorkflowInstanceStatus status;
}

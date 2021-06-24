package com.github.bryx.workflow.dto.runtime;

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
public class CreateWorkflowInstanceDto{

    @ApiModelProperty(value = "模型定义id")
    String defId;

    @ApiModelProperty(value = "模型定义版本id")
    String defRevId;

    @ApiModelProperty(value = "activiti process id")
    String processId;

    @ApiModelProperty(value = "创建人id")
    String creatorId;

    @ApiModelProperty(value = "表单数据")
    Map<String, Object> formData;
}

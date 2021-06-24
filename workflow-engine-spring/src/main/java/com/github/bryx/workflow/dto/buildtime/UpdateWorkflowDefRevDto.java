package com.github.bryx.workflow.dto.buildtime;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class UpdateWorkflowDefRevDto {
    @ApiModelProperty(value = "版本id")
    String revId;

    @ApiModelProperty(value = "修改人id")
    String modifierId;

    @ApiModelProperty(value="模型表单UI布局")
    ObjectNode moduleUILayout;

    @ApiModelProperty(value="流程配置")
    WorkflowDefProcessConfig processConfig;
}

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
public class UpdateWorkflowDefDraftDto {
    @ApiModelProperty(value = "流程定义id")
    String defId;

    @ApiModelProperty(value = "修改人id")
    String modifierId;

    @ApiModelProperty(value="绑定的流程xml，base64编码")
    String processFlowFileString;

    @ApiModelProperty(value="模型表单UI布局")
    ObjectNode moduleUILayout;

    @ApiModelProperty(value="流程配置")
    WorkflowDefProcessConfig processConfig;
}

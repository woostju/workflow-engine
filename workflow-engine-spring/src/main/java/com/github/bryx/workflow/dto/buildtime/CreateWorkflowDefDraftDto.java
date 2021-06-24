package com.github.bryx.workflow.dto.buildtime;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.bryx.workflow.domain.WorkflowDefProcessConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class CreateWorkflowDefDraftDto {

    @ApiModelProperty(value = "模型定义id", required = false, notes = "在已有流程模型上创建草稿必传")
    String id;

    @ApiModelProperty(value = "模型定义名称", required = false, notes = "只有创建新流程模型时使用")
    String name;

    @ApiModelProperty(value = "activiti流程定义type", required = false, notes = "只有创建新流程模型时使用")
    String processDefType; // applyLeaveWorkflow

    @ApiModelProperty(value = "创建人id", required = true)
    String creatorId;

    @ApiModelProperty(value="发起人用户id", notes = "只有创建新流程模型时使用")
    List<String> initUserIds;

    @ApiModelProperty(value="发起人用户组id", notes = "只有创建新流程模型时使用")
    List<String> initGroupIds;

    @ApiModelProperty(value="绑定的流程xml，base64编码", required = true)
    String processFlowFileString;

    @ApiModelProperty(value="模型表单UI布局", required = true)
    ObjectNode moduleUILayout;

    @ApiModelProperty(value="流程配置", required = true)
    WorkflowDefProcessConfig processConfig;

}

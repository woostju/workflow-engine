package com.github.bryx.workflow.dto.buildtime;

import com.github.bryx.workflow.domain.WorkflowDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class UpdateWorkflowDefDto {

    @ApiModelProperty(value = "id")
    String id;

    @ApiModelProperty(value = "模型定义名称")
    String name;

    @ApiModelProperty(value = "模型定义状态")
    WorkflowDef.WorkflowDefStatus status;

    @ApiModelProperty(hidden = true)
    String modifierId;

    @ApiModelProperty(value="发起人用户id")
    List<String> initiatorIds;

    @ApiModelProperty(value="发起人用户组id")
    List<String> initiatorGroupIds;
}

package com.github.bryx.workflow.dto.buildtime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class PublishWorkflowDefDraftDto {
    @ApiModelProperty(value = "模型定义id")
    String id;

    @ApiModelProperty(value = "操作人id")
    String operatorId;
}

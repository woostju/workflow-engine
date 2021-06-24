package com.github.bryx.workflow.domain;

import java.util.Date;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName(value = "WORKFLOW_DEF")
public class WorkflowDef {
    public enum WorkflowDefStatus{
        ENABLE, // 激活状态
        DISABLE // 禁用状态
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    String id;

    @ApiModelProperty(value = "模型定义名称")
    @TableField(value = "NAME")
    String name;

    @ApiModelProperty(value = "模型定义状态")
    @TableField(value = "STATUS")
    WorkflowDefStatus status;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "CREATE_TIME")
    Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "LAST_MODIFY_TIME")
    Date lastModifyTime;

    @ApiModelProperty(value = "activiti流程定义type")
    @TableField(value = "PROCESS_DEF_TYPE")
    String processDefType; // applyLeaveWorkflow

    @ApiModelProperty(hidden = true)
    @TableField(value = "CREATOR_ID")
    String creatorId;

    @ApiModelProperty(hidden = true)
    @TableField(value = "LAST_MODIFIER_ID")
    String lastModifierId;

    @ApiModelProperty(value = "是否删除")
    @TableField(value = "DELETED")
    Boolean deleted = false;

    @ApiModelProperty(value = "版本")
    @TableField(exist = false)
    WorkflowDefRev rev;
}

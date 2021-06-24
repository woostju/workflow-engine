package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "WORKFLOW_INST", autoResultMap = true)
public class WorkflowInstance {
    public enum WorkflowInstanceStatus{
        ONGOING,
        COMPLETED,
        CLOSED
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    String id;

    @ApiModelProperty(value = "实例序号")
    @TableField(value = "SEQ")
    String seq;

    @ApiModelProperty(value = "状态")
    @TableField(value = "STATUS")
    WorkflowInstanceStatus status;

    @ApiModelProperty(value = "模型定义id")
    @TableField(value = "DEF_ID")
    String defId;

    @ApiModelProperty(value = "模型定义版本id")
    @TableField(value = "DEF_REV_ID")
    String defRevId;

    @ApiModelProperty(value = "activiti process id")
    @TableField(value = "PROCESS_ID")
    String processId;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "CREATE_TIME")
    Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "LAST_MODIFY_TIME")
    Date lastModifyTime;

    @ApiModelProperty(hidden = true)
    @TableField(value = "CREATOR_ID")
    String creatorId;

    @ApiModelProperty(hidden = true)
    @TableField(value = "LAST_MODIFIER_ID")
    String lastModifierId;

    @ApiModelProperty(value = "是否删除")
    @TableField(value = "DELETED")
    Boolean deleted;

    @ApiModelProperty(value = "表单数据")
    @TableField(value = "FORM_DATA", typeHandler = FastjsonTypeHandler.class)
    Map<String, Object> formData;

}

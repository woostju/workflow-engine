package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@TableName(value = "WORKFLOW_INST_ACTIVITY", autoResultMap = true)
public class WorkflowInstanceActivity {
    public enum WorkflowInstanceActivityType {
        SAVE,
        REJECT_BACK
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    String id;

    @ApiModelProperty(value = "任务名称")
    @TableField(value = "NAME")
    String name;

    @ApiModelProperty(value = "类型")
    @TableField(value = "TYPE")
    WorkflowInstanceActivityType type;

    @ApiModelProperty(value = "操作人id")
    @TableField(value = "OPERATOR_ID")
    String operatorId;

    @ApiModelProperty(value = "操作时间")
    @TableField(value = "OCCUR_TIME")
    Date occurTime;

    @ApiModelProperty(value = "流程task id")
    @TableField(value = "WORKFLOW_TASK_ID")
    String taskId;

    @ApiModelProperty(value = "流程id")
    @TableField(value = "WORKFLOW_INST_ID")
    String instanceId;

    @ApiModelProperty(value = "时间内容")
    @TableField(value = "CONTENT", typeHandler = FastjsonTypeHandler.class)
    Map<String, Object> content;

}

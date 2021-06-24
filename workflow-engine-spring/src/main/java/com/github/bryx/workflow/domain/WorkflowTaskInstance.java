package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "WORKFLOW_INST_TASK", autoResultMap = true)
public class WorkflowTaskInstance {
    public enum WorkflowTaskInstanceStatus{
        ONGOING,
        COMPLETED,
        INTERRUPTED
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    String id;

    @ApiModelProperty(value = "任务名称")
    @TableField(value = "NAME")
    String name;

    @ApiModelProperty(value = "流程实例id")
    @TableField(value = "WORKFLOW_INST_ID")
    String workflowInstanceId;

    @ApiModelProperty(value = "状态")
    @TableField(value = "STATUS")
    WorkflowTaskInstanceStatus status;

    @ApiModelProperty(value = "activiti流程任务id")
    @TableField(value = "PROCESS_TASK_ID")
    String processTaskId;

    @ApiModelProperty(value = "activiti流程任务定义id")
    @TableField(value = "PROCESS_TASK_DEF_ID")
    String processTaskDefId;

    @ApiModelProperty(value = "开始时间")
    @TableField(value = "START_TIME")
    Date startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField(value = "END_TIME")
    Date endTime;

    @ApiModelProperty(value = "执行人id")
    @TableField(value = "EXECUTOR_ID")
    String executorId;

    @ApiModelProperty(value = "表单数据")
    @TableField(value = "FORM_DATA", typeHandler = FastjsonTypeHandler.class)
    Map<String, Object> formData;

    @ApiModelProperty(value = "受理人ids")
    @TableField(exist = false)
    List<String> assigneeUserIds;

    @ApiModelProperty(value = "受理组ids")
    @TableField(exist = false)
    List<String> assigneeGroupIds;



}

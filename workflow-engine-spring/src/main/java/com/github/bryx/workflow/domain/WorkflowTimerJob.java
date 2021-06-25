package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.github.bryx.workflow.domain.process.runtime.TaskObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@TableName(value = "WORKFLOW_INST_TIMER_JOB", autoResultMap = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTimerJob {

    public enum Status{
        CLOSED,
        CREATED
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "状态")
    @TableField(value = "STATUS")
    private Status status;

    @ApiModelProperty(value = "状态")
    @TableField(value = "WORKFLOW_INST_ID")
    private String workflowInstanceId;

    @ApiModelProperty(value = "任务id")
    @TableField(value = "WORKFLOW_TASK_ID")
    private String workflowTaskInstanceId;

    @ApiModelProperty(value = "触发时间")
    @TableField(value = "TRIGGER_TIME")
    private Date nextTriggerTime;

    @ApiModelProperty(value = "停止时间")
    @TableField(value = "END_TIME")
    private Date endDate;

    @ApiModelProperty(value = "timer定义id")
    @TableField(value = "PROCESS_TIMER_DEF_ID")
    private String processTimerDefId;

    @ApiModelProperty(value = "timer任务id")
    @TableField(value = "PROCESS_TIMER_JOB_ID")
    private String processTimerJobId;

    @ApiModelProperty(value = "附属信息")
    @TableField(value = "ADD_ON", typeHandler = FastjsonTypeHandler.class)
    private Map<String, Object> addon;

}

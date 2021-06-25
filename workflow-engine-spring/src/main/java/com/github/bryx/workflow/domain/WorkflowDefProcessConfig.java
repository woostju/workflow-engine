package com.github.bryx.workflow.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author jameswu
 * @Date 2021/5/25
 *
 * workflow def config
 **/
@Data
public class WorkflowDefProcessConfig {
    @Data
    public static class UserOperationConfig {
        @ApiModelProperty(value = "操作按钮类型")
        WorkflowUserOperationType type;
        @ApiModelProperty(value = "操作按钮名称")
        String displayName;
    }

    @Data
    public static class FormConfig {
        @ApiModelProperty(value = "流程总表单字段",notes = "是所有节点字段的合集")
        List<WorkflowDefField> fields;
    }

    @Data
    public static class TimerConfig {
        @ApiModelProperty(value = "timerDefinitionId",required = false, hidden = true)
        String timerDefinitionId;
        @ApiModelProperty(value = "repeat")
        Integer repeat;
        @ApiModelProperty(value = "duration")
        Integer duration;
        @ApiModelProperty(value = "duration单位", notes = "DAYS, MINUTES, HOURS, SECONDS")
        TimeUnit timeUnit;
        @ApiModelProperty(value = "timer的配置")
        String extension;
    }

    @Data
    public static class UserTaskConfig {
        @ApiModelProperty("任务定义id")
        String taskDefId;
        @ApiModelProperty("节点表单配置")
        FormConfig form;
        @ApiModelProperty("受理人")
        List<String> assigneeUserIds;
        @ApiModelProperty("受理组")
        List<String> assigneeGroupIds;
        @ApiModelProperty("用户操作")
        List<UserOperationConfig> userActions;
        @ApiModelProperty("计时器配置")
        Map<String, TimerConfig> timer;
    }

    @ApiModelProperty(value = "流程各节点配置")
    Map<String, UserTaskConfig> userTasks;

    @ApiModelProperty(value = "流程表单配置")
    FormConfig form;

    @ApiModelProperty(value = "流程配置版本", hidden = true)
    String version = "1.0";

    @ApiModelProperty(value = "扩展配置")
    Object extension;

}

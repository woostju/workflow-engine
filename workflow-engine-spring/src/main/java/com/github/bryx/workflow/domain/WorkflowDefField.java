package com.github.bryx.workflow.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/5/25
 **/
@Data
public class WorkflowDefField {
    public enum WorkflowFieldType{
        TEXT_FIELD,
        TEXT_AREA,
        RADIO,
        CHECKBOX,
        DROPDOWN_LIST_SINGLE,
        DROPDOWN_LIST_MULTIPLE,
        DATETIME,
        PIC,
        ATTACHMENT
    }
    public enum WorkflowFieldValueType{
        INT_,
        STRING_,
        PASSWORD_,
        FLOAT_,
        BOOLEAN_,
        DOUBLE_,
        DATETIME_,
        USER,
        GROUP,
        USER_GROUP
    }
    @ApiModelProperty(value="id", notes = "不同节点不可变", required = true)
    String id;
    @ApiModelProperty(value="字段控件类型", notes = "不同节点不可变")
    WorkflowFieldType fieldType;
    @ApiModelProperty(value="字段类型", notes = "不同节点不可变")
    WorkflowFieldValueType fieldValueType;

    @ApiModelProperty(value="名称", notes = "不同节点可变")
    String name;
    @ApiModelProperty(value="显示名称", notes = "不同节点可变")
    String displayName;
    @ApiModelProperty(value="placeholder", notes = "不同节点可变")
    String placeholder;
    @ApiModelProperty(value="可选项", notes = "不同节点可变")
    List<Object> options;
    @ApiModelProperty(value="值", notes = "不同节点可变")
    Object value;
    @ApiModelProperty(value="默认值", notes = "不同节点可变")
    Object defaultValue;
    @ApiModelProperty(value="必填", notes = "不同节点可变")
    boolean required = false;
    @ApiModelProperty(value="校验规则表达式", notes = "不同节点可变")
    List<String> validationRegex;
    @ApiModelProperty(value="校验失败提示语", notes = "不同节点可变")
    List<String> validationErrMsg;
    @ApiModelProperty(value="是否可见", notes = "不同节点可变")
    boolean visible = false;
    @ApiModelProperty(value="是否可编辑", notes = "不同节点可变")
    boolean editable = false;

}

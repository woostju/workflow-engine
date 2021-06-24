package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "WORKFLOW_DEF_REV", autoResultMap = true)
public class WorkflowDefRev {
	public enum WorkflowDefRevStatus{
		DRAFT,
		ENABLE,
		DISABLE
	}

	@ApiModelProperty(value="版本id")
	@TableId(value = "ID",type = IdType.ASSIGN_UUID)
	String id;

	@ApiModelProperty(hidden=true)
	@TableField(value = "DEF_ID")
	String defId;

	@ApiModelProperty(value="定义版本")
	@TableField(value = "VERSION")
	Integer version;

	@ApiModelProperty(value="版本状态")
	@TableField(value = "STATUS")
	WorkflowDefRevStatus status;

	@ApiModelProperty(value="创建时间")
	@TableField(value = "CREATE_TIME")
	Date createTime;

	@ApiModelProperty(value="修改时间")
	@TableField(value = "LAST_MODIFY_TIME")
	Date lastModifyTime;

	@ApiModelProperty(hidden=true)
	@TableField(value = "CREATOR_ID")
	String creatorId;

	@ApiModelProperty(hidden=true)
	@TableField(value = "LAST_MODIFIER_ID")
	String lastModifierId;

	@ApiModelProperty(value="绑定的流程xml，base64编码")
	@TableField(value = "PROCESS_FLOW_FILE")
	String processFlowFileString;

	// process definition in activiti
	@ApiModelProperty(value="activiti流程定义版本")
	@TableField(value = "PROCESS_DEF_VERSION")
	Integer processDefVersion; // 1

	@ApiModelProperty(value="activiti流程定义deployment id")
	@TableField(value = "PROCESS_DEF_DEPLOYMENT_ID")
	String processDefDeploymentId; // sample: applyLeaveWorkflow:1:5004

	@ApiModelProperty(value="activiti流程定义definition id")
	@TableField(value = "PROCESS_DEF_ID")
	String processDefId; // 5001

	@ApiModelProperty(value="模型表单UI布局")
	@TableField(value = "PROCESS_UI_CONFIG", typeHandler = FastjsonTypeHandler.class)
	ObjectNode moduleUILayout;

	@ApiModelProperty(value="流程配置")
	@TableField(value = "PROCESS_CONFIG", typeHandler = FastjsonTypeHandler.class)
    WorkflowDefProcessConfig processConfig;

	@ApiModelProperty(value = "是否删除")
	@TableField(value = "DELETED")
	Boolean deleted =false;
}

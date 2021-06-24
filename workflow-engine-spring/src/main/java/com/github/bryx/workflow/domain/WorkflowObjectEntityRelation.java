package com.github.bryx.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName(value = "WORKFLOW_OBJECT_ENTITY_REL")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowObjectEntityRelation {
    public enum RelationType {
        WORKFLOW_INST_INIT_PERMISSION // 工作定义上工作流实例发起权限
    }

    public enum ObjectType {
        WORKFLOW_DEF
    }

    public enum EntityType {
        USER,
        GROUP,
    }

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    String id;

    @ApiModelProperty(value = "对象id")
    @TableField(value = "OBJECT_ID")
    String objectId;

    @ApiModelProperty(value = "对象类型")
    @TableField(value = "OBJECT_TYPE")
    ObjectType objectType;

    @ApiModelProperty(value = "实体id")
    @TableField(value = "ENTITY_ID")
    String entityId;

    @ApiModelProperty(value = "实体类型")
    @TableField(value = "ENTITY_TYPE")
    EntityType entityType;

    @ApiModelProperty(value = "类型")
    @TableField(value = "REL_TYPE")
    RelationType relationType;
}

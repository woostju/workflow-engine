package com.github.bryx.workflow.dto.buildtime;

import com.github.bryx.workflow.dto.PageDto;
import com.github.bryx.workflow.domain.WorkflowDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class QueryWorkflowDefDto<T> extends PageDto<T> {

    @ApiModelProperty("按状态筛选")
    List<WorkflowDef.WorkflowDefStatus> statuses;

    @ApiModelProperty("按defIds筛选")
    Collection<String> defIds;

    @ApiModelProperty("null则返回已删除和未删除的")
    Boolean deleted = false;

    @ApiModelProperty("关键字")
    String keyword;

    @ApiModelProperty("查询initiatorIds可发起的流程")
    List<String> initiatorIds;

    @ApiModelProperty("查询initiatorGroupIds可发起的流程")
    List<String> initiatorGroupIds;
}

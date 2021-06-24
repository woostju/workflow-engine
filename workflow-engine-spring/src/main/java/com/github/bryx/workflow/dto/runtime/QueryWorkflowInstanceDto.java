package com.github.bryx.workflow.dto.runtime;

import com.github.bryx.workflow.dto.PageDto;
import com.github.bryx.workflow.domain.WorkflowInstance;
import lombok.Data;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
public class QueryWorkflowInstanceDto<T> extends PageDto<T> {

    private String keyword;

    private List<String> ids;

    private List<WorkflowInstance.WorkflowInstanceStatus> statuses;
}

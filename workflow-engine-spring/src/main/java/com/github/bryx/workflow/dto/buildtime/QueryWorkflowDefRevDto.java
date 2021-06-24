package com.github.bryx.workflow.dto.buildtime;

import com.github.bryx.workflow.dto.PageDto;
import com.github.bryx.workflow.domain.WorkflowDefRev;
import lombok.Data;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class QueryWorkflowDefRevDto<T> extends PageDto<T> {

    List<WorkflowDefRev.WorkflowDefRevStatus> statuses;

    String defId;

    String defRevId;

    Boolean deleted = false;
}

package com.github.bryx.workflow.dto.buildtime;

import com.github.bryx.workflow.domain.WorkflowDefRev;
import lombok.Data;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Data
public class ChangeWorkflowDefRevStatusDto {
    String defRevId;

    WorkflowDefRev.WorkflowDefRevStatus status;

    String modifierId;
}

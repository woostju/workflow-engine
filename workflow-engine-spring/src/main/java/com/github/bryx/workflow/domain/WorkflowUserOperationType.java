package com.github.bryx.workflow.domain;

/**
 * @Author jameswu
 * @Date 2021/5/25
 **/
public enum WorkflowUserOperationType {
    REJECT_BACK, // 驳回
    REJECT_BACK_ASSIGN, // 驳回并指定受理人
    CLAIM, // 认领
    TRANSFER, // 转移
    MODIFY, // 修改
    SUBMIT, // 提交
    SUBMIT_ASSIGN, // 提交并指定受理人
    CLOSE, // 关闭
}

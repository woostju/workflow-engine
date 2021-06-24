package com.github.bryx.workflow.service.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import com.github.bryx.workflow.mapper.WorkflowTaskInstanceMapper;
import org.springframework.stereotype.Service;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Service
public class WorkflowTaskInstanceDao extends ServiceImpl<WorkflowTaskInstanceMapper, WorkflowTaskInstance> {

}

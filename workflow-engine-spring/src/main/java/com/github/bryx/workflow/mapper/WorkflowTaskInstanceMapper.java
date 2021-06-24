package com.github.bryx.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.bryx.workflow.domain.WorkflowTaskInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author jameswu
 * @Date 2021/6/2
 **/
@Mapper
public interface WorkflowTaskInstanceMapper extends BaseMapper<WorkflowTaskInstance> {

}

package com.github.bryx.workflow.mapper.process;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author jameswu
 * @Date 2021/5/18
 **/
@Mapper
public interface TaskObjectAssigneeMapper extends BaseMapper<TaskObjectAssignee> {
}

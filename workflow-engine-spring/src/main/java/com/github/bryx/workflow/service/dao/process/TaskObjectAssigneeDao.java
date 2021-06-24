package com.github.bryx.workflow.service.dao.process;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bryx.workflow.mapper.process.TaskObjectAssigneeMapper;
import com.github.bryx.workflow.domain.process.runtime.TaskObjectAssignee;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/5/18
 **/
@Service
public class TaskObjectAssigneeDao extends ServiceImpl<TaskObjectAssigneeMapper, TaskObjectAssignee>{

    public void removeByTaskIds(List<String> taskIds){
        LambdaQueryWrapper<TaskObjectAssignee> lambdaQueryWrapper = new LambdaQueryWrapper<TaskObjectAssignee>();
        lambdaQueryWrapper.in(TaskObjectAssignee::getTaskId, taskIds);
        this.remove(lambdaQueryWrapper);
    }
}

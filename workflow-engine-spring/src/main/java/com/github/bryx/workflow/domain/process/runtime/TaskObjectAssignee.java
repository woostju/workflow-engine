package com.github.bryx.workflow.domain.process.runtime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.bryx.workflow.util.CollectionsUtil;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author jameswu
 * @Date 2021/5/18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ACT_TASK_ASSIGNEE")
public class TaskObjectAssignee {
    public enum AssigneeType{
        USER,
        GROUP
    }
    @TableField(value = "TASK_ID_")
    private String taskId;
    @TableField(value = "ASSIGNEE_ID_")
    private String assigneeId;
    @TableField(value = "ASSIGNEE_TYPE_")
    private AssigneeType assigneeType;

    public static List<TaskObjectAssignee> createTaskObjectAssignees(List<String> assigneeUserIs, List<String> assigneeGroupIds){
        if(CollectionsUtil.isNotEmpty(assigneeUserIs) || CollectionsUtil.isNotEmpty(assigneeGroupIds)){
            List<TaskObjectAssignee> taskObjectAssignees = Lists.newArrayList();
            if (CollectionsUtil.isNotEmpty(assigneeUserIs)){
                assigneeUserIs.forEach(userId->{
                    taskObjectAssignees.add(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.USER).assigneeId(userId).build());
                });
            }
            if (CollectionsUtil.isNotEmpty(assigneeGroupIds)){
                assigneeGroupIds.forEach(groupId->{
                    taskObjectAssignees.add(TaskObjectAssignee.builder().assigneeType(TaskObjectAssignee.AssigneeType.GROUP).assigneeId(groupId).build());
                });
            }
            return taskObjectAssignees;
        }
        return null;
    }
}

package com.github.bryx.workflow.command.executor;

import com.github.bryx.workflow.config.WorkflowInstanceInterceptorRegistry;
import com.github.bryx.workflow.command.CommandConfiguration;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.github.bryx.workflow.util.SpringUtil;
import lombok.Data;
import org.apache.commons.lang3.Validate;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Data
public abstract class BaseCommandExecutor<RETURN, COMMAND> {

    private COMMAND command;

    private CommandExecutorHelper commandExecutorHelper;

    public static <RETURN, COMMAND> RETURN execute(COMMAND command){
        CommandConfiguration commandConfiguration = command.getClass().getAnnotation(CommandConfiguration.class);
        Class<? extends BaseCommandExecutor> executorClass = commandConfiguration.executor();
        BaseCommandExecutor<RETURN, COMMAND> executor = SpringUtil.getApplicationContext().getBean(executorClass);
        executor.setCommand(command);
        executor.setCommandExecutorHelper(SpringUtil.getApplicationContext().getBean(CommandExecutorHelper.class));
        return executor.run();
    }

    public abstract RETURN run();

    protected WorkflowInstanceInterceptor getWorkflowInstanceInterceptor(String workflowDefIdentifier){
        WorkflowInstanceInterceptorRegistry workflowInstanceInterceptorRegistry = SpringUtil.getApplicationContext().getBean(WorkflowInstanceInterceptorRegistry.class);
        WorkflowInstanceInterceptor interceptor = workflowInstanceInterceptorRegistry.getInterceptor(workflowDefIdentifier);
        Validate.notNull(interceptor, "please register workflow instance interceptor for %s", workflowDefIdentifier);
        return interceptor;
    }
}

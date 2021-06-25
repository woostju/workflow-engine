package com.github.bryx.workflow.command;

import com.github.bryx.workflow.command.executor.CommandExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author jameswu
 * @Date 2021/6/10
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandConfiguration {
    /**
     * assign executor
     * @return
     */
    Class<? extends CommandExecutor> executor();
}

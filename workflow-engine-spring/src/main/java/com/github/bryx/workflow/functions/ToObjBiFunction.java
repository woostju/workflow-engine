package com.github.bryx.workflow.functions;


/**
 * @Author jameswu
 * @Date 2021/5/18
 **/

@FunctionalInterface
public interface ToObjBiFunction<T, U, R> {
    R apply(T t, U  u);
}

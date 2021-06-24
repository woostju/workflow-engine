package com.github.bryx.workflow.config;

import com.github.bryx.workflow.exception.WorkflowRuntimeException;
import com.github.bryx.workflow.interceptor.Interceptor;
import com.github.bryx.workflow.interceptor.WorkflowInstanceInterceptor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author jameswu
 * @Date 2021/6/11
 **/
@Component
@Slf4j
public class WorkflowInstanceInterceptorRegistry implements InitializingBean {

    @Autowired
    ApplicationContext applicationContext;

    Map<String, WorkflowInstanceInterceptor> workflowInstanceInterceptors = Maps.newHashMap();

    public void register(String workflowDefIdentifier, WorkflowInstanceInterceptor workflowInstanceInterceptor){
        workflowInstanceInterceptors.put(workflowDefIdentifier, workflowInstanceInterceptor);
    }

    public WorkflowInstanceInterceptor getInterceptor(String workflowDefIdentifier){
        return workflowInstanceInterceptors.get(workflowDefIdentifier);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(Interceptor.class);
        for (String beanName : beanNamesForAnnotation){
            Object bean = applicationContext.getBean(beanName);
            if (!(bean instanceof WorkflowInstanceInterceptor)){
                throw new WorkflowRuntimeException(String.format("%s must implement interface $s",bean.getClass(),WorkflowInstanceInterceptor.class));
            }
            String[] processKeys = bean.getClass().getAnnotation(Interceptor.class).processKey();
            for(String processKey : processKeys){
                this.register(processKey, (WorkflowInstanceInterceptor)bean);
                log.debug("register workflow interceptor 【{}】 on process【{}】", bean.getClass(), processKey);
            }
        }
    }


}

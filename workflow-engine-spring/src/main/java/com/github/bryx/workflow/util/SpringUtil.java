package com.github.bryx.workflow.util;

import org.springframework.context.ApplicationContext;

/**
 * @Author jameswu
 * @Date 2021/6/21
 **/
public class SpringUtil {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext){
        SpringUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

}

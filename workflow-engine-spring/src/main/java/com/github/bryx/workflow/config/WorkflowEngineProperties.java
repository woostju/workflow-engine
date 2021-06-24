package com.github.bryx.workflow.config;

import lombok.Data;

@Data
public class WorkflowEngineProperties {
    public enum DbType{
        MYSQL("mysql", "MYSQL"),
        POSTGRES("postgres","POSTGRES"),
        ORACLE("oracle","Oracle11g及以下数据库(高版本推荐使用ORACLE_NEW)"),
        ORACLE_12C("oracle","Oracle12c+数据库");

        String activitiDbType;
        String desc;
        DbType(String activitiDbType, String desc){
            this.activitiDbType = activitiDbType;
            this.desc = desc;
        }

        public String getActivitiDbType() {
            return activitiDbType;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 数据库类型, default mysql
     */
    private DbType dbType = DbType.MYSQL;

    /**
     * 开启计时器功能，开启后将开启asyncExecutor线程池
     * 如果不使用计时器，请关闭
     */
    private boolean timerEnable = true;
}
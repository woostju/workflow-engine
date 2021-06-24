package com.github.bryx.workflow.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 *
 * 独立与外部应用的mybatisplus及mybatis配置，但是数据源使用外部的
 *
 * @Author jameswu
 * @Date 2021/6/21
 **/
@Configuration
@MapperScan(basePackages = {"com.github.bryx.workflow.mapper.process","com.github.bryx.workflow.mapper"},
        sqlSessionFactoryRef = "workflowSqlSessionFactory")
public class ORMConfiguration {

    @Autowired
    WorkflowEngineProperties workflowEngineProperties;

    @Bean(name = "workflowSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setPlugins(paginationInterceptor());
        String mapperResourceLocation = "";
        switch (workflowEngineProperties.getDbType()){
            case MYSQL:
                mapperResourceLocation = "classpath*:/mapper/mysql/*.xml";
                break;
            case ORACLE:
                mapperResourceLocation = "classpath*:/mapper/oracle/*.xml";
                break;
            case ORACLE_12C:
                mapperResourceLocation = "classpath*:/mapper/oracle/*.xml";
                break;
            case POSTGRES:
                mapperResourceLocation = "classpath*:/mapper/postgres/*.xml";
                break;
        }
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperResourceLocation));
        sqlSessionFactory.setConfiguration(mybatisPlusConfiguration());
        sqlSessionFactory.setGlobalConfig(globalConfig());
        return sqlSessionFactory.getObject();
    }

    public MybatisConfiguration mybatisPlusConfiguration() throws Exception {
        MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
        mybatisConfiguration.setMapUnderscoreToCamelCase(true);
        mybatisConfiguration.setCacheEnabled(false);
        return mybatisConfiguration;
    }

    public GlobalConfig globalConfig(){
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        dbConfig.setInsertStrategy(FieldStrategy.NOT_NULL);
        dbConfig.setUpdateStrategy(FieldStrategy.NOT_NULL);
        dbConfig.setSelectStrategy(FieldStrategy.NOT_NULL);
        dbConfig.setTableUnderline(true);
        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }

    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        switch (workflowEngineProperties.getDbType()){
            case MYSQL:
                innerInterceptor.setDbType(DbType.MYSQL);
                break;
            case ORACLE:
                innerInterceptor.setDbType(DbType.ORACLE);
                break;
            case ORACLE_12C:
                innerInterceptor.setDbType(DbType.ORACLE_12C);
                break;
            case POSTGRES:
                innerInterceptor.setDbType(DbType.POSTGRE_SQL);
                break;
        }
        mybatisPlusInterceptor.addInnerInterceptor(innerInterceptor);
        return mybatisPlusInterceptor;
    }
}
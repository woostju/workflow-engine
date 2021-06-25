create table ACT_GE_PROPERTY (
                                 NAME_ NVARCHAR2(64),
                                 VALUE_ NVARCHAR2(300),
                                 REV_ INTEGER,
                                 primary key (NAME_)
);

insert into ACT_GE_PROPERTY
values ('schema.version', '6.0.0.4', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(6.0.0.4)', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ACT_GE_BYTEARRAY (
                                  ID_ NVARCHAR2(64),
                                  REV_ INTEGER,
                                  NAME_ NVARCHAR2(255),
                                  DEPLOYMENT_ID_ NVARCHAR2(64),
                                  BYTES_ BLOB,
                                  GENERATED_ NUMBER(1,0) CHECK (GENERATED_ IN (1,0)),
                                  primary key (ID_)
);

create table ACT_RE_DEPLOYMENT (
                                   ID_ NVARCHAR2(64),
                                   NAME_ NVARCHAR2(255),
                                   CATEGORY_ NVARCHAR2(255),
                                   KEY_ NVARCHAR2(255),
                                   TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                   DEPLOY_TIME_ TIMESTAMP(6),
                                   ENGINE_VERSION_ NVARCHAR2(255),
                                   primary key (ID_)
);

create table ACT_RE_MODEL (
                              ID_ NVARCHAR2(64) not null,
                              REV_ INTEGER,
                              NAME_ NVARCHAR2(255),
                              KEY_ NVARCHAR2(255),
                              CATEGORY_ NVARCHAR2(255),
                              CREATE_TIME_ TIMESTAMP(6),
                              LAST_UPDATE_TIME_ TIMESTAMP(6),
                              VERSION_ INTEGER,
                              META_INFO_ NVARCHAR2(2000),
                              DEPLOYMENT_ID_ NVARCHAR2(64),
                              EDITOR_SOURCE_VALUE_ID_ NVARCHAR2(64),
                              EDITOR_SOURCE_EXTRA_VALUE_ID_ NVARCHAR2(64),
                              TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                              primary key (ID_)
);

create table ACT_RU_EXECUTION (
                                  ID_ NVARCHAR2(64),
                                  REV_ INTEGER,
                                  PROC_INST_ID_ NVARCHAR2(64),
                                  BUSINESS_KEY_ NVARCHAR2(255),
                                  PARENT_ID_ NVARCHAR2(64),
                                  PROC_DEF_ID_ NVARCHAR2(64),
                                  SUPER_EXEC_ NVARCHAR2(64),
                                  ROOT_PROC_INST_ID_ NVARCHAR2(64),
                                  ACT_ID_ NVARCHAR2(255),
                                  IS_ACTIVE_ NUMBER(1,0) CHECK (IS_ACTIVE_ IN (1,0)),
                                  IS_CONCURRENT_ NUMBER(1,0) CHECK (IS_CONCURRENT_ IN (1,0)),
                                  IS_SCOPE_ NUMBER(1,0) CHECK (IS_SCOPE_ IN (1,0)),
                                  IS_EVENT_SCOPE_ NUMBER(1,0) CHECK (IS_EVENT_SCOPE_ IN (1,0)),
                                  IS_MI_ROOT_ NUMBER(1,0) CHECK (IS_MI_ROOT_ IN (1,0)),
                                  SUSPENSION_STATE_ INTEGER,
                                  CACHED_ENT_STATE_ INTEGER,
                                  TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                  NAME_ NVARCHAR2(255),
                                  START_TIME_ TIMESTAMP(6),
                                  START_USER_ID_ NVARCHAR2(255),
                                  LOCK_TIME_ TIMESTAMP(6),
                                  IS_COUNT_ENABLED_ NUMBER(1,0) CHECK (IS_COUNT_ENABLED_ IN (1,0)),
                                  EVT_SUBSCR_COUNT_ INTEGER,
                                  TASK_COUNT_ INTEGER,
                                  JOB_COUNT_ INTEGER,
                                  TIMER_JOB_COUNT_ INTEGER,
                                  SUSP_JOB_COUNT_ INTEGER,
                                  DEADLETTER_JOB_COUNT_ INTEGER,
                                  VAR_COUNT_ INTEGER,
                                  ID_LINK_COUNT_ INTEGER,
                                  primary key (ID_)
);

create table ACT_RU_JOB (
                            ID_ NVARCHAR2(64) NOT NULL,
                            REV_ INTEGER,
                            TYPE_ NVARCHAR2(255) NOT NULL,
                            LOCK_EXP_TIME_ TIMESTAMP(6),
                            LOCK_OWNER_ NVARCHAR2(255),
                            EXCLUSIVE_ NUMBER(1,0) CHECK (EXCLUSIVE_ IN (1,0)),
                            EXECUTION_ID_ NVARCHAR2(64),
                            PROCESS_INSTANCE_ID_ NVARCHAR2(64),
                            PROC_DEF_ID_ NVARCHAR2(64),
                            RETRIES_ INTEGER,
                            EXCEPTION_STACK_ID_ NVARCHAR2(64),
                            EXCEPTION_MSG_ NVARCHAR2(2000),
                            DUEDATE_ TIMESTAMP(6),
                            REPEAT_ NVARCHAR2(255),
                            HANDLER_TYPE_ NVARCHAR2(255),
                            HANDLER_CFG_ NVARCHAR2(2000),
                            TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                            primary key (ID_)
);

create table ACT_RU_TIMER_JOB (
                                  ID_ NVARCHAR2(64) NOT NULL,
                                  REV_ INTEGER,
                                  TYPE_ NVARCHAR2(255) NOT NULL,
                                  LOCK_EXP_TIME_ TIMESTAMP(6),
                                  LOCK_OWNER_ NVARCHAR2(255),
                                  EXCLUSIVE_ NUMBER(1,0) CHECK (EXCLUSIVE_ IN (1,0)),
                                  EXECUTION_ID_ NVARCHAR2(64),
                                  PROCESS_INSTANCE_ID_ NVARCHAR2(64),
                                  PROC_DEF_ID_ NVARCHAR2(64),
                                  RETRIES_ INTEGER,
                                  EXCEPTION_STACK_ID_ NVARCHAR2(64),
                                  EXCEPTION_MSG_ NVARCHAR2(2000),
                                  DUEDATE_ TIMESTAMP(6),
                                  REPEAT_ NVARCHAR2(255),
                                  HANDLER_TYPE_ NVARCHAR2(255),
                                  HANDLER_CFG_ NVARCHAR2(2000),
                                  TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                  primary key (ID_)
);

create table ACT_RU_SUSPENDED_JOB (
                                      ID_ NVARCHAR2(64) NOT NULL,
                                      REV_ INTEGER,
                                      TYPE_ NVARCHAR2(255) NOT NULL,
                                      EXCLUSIVE_ NUMBER(1,0) CHECK (EXCLUSIVE_ IN (1,0)),
                                      EXECUTION_ID_ NVARCHAR2(64),
                                      PROCESS_INSTANCE_ID_ NVARCHAR2(64),
                                      PROC_DEF_ID_ NVARCHAR2(64),
                                      RETRIES_ INTEGER,
                                      EXCEPTION_STACK_ID_ NVARCHAR2(64),
                                      EXCEPTION_MSG_ NVARCHAR2(2000),
                                      DUEDATE_ TIMESTAMP(6),
                                      REPEAT_ NVARCHAR2(255),
                                      HANDLER_TYPE_ NVARCHAR2(255),
                                      HANDLER_CFG_ NVARCHAR2(2000),
                                      TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                      primary key (ID_)
);

create table ACT_RU_DEADLETTER_JOB (
                                       ID_ NVARCHAR2(64) NOT NULL,
                                       REV_ INTEGER,
                                       TYPE_ NVARCHAR2(255) NOT NULL,
                                       EXCLUSIVE_ NUMBER(1,0) CHECK (EXCLUSIVE_ IN (1,0)),
                                       EXECUTION_ID_ NVARCHAR2(64),
                                       PROCESS_INSTANCE_ID_ NVARCHAR2(64),
                                       PROC_DEF_ID_ NVARCHAR2(64),
                                       EXCEPTION_STACK_ID_ NVARCHAR2(64),
                                       EXCEPTION_MSG_ NVARCHAR2(2000),
                                       DUEDATE_ TIMESTAMP(6),
                                       REPEAT_ NVARCHAR2(255),
                                       HANDLER_TYPE_ NVARCHAR2(255),
                                       HANDLER_CFG_ NVARCHAR2(2000),
                                       TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                       primary key (ID_)
);

create table ACT_RE_PROCDEF (
                                ID_ NVARCHAR2(64) NOT NULL,
                                REV_ INTEGER,
                                CATEGORY_ NVARCHAR2(255),
                                NAME_ NVARCHAR2(255),
                                KEY_ NVARCHAR2(255) NOT NULL,
                                VERSION_ INTEGER NOT NULL,
                                DEPLOYMENT_ID_ NVARCHAR2(64),
                                RESOURCE_NAME_ NVARCHAR2(2000),
                                DGRM_RESOURCE_NAME_ varchar(4000),
                                DESCRIPTION_ NVARCHAR2(2000),
                                HAS_START_FORM_KEY_ NUMBER(1,0) CHECK (HAS_START_FORM_KEY_ IN (1,0)),
                                HAS_GRAPHICAL_NOTATION_ NUMBER(1,0) CHECK (HAS_GRAPHICAL_NOTATION_ IN (1,0)),
                                SUSPENSION_STATE_ INTEGER,
                                TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                ENGINE_VERSION_ NVARCHAR2(255),
                                primary key (ID_)
);

create table ACT_RU_TASK (
                             ID_ NVARCHAR2(64),
                             REV_ INTEGER,
                             EXECUTION_ID_ NVARCHAR2(64),
                             PROC_INST_ID_ NVARCHAR2(64),
                             PROC_DEF_ID_ NVARCHAR2(64),
                             NAME_ NVARCHAR2(255),
                             PARENT_TASK_ID_ NVARCHAR2(64),
                             DESCRIPTION_ NVARCHAR2(2000),
                             TASK_DEF_KEY_ NVARCHAR2(255),
                             OWNER_ NVARCHAR2(255),
                             ASSIGNEE_ NVARCHAR2(255),
                             DELEGATION_ NVARCHAR2(64),
                             PRIORITY_ INTEGER,
                             CREATE_TIME_ TIMESTAMP(6),
                             DUE_DATE_ TIMESTAMP(6),
                             CATEGORY_ NVARCHAR2(255),
                             SUSPENSION_STATE_ INTEGER,
                             TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                             FORM_KEY_ NVARCHAR2(255),
                             CLAIM_TIME_ TIMESTAMP(6),
                             primary key (ID_)
);

create table ACT_RU_IDENTITYLINK (
                                     ID_ NVARCHAR2(64),
                                     REV_ INTEGER,
                                     GROUP_ID_ NVARCHAR2(255),
                                     TYPE_ NVARCHAR2(255),
                                     USER_ID_ NVARCHAR2(255),
                                     TASK_ID_ NVARCHAR2(64),
                                     PROC_INST_ID_ NVARCHAR2(64),
                                     PROC_DEF_ID_ NVARCHAR2(64),
                                     primary key (ID_)
);

create table ACT_RU_VARIABLE (
                                 ID_ NVARCHAR2(64) not null,
                                 REV_ INTEGER,
                                 TYPE_ NVARCHAR2(255) not null,
                                 NAME_ NVARCHAR2(255) not null,
                                 EXECUTION_ID_ NVARCHAR2(64),
                                 PROC_INST_ID_ NVARCHAR2(64),
                                 TASK_ID_ NVARCHAR2(64),
                                 BYTEARRAY_ID_ NVARCHAR2(64),
                                 DOUBLE_ NUMBER(*,10),
                                 LONG_ NUMBER(19,0),
                                 TEXT_ NVARCHAR2(2000),
                                 TEXT2_ NVARCHAR2(2000),
                                 primary key (ID_)
);

create table ACT_RU_EVENT_SUBSCR (
                                     ID_ NVARCHAR2(64) not null,
                                     REV_ integer,
                                     EVENT_TYPE_ NVARCHAR2(255) not null,
                                     EVENT_NAME_ NVARCHAR2(255),
                                     EXECUTION_ID_ NVARCHAR2(64),
                                     PROC_INST_ID_ NVARCHAR2(64),
                                     ACTIVITY_ID_ NVARCHAR2(64),
                                     CONFIGURATION_ NVARCHAR2(255),
                                     CREATED_ TIMESTAMP(6) not null,
                                     PROC_DEF_ID_ NVARCHAR2(64),
                                     TENANT_ID_ NVARCHAR2(255) DEFAULT '',
                                     primary key (ID_)
);

create table ACT_EVT_LOG (
                             LOG_NR_ NUMBER(19),
                             TYPE_ NVARCHAR2(64),
                             PROC_DEF_ID_ NVARCHAR2(64),
                             PROC_INST_ID_ NVARCHAR2(64),
                             EXECUTION_ID_ NVARCHAR2(64),
                             TASK_ID_ NVARCHAR2(64),
                             TIME_STAMP_ TIMESTAMP(6) not null,
                             USER_ID_ NVARCHAR2(255),
                             DATA_ BLOB,
                             LOCK_OWNER_ NVARCHAR2(255),
                             LOCK_TIME_ TIMESTAMP(6) null,
                             IS_PROCESSED_ NUMBER(3) default 0,
                             primary key (LOG_NR_)
);

create sequence act_evt_log_seq;

create table ACT_PROCDEF_INFO (
                                  ID_ NVARCHAR2(64) not null,
                                  PROC_DEF_ID_ NVARCHAR2(64) not null,
                                  REV_ integer,
                                  INFO_JSON_ID_ NVARCHAR2(64),
                                  primary key (ID_)
);

create index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_EXEC_ROOT on ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_);

create index ACT_IDX_BYTEAR_DEPL on ACT_GE_BYTEARRAY(DEPLOYMENT_ID_);
alter table ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
        foreign key (DEPLOYMENT_ID_)
            references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
        unique (KEY_,VERSION_, TENANT_ID_);

create index ACT_IDX_EXE_PROCINST on ACT_RU_EXECUTION(PROC_INST_ID_);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST
        foreign key (PROC_INST_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PARENT on ACT_RU_EXECUTION(PARENT_ID_);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT
        foreign key (PARENT_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_SUPER on ACT_RU_EXECUTION(SUPER_EXEC_);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER
        foreign key (SUPER_EXEC_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PROCDEF on ACT_RU_EXECUTION(PROC_DEF_ID_);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_TSKASS_TASK on ACT_RU_IDENTITYLINK(TASK_ID_);
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
        foreign key (TASK_ID_)
            references ACT_RU_TASK (ID_);

create index ACT_IDX_ATHRZ_PROCEDEF  on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_IDL_PROCINST on ACT_RU_IDENTITYLINK(PROC_INST_ID_);
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
        foreign key (PROC_INST_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_EXEC on ACT_RU_TASK(EXECUTION_ID_);
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_PROCINST on ACT_RU_TASK(PROC_INST_ID_);
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
        foreign key (PROC_INST_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_PROCDEF on ACT_RU_TASK(PROC_DEF_ID_);
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCDEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_VAR_EXE on ACT_RU_VARIABLE(EXECUTION_ID_);
alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_EXE
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_VAR_PROCINST on ACT_RU_VARIABLE(PROC_INST_ID_);
alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
        foreign key (PROC_INST_ID_)
            references ACT_RU_EXECUTION(ID_);

create index ACT_IDX_VAR_BYTEARRAY on ACT_RU_VARIABLE(BYTEARRAY_ID_);
alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_BYTEARRAY
        foreign key (BYTEARRAY_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_JOB_EXECUTION_ID on ACT_RU_JOB(EXECUTION_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_EXECUTION
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_JOB_PROC_INST_ID on ACT_RU_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROCESS_INSTANCE
        foreign key (PROCESS_INSTANCE_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_JOB_PROC_DEF_ID on ACT_RU_JOB(PROC_DEF_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROC_DEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_JOB_EXCEPTION on ACT_RU_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_EXCEPTION
        foreign key (EXCEPTION_STACK_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_TJOB_EXECUTION_ID on ACT_RU_TIMER_JOB(EXECUTION_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TJOB_EXECUTION
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TJOB_PROC_INST_ID on ACT_RU_TIMER_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TJOB_PROCESS_INSTANCE
        foreign key (PROCESS_INSTANCE_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TJOB_PROC_DEF_ID on ACT_RU_TIMER_JOB(PROC_DEF_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TJOB_PROC_DEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_TJOB_EXCEPTION on ACT_RU_TIMER_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TJOB_EXCEPTION
        foreign key (EXCEPTION_STACK_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_SJOB_EXECUTION_ID on ACT_RU_SUSPENDED_JOB(EXECUTION_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SJOB_EXECUTION
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_SJOB_PROC_INST_ID on ACT_RU_SUSPENDED_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SJOB_PROCESS_INSTANCE
        foreign key (PROCESS_INSTANCE_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_SJOB_PROC_DEF_ID on ACT_RU_SUSPENDED_JOB(PROC_DEF_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SJOB_PROC_DEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_SJOB_EXCEPTION on ACT_RU_SUSPENDED_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SJOB_EXCEPTION
        foreign key (EXCEPTION_STACK_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_DJOB_EXECUTION_ID on ACT_RU_DEADLETTER_JOB(EXECUTION_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DJOB_EXECUTION
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_DJOB_PROC_INST_ID on ACT_RU_DEADLETTER_JOB(PROCESS_INSTANCE_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DJOB_PROCESS_INSTANCE
        foreign key (PROCESS_INSTANCE_ID_)
            references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_DJOB_PROC_DEF_ID on ACT_RU_DEADLETTER_JOB(PROC_DEF_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DJOB_PROC_DEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

create index ACT_IDX_DJOB_EXCEPTION on ACT_RU_DEADLETTER_JOB(EXCEPTION_STACK_ID_);
alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DJOB_EXCEPTION
        foreign key (EXCEPTION_STACK_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_EVENT_SUBSCR on ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
        foreign key (EXECUTION_ID_)
            references ACT_RU_EXECUTION(ID_);

create index ACT_IDX_MODEL_SOURCE on ACT_RE_MODEL(EDITOR_SOURCE_VALUE_ID_);
alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE
        foreign key (EDITOR_SOURCE_VALUE_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_SOURCE_EXTRA on ACT_RE_MODEL(EDITOR_SOURCE_EXTRA_VALUE_ID_);
alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE_EXTRA
        foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_DEPLOYMENT on ACT_RE_MODEL(DEPLOYMENT_ID_);
alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_DEPLOYMENT
        foreign key (DEPLOYMENT_ID_)
            references ACT_RE_DEPLOYMENT (ID_);

create index ACT_IDX_PROCDEF_INFO_JSON on ACT_PROCDEF_INFO(INFO_JSON_ID_);
alter table ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_JSON_BA
        foreign key (INFO_JSON_ID_)
            references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_PROCDEF_INFO_PROC on ACT_PROCDEF_INFO(PROC_DEF_ID_);
alter table ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_PROCDEF
        foreign key (PROC_DEF_ID_)
            references ACT_RE_PROCDEF (ID_);

alter table ACT_PROCDEF_INFO
    add constraint ACT_UNIQ_INFO_PROCDEF
        unique (PROC_DEF_ID_);


create table ACT_HI_PROCINST (
                                 ID_ NVARCHAR2(64) not null,
                                 PROC_INST_ID_ NVARCHAR2(64) not null,
                                 BUSINESS_KEY_ NVARCHAR2(255),
                                 PROC_DEF_ID_ NVARCHAR2(64) not null,
                                 START_TIME_ TIMESTAMP(6) not null,
                                 END_TIME_ TIMESTAMP(6),
                                 DURATION_ NUMBER(19,0),
                                 START_USER_ID_ NVARCHAR2(255),
                                 START_ACT_ID_ NVARCHAR2(255),
                                 END_ACT_ID_ NVARCHAR2(255),
                                 SUPER_PROCESS_INSTANCE_ID_ NVARCHAR2(64),
                                 DELETE_REASON_ NVARCHAR2(2000),
                                 TENANT_ID_ NVARCHAR2(255) default '',
                                 NAME_ NVARCHAR2(255),
                                 primary key (ID_),
                                 unique (PROC_INST_ID_)
);

create table ACT_HI_ACTINST (
                                ID_ NVARCHAR2(64) not null,
                                PROC_DEF_ID_ NVARCHAR2(64) not null,
                                PROC_INST_ID_ NVARCHAR2(64) not null,
                                EXECUTION_ID_ NVARCHAR2(64) not null,
                                ACT_ID_ NVARCHAR2(255) not null,
                                TASK_ID_ NVARCHAR2(64),
                                CALL_PROC_INST_ID_ NVARCHAR2(64),
                                ACT_NAME_ NVARCHAR2(255),
                                ACT_TYPE_ NVARCHAR2(255) not null,
                                ASSIGNEE_ NVARCHAR2(255),
                                START_TIME_ TIMESTAMP(6) not null,
                                END_TIME_ TIMESTAMP(6),
                                DURATION_ NUMBER(19,0),
                                DELETE_REASON_ NVARCHAR2(2000),
                                TENANT_ID_ NVARCHAR2(255) default '',
                                primary key (ID_)
);

create table ACT_HI_TASKINST (
                                 ID_ NVARCHAR2(64) not null,
                                 PROC_DEF_ID_ NVARCHAR2(64),
                                 TASK_DEF_KEY_ NVARCHAR2(255),
                                 PROC_INST_ID_ NVARCHAR2(64),
                                 EXECUTION_ID_ NVARCHAR2(64),
                                 PARENT_TASK_ID_ NVARCHAR2(64),
                                 NAME_ NVARCHAR2(255),
                                 DESCRIPTION_ NVARCHAR2(2000),
                                 OWNER_ NVARCHAR2(255),
                                 ASSIGNEE_ NVARCHAR2(255),
                                 START_TIME_ TIMESTAMP(6) not null,
                                 CLAIM_TIME_ TIMESTAMP(6),
                                 END_TIME_ TIMESTAMP(6),
                                 DURATION_ NUMBER(19,0),
                                 DELETE_REASON_ NVARCHAR2(2000),
                                 PRIORITY_ INTEGER,
                                 DUE_DATE_ TIMESTAMP(6),
                                 FORM_KEY_ NVARCHAR2(255),
                                 CATEGORY_ NVARCHAR2(255),
                                 TENANT_ID_ NVARCHAR2(255) default '',
                                 primary key (ID_)
);

create table ACT_HI_VARINST (
                                ID_ NVARCHAR2(64) not null,
                                PROC_INST_ID_ NVARCHAR2(64),
                                EXECUTION_ID_ NVARCHAR2(64),
                                TASK_ID_ NVARCHAR2(64),
                                NAME_ NVARCHAR2(255) not null,
                                VAR_TYPE_ NVARCHAR2(100),
                                REV_ INTEGER,
                                BYTEARRAY_ID_ NVARCHAR2(64),
                                DOUBLE_ NUMBER(*,10),
                                LONG_ NUMBER(19,0),
                                TEXT_ NVARCHAR2(2000),
                                TEXT2_ NVARCHAR2(2000),
                                CREATE_TIME_ TIMESTAMP(6),
                                LAST_UPDATED_TIME_ TIMESTAMP(6),
                                primary key (ID_)
);

create table ACT_HI_DETAIL (
                               ID_ NVARCHAR2(64) not null,
                               TYPE_ NVARCHAR2(255) not null,
                               PROC_INST_ID_ NVARCHAR2(64),
                               EXECUTION_ID_ NVARCHAR2(64),
                               TASK_ID_ NVARCHAR2(64),
                               ACT_INST_ID_ NVARCHAR2(64),
                               NAME_ NVARCHAR2(255) not null,
                               VAR_TYPE_ NVARCHAR2(64),
                               REV_ INTEGER,
                               TIME_ TIMESTAMP(6) not null,
                               BYTEARRAY_ID_ NVARCHAR2(64),
                               DOUBLE_ NUMBER(*,10),
                               LONG_ NUMBER(19,0),
                               TEXT_ NVARCHAR2(2000),
                               TEXT2_ NVARCHAR2(2000),
                               primary key (ID_)
);

create table ACT_HI_COMMENT (
                                ID_ NVARCHAR2(64) not null,
                                TYPE_ NVARCHAR2(255),
                                TIME_ TIMESTAMP(6) not null,
                                USER_ID_ NVARCHAR2(255),
                                TASK_ID_ NVARCHAR2(64),
                                PROC_INST_ID_ NVARCHAR2(64),
                                ACTION_ NVARCHAR2(255),
                                MESSAGE_ NVARCHAR2(2000),
                                FULL_MSG_ BLOB,
                                primary key (ID_)
);

create table ACT_HI_ATTACHMENT (
                                   ID_ NVARCHAR2(64) not null,
                                   REV_ INTEGER,
                                   USER_ID_ NVARCHAR2(255),
                                   NAME_ NVARCHAR2(255),
                                   DESCRIPTION_ NVARCHAR2(2000),
                                   TYPE_ NVARCHAR2(255),
                                   TASK_ID_ NVARCHAR2(64),
                                   PROC_INST_ID_ NVARCHAR2(64),
                                   URL_ NVARCHAR2(2000),
                                   CONTENT_ID_ NVARCHAR2(64),
                                   TIME_ TIMESTAMP(6),
                                   primary key (ID_)
);

create table ACT_HI_IDENTITYLINK (
                                     ID_ NVARCHAR2(64),
                                     GROUP_ID_ NVARCHAR2(255),
                                     TYPE_ NVARCHAR2(255),
                                     USER_ID_ NVARCHAR2(255),
                                     TASK_ID_ NVARCHAR2(64),
                                     PROC_INST_ID_ NVARCHAR2(64),
                                     primary key (ID_)
);

create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_);

create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_TASK_INST_PROCINST on ACT_HI_TASKINST(PROC_INST_ID_);

create table ACT_ID_GROUP (
                              ID_ NVARCHAR2(64),
                              REV_ INTEGER,
                              NAME_ NVARCHAR2(255),
                              TYPE_ NVARCHAR2(255),
                              primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
                                   USER_ID_ NVARCHAR2(64),
                                   GROUP_ID_ NVARCHAR2(64),
                                   primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
                             ID_ NVARCHAR2(64),
                             REV_ INTEGER,
                             FIRST_ NVARCHAR2(255),
                             LAST_ NVARCHAR2(255),
                             EMAIL_ NVARCHAR2(255),
                             PWD_ NVARCHAR2(255),
                             PICTURE_ID_ NVARCHAR2(64),
                             primary key (ID_)
);

create table ACT_ID_INFO (
                             ID_ NVARCHAR2(64),
                             REV_ INTEGER,
                             USER_ID_ NVARCHAR2(64),
                             TYPE_ NVARCHAR2(64),
                             KEY_ NVARCHAR2(255),
                             VALUE_ NVARCHAR2(255),
                             PASSWORD_ BLOB,
                             PARENT_ID_ NVARCHAR2(255),
                             primary key (ID_)
);

create index ACT_IDX_MEMB_GROUP on ACT_ID_MEMBERSHIP(GROUP_ID_);
alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_GROUP
        foreign key (GROUP_ID_)
            references ACT_ID_GROUP (ID_);

create index ACT_IDX_MEMB_USER on ACT_ID_MEMBERSHIP(USER_ID_);
alter table ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_USER
        foreign key (USER_ID_)
            references ACT_ID_USER (ID_);


-- ----------------------------
-- Table structure for ACT_TASK_ASSIGNEE
-- ----------------------------

create table ACT_TASK_ASSIGNEE (
     TASK_ID_ NVARCHAR2(64) NOT NULL,
     ASSIGNEE_ID_ NVARCHAR2(255) NOT NULL,
     ASSIGNEE_TYPE_ NVARCHAR2(10) NOT NULL
);

COMMENT ON COLUMN ACT_TASK_ASSIGNEE.TASK_ID_ IS '任务ID';
COMMENT ON COLUMN ACT_TASK_ASSIGNEE.ASSIGNEE_ID_ IS '受理人id';
COMMENT ON COLUMN ACT_TASK_ASSIGNEE.ASSIGNEE_TYPE_ IS '受理人类型';

-- ----------------------------
-- Table structure for WORKFLOW_DEF
-- ----------------------------

CREATE TABLE WORKFLOW_DEF (
    ID NVARCHAR2(64) NOT NULL,
    NAME NVARCHAR2(255) NOT NULL,
    STATUS NVARCHAR2(255) NOT NULL,
    CREATOR_ID NVARCHAR2(255) NOT NULL,
    LAST_MODIFIER_ID NVARCHAR2(255) NULL,
    CREATE_TIME TIMESTAMP(6)  NULL,
    LAST_MODIFY_TIME TIMESTAMP(6)  NULL,
    DELETED NUMBER(1,0)  NOT NULL,
    PROCESS_DEF_TYPE NVARCHAR2(255)  NULL,
    primary key (ID)
);

COMMENT ON COLUMN WORKFLOW_DEF.NAME IS '名称';
COMMENT ON COLUMN WORKFLOW_DEF.STATUS IS '状态';
COMMENT ON COLUMN WORKFLOW_DEF.CREATOR_ID IS '创建人';
COMMENT ON COLUMN WORKFLOW_DEF.LAST_MODIFIER_ID IS '最后修改人';
COMMENT ON COLUMN WORKFLOW_DEF.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN WORKFLOW_DEF.LAST_MODIFY_TIME IS '最近修改时间';
COMMENT ON COLUMN WORKFLOW_DEF.DELETED IS '是否删除';
COMMENT ON COLUMN WORKFLOW_DEF.PROCESS_DEF_TYPE IS 'ACTIVITI 流程定义类型';

-- ----------------------------
-- Table structure for WORKFLOW_DEF_REV
-- ----------------------------

CREATE TABLE WORKFLOW_DEF_REV (
        ID NVARCHAR2(64) NOT NULL,
        DEF_ID NVARCHAR2(64) NOT NULL,
        VERSION INTEGER NULL,
        STATUS NVARCHAR2(255) NOT NULL,
        CREATE_TIME TIMESTAMP(6) NOT NULL,
        LAST_MODIFY_TIME TIMESTAMP(6) NULL,
        CREATOR_ID NVARCHAR2(255) NOT NULL,
        LAST_MODIFIER_ID NVARCHAR2(255) NULL,
        PROCESS_FLOW_FILE CLOB NULL,
        PROCESS_DEF_VERSION INTEGER NULL,
        PROCESS_DEF_DEPLOYMENT_ID NVARCHAR2(255) NULL,
        PROCESS_DEF_ID NVARCHAR2(255) NULL,
        PROCESS_CONFIG CLOB NULL,
        PROCESS_UI_CONFIG CLOB NULL,
        DELETED NUMBER(1,0) NOT NULL,
        PRIMARY KEY (ID)
);

COMMENT ON COLUMN WORKFLOW_DEF_REV.DEF_ID IS '流程定义id';
COMMENT ON COLUMN WORKFLOW_DEF_REV.VERSION IS '版本号';
COMMENT ON COLUMN WORKFLOW_DEF_REV.STATUS IS '状态';
COMMENT ON COLUMN WORKFLOW_DEF_REV.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN WORKFLOW_DEF_REV.LAST_MODIFY_TIME IS '最后修改时间';
COMMENT ON COLUMN WORKFLOW_DEF_REV.CREATOR_ID IS '创建人';
COMMENT ON COLUMN WORKFLOW_DEF_REV.LAST_MODIFIER_ID IS '最后修改人';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_FLOW_FILE IS '流程BPMN文件';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_DEF_VERSION IS 'ACTIVITI 流程定义版本';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_DEF_DEPLOYMENT_ID IS 'ACTIVITI 流程定义部署id';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_DEF_ID IS 'ACTIVITI 流程定义id';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_CONFIG IS '流程配置';
COMMENT ON COLUMN WORKFLOW_DEF_REV.PROCESS_UI_CONFIG IS 'UI LAYOUT 前端配置';
COMMENT ON COLUMN WORKFLOW_DEF_REV.DELETED IS '是否删除';

-- ----------------------------
-- Table structure for WORKFLOW_INST
-- ----------------------------
CREATE TABLE WORKFLOW_INST (
     ID NVARCHAR2(64) NOT NULL,
     SEQ NVARCHAR2(255) NULL,
     STATUS NVARCHAR2(255) NOT NULL,
     DEF_ID NVARCHAR2(64) NOT NULL,
     DEF_REV_ID NVARCHAR2(64) NOT NULL,
     PROCESS_ID NVARCHAR2(64) NOT NULL,
     FORM_DATA CLOB NOT NULL,
     CREATOR_ID NVARCHAR2(255) NOT NULL,
     CREATE_TIME TIMESTAMP(6) NOT NULL,
     LAST_MODIFIER_ID NVARCHAR2(255) NULL,
     LAST_MODIFY_TIME TIMESTAMP(6) NULL,
     DELETED NUMBER(1,0) NOT NULL,
     PRIMARY KEY (ID)
);

COMMENT ON COLUMN WORKFLOW_INST.ID IS 'ID';
COMMENT ON COLUMN WORKFLOW_INST.SEQ IS '序号';
COMMENT ON COLUMN WORKFLOW_INST.STATUS IS '状态';
COMMENT ON COLUMN WORKFLOW_INST.DEF_ID IS '流程定义ID';
COMMENT ON COLUMN WORKFLOW_INST.DEF_REV_ID IS '流程定义版本ID';
COMMENT ON COLUMN WORKFLOW_INST.PROCESS_ID IS 'ACTIVITI PROCESS ID';
COMMENT ON COLUMN WORKFLOW_INST.FORM_DATA IS '表单数据';
COMMENT ON COLUMN WORKFLOW_INST.CREATOR_ID IS '创建人';
COMMENT ON COLUMN WORKFLOW_INST.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN WORKFLOW_INST.LAST_MODIFIER_ID IS '最后修改人';
COMMENT ON COLUMN WORKFLOW_INST.LAST_MODIFY_TIME IS '最后修改时间';
COMMENT ON COLUMN WORKFLOW_INST.DELETED IS '是否删除';

-- ----------------------------
-- Table structure for WORKFLOW_INST_ACTIVITY
-- ----------------------------
CREATE TABLE WORKFLOW_INST_ACTIVITY (
      ID NVARCHAR2(64) NOT NULL,
      NAME NVARCHAR2(255) NOT NULL,
      TYPE NVARCHAR2(255) NOT NULL,
      OPERATOR_ID NVARCHAR2(255) NOT NULL,
      OPERATE_TIME TIMESTAMP(6) NOT NULL,
      CONTENT CLOB NOT NULL,
      WORKFLOW_TASK_ID NVARCHAR2(64) NULL,
      WORKFLOW_INST_ID NVARCHAR2(64) NULL,
      PRIMARY KEY (ID)
);
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.ID IS 'ID';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.NAME IS '名称';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.TYPE IS '类型';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.OPERATOR_ID IS '操作人id';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.OPERATE_TIME IS '操作时间';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.CONTENT IS '操作内容';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.WORKFLOW_TASK_ID IS 'workflow任务ID';
COMMENT ON COLUMN WORKFLOW_INST_ACTIVITY.WORKFLOW_INST_ID IS 'workflow实例ID';


-- ----------------------------
-- Table structure for WORKFLOW_INST_TASK
-- ----------------------------
CREATE TABLE WORKFLOW_INST_TASK (
      ID NVARCHAR2(64) NOT NULL ,
      NAME NVARCHAR2(255) NOT NULL,
      WORKFLOW_INST_ID NVARCHAR2(64) NOT NULL,
      PROCESS_TASK_ID NVARCHAR2(255) NOT NULL,
      PROCESS_TASK_DEF_ID NVARCHAR2(64) NOT NULL,
      STATUS NVARCHAR2(255) NOT NULL,
      TYPE NVARCHAR2(64) NULL,
      EXECUTOR_ID NVARCHAR2(255) NULL,
      START_TIME TIMESTAMP(6) NOT NULL,
      END_TIME TIMESTAMP(6) NULL,
      FORM_DATA CLOB NULL,
      PRIMARY KEY (ID)
);

COMMENT ON COLUMN WORKFLOW_INST_TASK.ID IS 'ID';
COMMENT ON COLUMN WORKFLOW_INST_TASK.NAME IS '名称';
COMMENT ON COLUMN WORKFLOW_INST_TASK.WORKFLOW_INST_ID IS '流程实例id';
COMMENT ON COLUMN WORKFLOW_INST_TASK.PROCESS_TASK_ID IS 'ACTIVITI TASK ID';
COMMENT ON COLUMN WORKFLOW_INST_TASK.PROCESS_TASK_DEF_ID IS 'ACTIVITI TASK定义ID';
COMMENT ON COLUMN WORKFLOW_INST_TASK.STATUS IS '状态';
COMMENT ON COLUMN WORKFLOW_INST_TASK.TYPE IS '类型';
COMMENT ON COLUMN WORKFLOW_INST_TASK.EXECUTOR_ID IS '执行人id';
COMMENT ON COLUMN WORKFLOW_INST_TASK.START_TIME IS '开始时间';
COMMENT ON COLUMN WORKFLOW_INST_TASK.END_TIME IS '结束时间';
COMMENT ON COLUMN WORKFLOW_INST_TASK.FORM_DATA IS '提交时的表单数据';

-- ----------------------------
-- Table structure for WORKFLOW_OBJECT_ENTITY_REL
-- ----------------------------
CREATE TABLE WORKFLOW_OBJECT_ENTITY_REL (
      ID NVARCHAR2(255) NOT NULL ,
      OBJECT_ID NVARCHAR2(255) NOT NULL ,
      ENTITY_ID NVARCHAR2(255) NOT NULL ,
      ENTITY_TYPE NVARCHAR2(255) NOT NULL ,
      OBJECT_TYPE NVARCHAR2(255) NOT NULL ,
      REL_TYPE NVARCHAR2(255) NOT NULL ,
      PRIMARY KEY (ID)
);

COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.ID IS 'ID';
COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.OBJECT_ID IS '对象ID';
COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.ENTITY_ID IS '实体ID';
COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.ENTITY_TYPE IS '实体类型';
COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.OBJECT_TYPE IS '对象类型';
COMMENT ON COLUMN WORKFLOW_OBJECT_ENTITY_REL.REL_TYPE IS '关系类型';
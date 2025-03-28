CREATE DATABASE zhihuiwen_data;
-- 使用数据库
USE zhihuiwen_data;

create table report
(
    id               bigint auto_increment
        primary key,
    questionnaire_id bigint    null,
    content          text      null,
    created_at       timestamp null
)
    collate = utf8mb4_unicode_ci;

create table total_report
(
    id         bigint auto_increment
        primary key,
    content    text      null,
    user_id    bigint    null,
    created_at timestamp null
);

create table user
(
    id         bigint unsigned auto_increment comment '用户唯一标识'
        primary key,
    avatar     varchar(255) default '/default-avatar.png' not null comment '用户头像URL',
    email      varchar(254)                               not null comment '用户邮箱（RFC 5321标准）',
    username   varchar(30)                                not null comment '用户名（唯一）',
    password   char(255)                                  not null comment 'BCrypt加密后的密码（固定255字符）',
    created_at timestamp    default CURRENT_TIMESTAMP     not null comment '创建时间',
    updated_at timestamp    default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint email
        unique (email),
    constraint username
        unique (username)
)
    comment '系统用户表' collate = utf8mb4_unicode_ci;

create table category
(
    id                  bigint unsigned auto_increment comment '分类唯一标识'
        primary key,
    user_id             bigint unsigned                           not null comment '所属用户ID',
    name                varchar(100)                              not null comment '分类名称',
    catalog             enum ('questionnaire', 'template')        not null comment '分类目录类型',
    description         varchar(500)                              null comment '分类描述（限500字符）',
    questionnaire_count bigint unsigned default 0                 not null comment '分类下问卷数量',
    created_at          timestamp       default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at          timestamp       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_category_unique
        unique (user_id, name, catalog),
    constraint category_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade
)
    comment '统一分类表（问卷/模板）' collate = utf8mb4_unicode_ci;

create index idx_category_catalog
    on category (catalog);

create table questionnaire
(
    id          bigint unsigned auto_increment comment '问卷唯一标识'
        primary key,
    category_id bigint unsigned                                                 null comment '分类ID',
    user_id     bigint unsigned                                                 not null comment '创建用户ID',
    title       varchar(255)                                                    not null comment '问卷标题',
    description varchar(500)                                                    null comment '问卷描述（限500字符）',
    status      enum ('draft', 'published', 'closed') default 'draft'           not null comment '问卷状态',
    start_time  datetime                                                        null comment '开始时间',
    end_time    datetime                                                        null comment '结束时间',
    created_at  timestamp                             default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  timestamp                             default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint questionnaire_ibfk_1
        foreign key (category_id) references category (id)
            on delete set null,
    constraint questionnaire_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
)
    comment '问卷表' collate = utf8mb4_unicode_ci;

create index category_id
    on questionnaire (category_id);

create index idx_questionnaire_status
    on questionnaire (status);

create index idx_questionnaire_time
    on questionnaire (start_time, end_time);

create index user_id
    on questionnaire (user_id);

create definer = root@localhost trigger update_category_count_on_delete
    after delete
    on questionnaire
    for each row
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
    END IF;
END;

create definer = root@localhost trigger update_category_count_on_insert
    after insert
    on questionnaire
    for each row
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
    END IF;
END;

create definer = root@localhost trigger update_category_count_on_update
    after update
    on questionnaire
    for each row
BEGIN
    IF OLD.category_id <> NEW.category_id
        OR (OLD.category_id IS NULL AND NEW.category_id IS NOT NULL)
        OR (OLD.category_id IS NOT NULL AND NEW.category_id IS NULL) THEN
        IF OLD.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
        END IF;
        IF NEW.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
        END IF;
    END IF;
END;

create table questionnaire_question
(
    id               bigint unsigned auto_increment comment '问题唯一标识'
        primary key,
    questionnaire_id bigint unsigned                               not null comment '所属问卷ID',
    question_text    varchar(1000)                                 not null comment '问题内容（限1000字符）',
    question_type    enum ('single', 'multiple', 'text', 'rating') not null comment '问题类型',
    sort_order       smallint unsigned default 0                   not null comment '排序序号（0-32767）',
    constraint questionnaire_question_ibfk_1
        foreign key (questionnaire_id) references questionnaire (id)
            on delete cascade
)
    comment '问卷问题表' collate = utf8mb4_unicode_ci;

create table questionnaire_option
(
    id          bigint unsigned auto_increment comment '选项唯一标识'
        primary key,
    question_id bigint unsigned             not null comment '所属问题ID',
    option_text varchar(500)                not null comment '选项内容（限500字符）',
    sort_order  smallint unsigned default 0 not null comment '排序序号（0-32767）',
    constraint questionnaire_option_ibfk_1
        foreign key (question_id) references questionnaire_question (id)
            on delete cascade
)
    comment '问卷选项表' collate = utf8mb4_unicode_ci;

create index idx_questionnaireoption_order
    on questionnaire_option (sort_order);

create index question_id
    on questionnaire_option (question_id);

create index idx_questionnairequestion_order
    on questionnaire_question (sort_order);

create index questionnaire_id
    on questionnaire_question (questionnaire_id);

create table response
(
    id               bigint unsigned auto_increment comment '答卷唯一标识'
        primary key,
    questionnaire_id bigint unsigned                     not null comment '问卷ID',
    user_id          bigint unsigned                     null comment '提交用户ID',
    duration         smallint unsigned                   null comment '填写耗时（秒）',
    ip_address       varchar(45)                         null comment '提交IP地址',
    user_agent       varchar(500)                        null comment '浏览器标识',
    submitted_at     timestamp default CURRENT_TIMESTAMP not null comment '提交时间',
    constraint response_ibfk_1
        foreign key (questionnaire_id) references questionnaire (id)
            on delete cascade,
    constraint response_ibfk_2
        foreign key (user_id) references user (id)
            on delete set null
)
    comment '问卷答卷表' collate = utf8mb4_unicode_ci;

create table answer
(
    id             bigint unsigned auto_increment comment '答案唯一标识'
        primary key,
    response_id    bigint unsigned                     not null comment '所属答卷ID',
    question_id    bigint unsigned                     not null comment '问题ID',
    answer_type    enum ('option', 'text')             not null comment '答案类型',
    answer_content json                                not null comment '答案内容（根据类型存储不同数据）',
    created_at     timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint answer_ibfk_1
        foreign key (response_id) references response (id)
            on delete cascade,
    constraint answer_ibfk_2
        foreign key (question_id) references questionnaire_question (id)
            on delete cascade
)
    comment '问卷答案表' collate = utf8mb4_unicode_ci;

create index idx_answer_question
    on answer (question_id);

create index response_id
    on answer (response_id);

create index idx_response_submitted
    on response (submitted_at);

create index questionnaire_id
    on response (questionnaire_id);

create index user_id
    on response (user_id);

create table template
(
    id          bigint unsigned auto_increment comment '模板唯一标识'
        primary key,
    category_id bigint unsigned                                       null comment '分类ID',
    user_id     bigint unsigned                                       not null comment '创建用户ID',
    title       varchar(255)                                          not null comment '模板标题',
    description varchar(500)                                          null comment '模板描述（限500字符）',
    status      enum ('private', 'publish') default 'private'         not null comment '模板状态',
    usage_count bigint unsigned             default 0                 not null comment '使用次数',
    created_at  timestamp                   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  timestamp                   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint template_ibfk_1
        foreign key (category_id) references category (id)
            on delete set null,
    constraint template_ibfk_2
        foreign key (user_id) references user (id)
            on delete cascade
)
    comment '问卷模板表' collate = utf8mb4_unicode_ci;

create index category_id
    on template (category_id);

create index idx_template_status
    on template (status);

create index idx_template_user
    on template (user_id);

create definer = root@localhost trigger update_category_template_count_on_delete
    after delete
    on template
    for each row
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
    END IF;
END;

create definer = root@localhost trigger update_category_template_count_on_insert
    after insert
    on template
    for each row
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
    END IF;
END;

create definer = root@localhost trigger update_category_template_count_on_update
    after update
    on template
    for each row
BEGIN
    IF OLD.category_id <> NEW.category_id
        OR (OLD.category_id IS NULL AND NEW.category_id IS NOT NULL)
        OR (OLD.category_id IS NOT NULL AND NEW.category_id IS NULL) THEN
        IF OLD.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
        END IF;
        IF NEW.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
        END IF;
    END IF;
END;

create table template_question
(
    id            bigint unsigned auto_increment comment '问题唯一标识'
        primary key,
    template_id   bigint unsigned                               not null comment '所属模板ID',
    question_text varchar(1000)                                 not null comment '问题内容（限1000字符）',
    question_type enum ('single', 'multiple', 'text', 'rating') not null comment '问题类型',
    sort_order    smallint unsigned default 0                   not null comment '排序序号（0-32767）',
    constraint template_question_ibfk_1
        foreign key (template_id) references template (id)
            on delete cascade
)
    comment '模板问题表' collate = utf8mb4_unicode_ci;

create table template_option
(
    id          bigint unsigned auto_increment comment '选项唯一标识'
        primary key,
    question_id bigint unsigned             not null comment '所属问题ID',
    option_text varchar(500)                not null comment '选项内容（限500字符）',
    sort_order  smallint unsigned default 0 not null comment '排序序号（0-32767）',
    constraint template_option_ibfk_1
        foreign key (question_id) references template_question (id)
            on delete cascade
)
    comment '模板选项表' collate = utf8mb4_unicode_ci;

create index idx_templateoption_order
    on template_option (sort_order);

create index question_id
    on template_option (question_id);

create index idx_templatequestion_order
    on template_question (sort_order);

create index template_id
    on template_question (template_id);

create index idx_user_email
    on user (email);

create index idx_user_username
    on user (username);


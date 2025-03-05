CREATE DATABASE zhihuiwen_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhihuiwen_data;

-- 用户表增强安全约束
CREATE TABLE User (
                      id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一标识',
                      avatar VARCHAR(255) NOT NULL DEFAULT '/default-avatar.png' COMMENT '用户头像URL',
                      email VARCHAR(254) NOT NULL UNIQUE COMMENT '用户邮箱（RFC 5321标准）',
                      username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名（唯一）',
                      password CHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码（固定255字符）',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      INDEX idx_user_email (email),
                      INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 统一分类表设计
CREATE TABLE Category (
                          id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '分类唯一标识',
                          user_id INT UNSIGNED NOT NULL COMMENT '所属用户ID',
                          name VARCHAR(100) NOT NULL COMMENT '分类名称',
                          catalog ENUM('survey','template') NOT NULL COMMENT '分类目录类型',
                          description VARCHAR(500) COMMENT '分类描述（限500字符）',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                          UNIQUE INDEX idx_category_unique (user_id, name, catalog),
                          INDEX idx_category_catalog (catalog)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一分类表（问卷/模板）';

-- 模板问卷表增强约束
CREATE TABLE Template (
                          id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '模板唯一标识',
                          category_id INT UNSIGNED COMMENT '分类ID',
                          user_id INT UNSIGNED NOT NULL COMMENT '创建用户ID',
                          title VARCHAR(255) NOT NULL COMMENT '模板标题',
                          description VARCHAR(500) COMMENT '模板描述（限500字符）',
                          status ENUM('draft','published','archived') NOT NULL DEFAULT 'draft' COMMENT '模板状态',
                          usage_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用次数',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE SET NULL,
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                          INDEX idx_template_status (status),
                          INDEX idx_template_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷模板表';

-- 模板问题表
CREATE TABLE TemplateQuestion (
                                  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问题唯一标识',
                                  template_id INT UNSIGNED NOT NULL COMMENT '所属模板ID',
                                  question_text VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
                                  question_type ENUM('single','multiple','text','rating') NOT NULL COMMENT '问题类型',
                                  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                  is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否必答',
                                  FOREIGN KEY (template_id) REFERENCES Template(id) ON DELETE CASCADE,
                                  INDEX idx_templatequestion_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板问题表';

-- 模板选项表（新增）
CREATE TABLE TemplateOption (
                                id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '选项唯一标识',
                                question_id INT UNSIGNED NOT NULL COMMENT '所属问题ID',
                                option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
                                sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                additional_data JSON COMMENT '扩展数据（如分数值等）',
                                FOREIGN KEY (question_id) REFERENCES TemplateQuestion(id) ON DELETE CASCADE,
                                INDEX idx_templateoption_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板选项表';

-- 问卷表增强约束
CREATE TABLE Survey (
                        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问卷唯一标识',
                        template_id INT UNSIGNED COMMENT '来源模板ID',
                        category_id INT UNSIGNED COMMENT '分类ID',
                        user_id INT UNSIGNED NOT NULL COMMENT '创建用户ID',
                        title VARCHAR(255) NOT NULL COMMENT '问卷标题',
                        description VARCHAR(500) COMMENT '问卷描述（限500字符）',
                        status ENUM('draft','published','closed') NOT NULL DEFAULT 'draft' COMMENT '问卷状态',
                        start_time DATETIME COMMENT '开始时间',
                        end_time DATETIME COMMENT '结束时间',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        FOREIGN KEY (template_id) REFERENCES Template(id) ON DELETE SET NULL,
                        FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE SET NULL,
                        FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                        INDEX idx_survey_status (status),
                        INDEX idx_survey_time (start_time, end_time),
                        CONSTRAINT chk_survey_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';

-- 问卷问题表
CREATE TABLE SurveyQuestion (
                                id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问题唯一标识',
                                survey_id INT UNSIGNED NOT NULL COMMENT '所属问卷ID',
                                question_text VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
                                question_type ENUM('single','multiple','text','rating') NOT NULL COMMENT '问题类型',
                                sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否必答',
                                FOREIGN KEY (survey_id) REFERENCES Survey(id) ON DELETE CASCADE,
                                INDEX idx_surveyquestion_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷问题表';

-- 问卷选项表
CREATE TABLE SurveyOption (
                              id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '选项唯一标识',
                              question_id INT UNSIGNED NOT NULL COMMENT '所属问题ID',
                              option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
                              sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                              additional_data JSON COMMENT '扩展数据（如分数值等）',
                              FOREIGN KEY (question_id) REFERENCES SurveyQuestion(id) ON DELETE CASCADE,
                              INDEX idx_surveyoption_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷选项表';

-- 答卷记录表增强约束
CREATE TABLE Response (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '答卷唯一标识',
                          survey_id INT UNSIGNED NOT NULL COMMENT '问卷ID',
                          user_id INT UNSIGNED COMMENT '提交用户ID',
                          duration SMALLINT UNSIGNED COMMENT '填写耗时（秒）',
                          ip_address VARCHAR(45) COMMENT '提交IP地址',
                          user_agent VARCHAR(500) COMMENT '浏览器标识',
                          submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                          FOREIGN KEY (survey_id) REFERENCES Survey(id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE SET NULL,
                          INDEX idx_response_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷答卷表';

-- 答案记录表优化
CREATE TABLE Answer (
                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '答案唯一标识',
                        response_id BIGINT UNSIGNED NOT NULL COMMENT '所属答卷ID',
                        question_id INT UNSIGNED NOT NULL COMMENT '问题ID',
                        answer_type ENUM('option', 'text') NOT NULL COMMENT '答案类型',
                        answer_content JSON NOT NULL COMMENT '答案内容（根据类型存储不同数据）',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        FOREIGN KEY (response_id) REFERENCES Response(id) ON DELETE CASCADE,
                        FOREIGN KEY (question_id) REFERENCES SurveyQuestion(id) ON DELETE CASCADE,
                        INDEX idx_answer_question (question_id),
                        CONSTRAINT chk_answer_validity CHECK (
                            (answer_type = 'option' AND JSON_TYPE(answer_content) = 'ARRAY') OR
                            (answer_type = 'text' AND JSON_TYPE(answer_content) = 'STRING')
                            )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷答案表';

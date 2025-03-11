CREATE DATABASE zhihuiwen_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zhihuiwen_data;

-- 用户表增强安全约束
CREATE TABLE User (
                      id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一标识',
                      avatar VARCHAR(255) NOT NULL DEFAULT '/default-avatar.png' COMMENT '用户头像URL',
                      email VARCHAR(254) NOT NULL UNIQUE COMMENT '用户邮箱（RFC 5321标准）',
                      username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名（唯一）',
                      password CHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码（固定255字符）',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      INDEX idx_user_email (email),
                      INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 统一分类表设计（新增问卷数量统计）
CREATE TABLE Category (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '分类唯一标识',
                          user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
                          name VARCHAR(100) NOT NULL COMMENT '分类名称',
                          catalog ENUM('questionnaire','template') NOT NULL COMMENT '分类目录类型',
                          description VARCHAR(500) COMMENT '分类描述（限500字符）',
                          questionnaire_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分类下问卷数量',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                          UNIQUE INDEX idx_category_unique (user_id, name, catalog),
                          INDEX idx_category_catalog (catalog)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一分类表（问卷/模板）';

-- 模板问卷表增强约束
CREATE TABLE Template (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '模板唯一标识',
                          category_id BIGINT UNSIGNED COMMENT '分类ID',
                          user_id BIGINT UNSIGNED NOT NULL COMMENT '创建用户ID',
                          title VARCHAR(255) NOT NULL COMMENT '模板标题',
                          description VARCHAR(500) COMMENT '模板描述（限500字符）',
                          status ENUM('draft','published','archived') NOT NULL DEFAULT 'draft' COMMENT '模板状态',
                          usage_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用次数',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE SET NULL,
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                          INDEX idx_template_status (status),
                          INDEX idx_template_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷模板表';

-- 模板问题表
CREATE TABLE TemplateQuestion (
                                  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问题唯一标识',
                                  template_id BIGINT UNSIGNED NOT NULL COMMENT '所属模板ID',
                                  question_text VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
                                  question_type ENUM('single','multiple','text','rating') NOT NULL COMMENT '问题类型',
                                  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                  is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否必答',
                                  FOREIGN KEY (template_id) REFERENCES Template(id) ON DELETE CASCADE,
                                  INDEX idx_templatequestion_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板问题表';

-- 模板选项表
CREATE TABLE TemplateOption (
                                id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '选项唯一标识',
                                question_id BIGINT UNSIGNED NOT NULL COMMENT '所属问题ID',
                                option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
                                sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                additional_data JSON COMMENT '扩展数据（如分数值等）',
                                FOREIGN KEY (question_id) REFERENCES TemplateQuestion(id) ON DELETE CASCADE,
                                INDEX idx_templateoption_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板选项表';

-- 问卷表增强约束（删除template_id字段）
CREATE TABLE Questionnaire (
                               id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问卷唯一标识',
                               category_id BIGINT UNSIGNED COMMENT '分类ID',
                               user_id BIGINT UNSIGNED NOT NULL COMMENT '创建用户ID',
                               title VARCHAR(255) NOT NULL COMMENT '问卷标题',
                               description VARCHAR(500) COMMENT '问卷描述（限500字符）',
                               status ENUM('draft','published','closed') NOT NULL DEFAULT 'draft' COMMENT '问卷状态',
                               start_time DATETIME COMMENT '开始时间',
                               end_time DATETIME COMMENT '结束时间',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE SET NULL,
                               FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
                               INDEX idx_questionnaire_status (status),
                               INDEX idx_questionnaire_time (start_time, end_time),
                               CONSTRAINT chk_questionnaire_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';

-- 问卷问题表
CREATE TABLE QuestionnaireQuestion (
                                       id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '问题唯一标识',
                                       questionnaire_id BIGINT UNSIGNED NOT NULL COMMENT '所属问卷ID',
                                       question_text VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
                                       question_type ENUM('single','multiple','text','rating') NOT NULL COMMENT '问题类型',
                                       sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                       is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否必答',
                                       FOREIGN KEY (questionnaire_id) REFERENCES Questionnaire(id) ON DELETE CASCADE,
                                       INDEX idx_questionnairequestion_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷问题表';

-- 问卷选项表
CREATE TABLE QuestionnaireOption (
                                     id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '选项唯一标识',
                                     question_id BIGINT UNSIGNED NOT NULL COMMENT '所属问题ID',
                                     option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
                                     sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号（0-32767）',
                                     additional_data JSON COMMENT '扩展数据（如分数值等）',
                                     FOREIGN KEY (question_id) REFERENCES QuestionnaireQuestion(id) ON DELETE CASCADE,
                                     INDEX idx_questionnaireoption_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷选项表';

-- 答卷记录表增强约束
CREATE TABLE Response (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '答卷唯一标识',
                          questionnaire_id BIGINT UNSIGNED NOT NULL COMMENT '问卷ID',
                          user_id BIGINT UNSIGNED COMMENT '提交用户ID',
                          duration SMALLINT UNSIGNED COMMENT '填写耗时（秒）',
                          ip_address VARCHAR(45) COMMENT '提交IP地址',
                          user_agent VARCHAR(500) COMMENT '浏览器标识',
                          submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                          FOREIGN KEY (questionnaire_id) REFERENCES Questionnaire(id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE SET NULL,
                          INDEX idx_response_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷答卷表';

-- 答案记录表优化
CREATE TABLE Answer (
                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '答案唯一标识',
                        response_id BIGINT UNSIGNED NOT NULL COMMENT '所属答卷ID',
                        question_id BIGINT UNSIGNED NOT NULL COMMENT '问题ID',
                        answer_type ENUM('option', 'text') NOT NULL COMMENT '答案类型',
                        answer_content JSON NOT NULL COMMENT '答案内容（根据类型存储不同数据）',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        FOREIGN KEY (response_id) REFERENCES Response(id) ON DELETE CASCADE,
                        FOREIGN KEY (question_id) REFERENCES QuestionnaireQuestion(id) ON DELETE CASCADE,
                        INDEX idx_answer_question (question_id),
                        CONSTRAINT chk_answer_validity CHECK (
                            (answer_type = 'option' AND JSON_TYPE(answer_content) = 'ARRAY') OR
                            (answer_type = 'text' AND JSON_TYPE(answer_content) = 'STRING')
                            )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷答案表';

-- 创建触发器自动更新Category表中的questionnaire_count
DELIMITER //
CREATE TRIGGER update_category_count_on_insert AFTER INSERT ON Questionnaire
    FOR EACH ROW
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE Category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
    END IF;
END//

CREATE TRIGGER update_category_count_on_delete AFTER DELETE ON Questionnaire
    FOR EACH ROW
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE Category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
    END IF;
END//

CREATE TRIGGER update_category_count_on_update AFTER UPDATE ON Questionnaire
    FOR EACH ROW
BEGIN
    IF OLD.category_id <> NEW.category_id OR (OLD.category_id IS NULL AND NEW.category_id IS NOT NULL) OR (OLD.category_id IS NOT NULL AND NEW.category_id IS NULL) THEN
        IF OLD.category_id IS NOT NULL THEN
            UPDATE Category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
        END IF;
        IF NEW.category_id IS NOT NULL THEN
            UPDATE Category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
        END IF;
    END IF;
END//
DELIMITER ;

-- 插入测试数据

-- 首先插入一个测试用户（必须）
INSERT INTO User (id, email, username, password)
VALUES (1, 'test@example.com', 'testuser', '$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLM');

-- 插入分类数据
INSERT INTO Category (id, user_id, name, catalog, description) VALUES
                                                                   (1, 1, '教育调研', 'questionnaire', '用于各类教育领域的调查问卷'),
                                                                   (2, 1, '产品反馈', 'questionnaire', '收集用户对产品的反馈意见'),
                                                                   (3, 1, '满意度调查', 'questionnaire', '了解客户满意度的问卷模板'),
                                                                   (4, 1, '市场调研', 'template', '常用的市场调研模板'),
                                                                   (5, 1, '课程评价', 'template', '课程满意度评价模板');

-- 插入模板数据
INSERT INTO Template (id, category_id, user_id, title, description, status, usage_count) VALUES
                                                                                             (1, 4, 1, '产品市场调研模板', '适用于各类产品的市场调研', 'published', 15),
                                                                                             (2, 5, 1, '大学课程评价模板', '用于评估大学课程质量和教学效果', 'published', 22),
                                                                                             (3, 4, 1, '消费者行为调研', '了解消费者购买习惯和偏好', 'draft', 0);

-- 插入模板问题
INSERT INTO TemplateQuestion (id, template_id, question_text, question_type, sort_order, is_required) VALUES
                                                                                                          (1, 1, '您的年龄段是？', 'single', 0, TRUE),
                                                                                                          (2, 1, '您每月在该类产品上的消费金额是多少？', 'single', 1, TRUE),
                                                                                                          (3, 1, '您最看重产品的哪些特性？（可多选）', 'multiple', 2, TRUE),
                                                                                                          (4, 1, '您对本产品的总体评价是？', 'rating', 3, TRUE),
                                                                                                          (5, 2, '您认为本课程的难度如何？', 'single', 0, TRUE),
                                                                                                          (6, 2, '您认为教师的教学质量如何？', 'rating', 1, TRUE),
                                                                                                          (7, 2, '您对课程有什么建议？', 'text', 2, FALSE);

-- 插入模板选项
INSERT INTO TemplateOption (id, question_id, option_text, sort_order) VALUES
                                                                          (1, 1, '18岁以下', 0),
                                                                          (2, 1, '18-25岁', 1),
                                                                          (3, 1, '26-35岁', 2),
                                                                          (4, 1, '36-45岁', 3),
                                                                          (5, 1, '46岁以上', 4),
                                                                          (6, 2, '100元以下', 0),
                                                                          (7, 2, '100-500元', 1),
                                                                          (8, 2, '500-1000元', 2),
                                                                          (9, 2, '1000元以上', 3),
                                                                          (10, 3, '价格', 0),
                                                                          (11, 3, '质量', 1),
                                                                          (12, 3, '品牌', 2),
                                                                          (13, 3, '售后服务', 3),
                                                                          (14, 5, '太简单', 0),
                                                                          (15, 5, '适中', 1),
                                                                          (16, 5, '有挑战', 2),
                                                                          (17, 5, '太难', 3);

-- 插入问卷数据
INSERT INTO Questionnaire (id, category_id, user_id, title, description, status, start_time, end_time) VALUES
                                                                                                           (1, 1, 1, '2025年春季学生满意度调查', '了解学生对本学期课程和教学的满意度', 'published', '2025-02-01 00:00:00', '2025-06-30 23:59:59'),
                                                                                                           (2, 2, 1, '智慧问产品体验反馈', '收集用户对智慧问产品的使用体验', 'published', '2025-01-15 00:00:00', '2025-04-15 23:59:59'),
                                                                                                           (3, 3, 1, '客户服务质量调查', '评估我们的客户服务质量和响应速度', 'draft', NULL, NULL),
                                                                                                           (4, 1, 1, '在线教育平台用户体验调查', '收集用户对在线教育平台的使用体验和建议', 'closed', '2024-11-01 00:00:00', '2025-01-31 23:59:59');

-- 插入问卷问题
INSERT INTO QuestionnaireQuestion (id, questionnaire_id, question_text, question_type, sort_order, is_required) VALUES
                                                                                                                    (1, 1, '您对本学期的课程安排满意度如何？', 'rating', 0, TRUE),
                                                                                                                    (2, 1, '您认为哪些方面需要改进？（可多选）', 'multiple', 1, TRUE),
                                                                                                                    (3, 1, '您希望增加哪些类型的课程？', 'text', 2, FALSE),
                                                                                                                    (4, 2, '您使用智慧问产品的频率是？', 'single', 0, TRUE),
                                                                                                                    (5, 2, '您对产品界面设计的满意度是？', 'rating', 1, TRUE),
                                                                                                                    (6, 2, '您对产品有哪些改进建议？', 'text', 2, FALSE),
                                                                                                                    (7, 3, '您最近一次联系客服是什么时候？', 'single', 0, TRUE),
                                                                                                                    (8, 3, '客服解决问题的速度如何？', 'rating', 1, TRUE),
                                                                                                                    (9, 4, '您使用在线教育平台的主要目的是？', 'multiple', 0, TRUE),
                                                                                                                    (10, 4, '您认为平台的教学质量如何？', 'rating', 1, TRUE);

-- 插入问卷选项
INSERT INTO questionnaire_option (id, question_id, option_text, sort_order, additional_data) VALUES
                                                                                                (1, 2, '课程内容', 0, NULL),
                                                                                                (2, 2, '教学方法', 1, NULL),
                                                                                                (3, 2, '教学设施', 2, NULL),
                                                                                                (4, 2, '课程安排时间', 3, NULL),
                                                                                                (5, 4, '每天', 0, NULL),
                                                                                                (6, 4, '每周几次', 1, NULL),
                                                                                                (7, 4, '每月几次', 2, NULL),
                                                                                                (8, 4, '很少使用', 3, NULL),
                                                                                                (9, 7, '一周内', 0, NULL),
                                                                                                (10, 7, '一个月内', 1, NULL),
                                                                                                (11, 7, '三个月内', 2, NULL),
                                                                                                (12, 7, '更长时间', 3, NULL),
                                                                                                (13, 9, '提高专业技能', 0, NULL),
                                                                                                (14, 9, '学习新知识', 1, NULL),
                                                                                                (15, 9, '准备考试', 2, NULL),
                                                                                                (16, 9, '兴趣爱好', 3, NULL);

-- 插入答卷数据
INSERT INTO Response (id, questionnaire_id, user_id, duration, ip_address, user_agent, submitted_at) VALUES
                                                                                                         (1, 1, NULL, 186, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '2025-02-10 14:23:45'),
                                                                                                         (2, 1, NULL, 215, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15', '2025-02-11 09:12:33'),
                                                                                                         (3, 2, 1, 152, '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15', '2025-01-20 16:45:22'),
                                                                                                         (4, 4, NULL, 278, '192.168.1.103', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '2025-01-15 11:30:45'),
                                                                                                         (5, 4, NULL, 195, '192.168.1.104', 'Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36', '2025-01-18 20:15:39');

-- 插入答案数据
INSERT INTO Answer (id, response_id, question_id, answer_type, answer_content, created_at) VALUES
                                                                                               (1, 1, 1, 'option', '[4]', '2025-02-10 14:23:45'), -- 评分4分（满意）
                                                                                               (2, 1, 2, 'option', '[1, 3]', '2025-02-10 14:23:45'), -- 选择了"教学方法"和"教学设施"
                                                                                               (3, 1, 3, 'text', '"希望增加更多实践类课程"', '2025-02-10 14:23:45'),
                                                                                               (4, 2, 1, 'option', '[3]', '2025-02-11 09:12:33'), -- 评分3分（一般）
                                                                                               (5, 2, 2, 'option', '[2, 4]', '2025-02-11 09:12:33'), -- 选择了"教学方法"和"课程安排时间"
                                                                                               (6, 2, 3, 'text', '"增加编程和数据分析相关课程"', '2025-02-11 09:12:33'),
                                                                                               (7, 3, 4, 'option', '[1]', '2025-01-20 16:45:22'), -- 选择了"每天"
                                                                                               (8, 3, 5, 'option', '[4]', '2025-01-20 16:45:22'), -- 评分4分（满意）
                                                                                               (9, 3, 6, 'text', '"希望增加夜间模式和更多自定义选项"', '2025-01-20 16:45:22'),
                                                                                               (10, 4, 9, 'option', '[1, 2]', '2025-01-15 11:30:45'), -- 选择了"提高专业技能"和"学习新知识"
                                                                                               (11, 4, 10, 'option', '[5]', '2025-01-15 11:30:45'), -- 评分5分（非常满意）
                                                                                               (12, 5, 9, 'option', '[3]', '2025-01-18 20:15:39'), -- 选择了"准备考试"
                                                                                               (13, 5, 10, 'option', '[4]', '2025-01-18 20:15:39'); -- 评分4分（满意）
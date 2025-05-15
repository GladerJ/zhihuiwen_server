CREATE DATABASE zhihuiwen_data;
USE zhihuiwen_data;

CREATE TABLE report
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NULL,
    content          TEXT NULL,
    created_at       TIMESTAMP NULL
) COLLATE = utf8mb4_unicode_ci;

CREATE TABLE total_report
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT NULL,
    user_id    BIGINT NULL,
    created_at TIMESTAMP NULL
);

CREATE TABLE user
(
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户唯一标识' PRIMARY KEY,
    avatar     VARCHAR(255) DEFAULT '/default-avatar.png' NOT NULL COMMENT '用户头像URL',
    email      VARCHAR(254) NOT NULL COMMENT '用户邮箱（RFC 5321标准）',
    username   VARCHAR(30) NOT NULL COMMENT '用户名（唯一）',
    password   CHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码（固定255字符）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY (email),
    UNIQUE KEY (username)
) COMMENT '系统用户表' COLLATE = utf8mb4_unicode_ci;

CREATE TABLE category
(
    id                  BIGINT UNSIGNED AUTO_INCREMENT COMMENT '分类唯一标识' PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
    name                VARCHAR(100) NOT NULL COMMENT '分类名称',
    catalog             ENUM('questionnaire', 'template') NOT NULL COMMENT '分类目录类型',
    description         VARCHAR(500) NULL COMMENT '分类描述（限500字符）',
    questionnaire_count BIGINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '分类下问卷数量',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY idx_category_unique (user_id, name, catalog),
    CONSTRAINT category_ibfk_1 FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '统一分类表（问卷/模板）' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_category_catalog ON category (catalog);

CREATE TABLE questionnaire
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '问卷唯一标识' PRIMARY KEY,
    category_id BIGINT UNSIGNED NULL COMMENT '分类ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '创建用户ID',
    title       VARCHAR(255) NOT NULL COMMENT '问卷标题',
    description VARCHAR(500) NULL COMMENT '问卷描述（限500字符）',
    status      ENUM('draft', 'published', 'closed') DEFAULT 'draft' NOT NULL COMMENT '问卷状态',
    start_time  DATETIME NULL COMMENT '开始时间',
    end_time    DATETIME NULL COMMENT '结束时间',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT questionnaire_ibfk_1 FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT questionnaire_ibfk_2 FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '问卷表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX category_id ON questionnaire (category_id);
CREATE INDEX idx_questionnaire_status ON questionnaire (status);
CREATE INDEX idx_questionnaire_time ON questionnaire (start_time, end_time);
CREATE INDEX user_id ON questionnaire (user_id);

DELIMITER $$

CREATE TRIGGER update_category_count_on_delete
    AFTER DELETE ON questionnaire
    FOR EACH ROW
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
    END IF;
END$$

CREATE TRIGGER update_category_count_on_insert
    AFTER INSERT ON questionnaire
    FOR EACH ROW
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
    END IF;
END$$

CREATE TRIGGER update_category_count_on_update
    AFTER UPDATE ON questionnaire
    FOR EACH ROW
BEGIN
    IF (OLD.category_id <> NEW.category_id)
        OR (OLD.category_id IS NULL AND NEW.category_id IS NOT NULL)
        OR (OLD.category_id IS NOT NULL AND NEW.category_id IS NULL) THEN
        IF OLD.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
        END IF;
        IF NEW.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
        END IF;
    END IF;
END$$

DELIMITER ;

CREATE TABLE questionnaire_question
(
    id               BIGINT UNSIGNED AUTO_INCREMENT COMMENT '问题唯一标识' PRIMARY KEY,
    questionnaire_id BIGINT UNSIGNED NOT NULL COMMENT '所属问卷ID',
    question_text    VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
    question_type    ENUM('single', 'multiple', 'text', 'rating') NOT NULL COMMENT '问题类型',
    sort_order       SMALLINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '排序序号（0-32767）',
    CONSTRAINT questionnaire_question_ibfk_1 FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE
) COMMENT '问卷问题表' COLLATE = utf8mb4_unicode_ci;

CREATE TABLE questionnaire_option
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '选项唯一标识' PRIMARY KEY,
    question_id BIGINT UNSIGNED NOT NULL COMMENT '所属问题ID',
    option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
    sort_order  SMALLINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '排序序号（0-32767）',
    CONSTRAINT questionnaire_option_ibfk_1 FOREIGN KEY (question_id) REFERENCES questionnaire_question(id) ON DELETE CASCADE
) COMMENT '问卷选项表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_questionnaireoption_order ON questionnaire_option (sort_order);
CREATE INDEX question_id ON questionnaire_option (question_id);
CREATE INDEX idx_questionnairequestion_order ON questionnaire_question (sort_order);
CREATE INDEX questionnaire_id ON questionnaire_question (questionnaire_id);

CREATE TABLE response
(
    id               BIGINT UNSIGNED AUTO_INCREMENT COMMENT '答卷唯一标识' PRIMARY KEY,
    questionnaire_id BIGINT UNSIGNED NOT NULL COMMENT '问卷ID',
    user_id          BIGINT UNSIGNED NULL COMMENT '提交用户ID',
    duration         SMALLINT UNSIGNED NULL COMMENT '填写耗时（秒）',
    ip_address       VARCHAR(45) NULL COMMENT '提交IP地址',
    user_agent       VARCHAR(500) NULL COMMENT '浏览器标识',
    submitted_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '提交时间',
    CONSTRAINT response_ibfk_1 FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE,
    CONSTRAINT response_ibfk_2 FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '问卷答卷表' COLLATE = utf8mb4_unicode_ci;

CREATE TABLE answer
(
    id             BIGINT UNSIGNED AUTO_INCREMENT COMMENT '答案唯一标识' PRIMARY KEY,
    response_id    BIGINT UNSIGNED NOT NULL COMMENT '所属答卷ID',
    question_id    BIGINT UNSIGNED NOT NULL COMMENT '问题ID',
    answer_type    ENUM('option', 'text') NOT NULL COMMENT '答案类型',
    answer_content JSON NOT NULL COMMENT '答案内容（根据类型存储不同数据）',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT answer_ibfk_1 FOREIGN KEY (response_id) REFERENCES response(id) ON DELETE CASCADE,
    CONSTRAINT answer_ibfk_2 FOREIGN KEY (question_id) REFERENCES questionnaire_question(id) ON DELETE CASCADE
) COMMENT '问卷答案表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_answer_question ON answer (question_id);
CREATE INDEX response_id ON answer (response_id);
CREATE INDEX idx_response_submitted ON response (submitted_at);
CREATE INDEX questionnaire_id ON response (questionnaire_id);
CREATE INDEX user_id ON response (user_id);

CREATE TABLE template
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '模板唯一标识' PRIMARY KEY,
    category_id BIGINT UNSIGNED NULL COMMENT '分类ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '创建用户ID',
    title       VARCHAR(255) NOT NULL COMMENT '模板标题',
    description VARCHAR(500) NULL COMMENT '模板描述（限500字符）',
    status      ENUM('private', 'publish') DEFAULT 'private' NOT NULL COMMENT '模板状态',
    usage_count BIGINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '使用次数',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT template_ibfk_1 FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT template_ibfk_2 FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '问卷模板表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX category_id ON template (category_id);
CREATE INDEX idx_template_status ON template (status);
CREATE INDEX idx_template_user ON template (user_id);

DELIMITER $$

CREATE TRIGGER update_category_template_count_on_delete
    AFTER DELETE ON template
    FOR EACH ROW
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
    END IF;
END$$

CREATE TRIGGER update_category_template_count_on_insert
    AFTER INSERT ON template
    FOR EACH ROW
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
    END IF;
END$$

CREATE TRIGGER update_category_template_count_on_update
    AFTER UPDATE ON template
    FOR EACH ROW
BEGIN
    IF (OLD.category_id <> NEW.category_id)
        OR (OLD.category_id IS NULL AND NEW.category_id IS NOT NULL)
        OR (OLD.category_id IS NOT NULL AND NEW.category_id IS NULL) THEN
        IF OLD.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count - 1 WHERE id = OLD.category_id;
        END IF;
        IF NEW.category_id IS NOT NULL THEN
            UPDATE category SET questionnaire_count = questionnaire_count + 1 WHERE id = NEW.category_id;
        END IF;
    END IF;
END$$

DELIMITER ;

CREATE TABLE template_question
(
    id            BIGINT UNSIGNED AUTO_INCREMENT COMMENT '问题唯一标识' PRIMARY KEY,
    template_id   BIGINT UNSIGNED NOT NULL COMMENT '所属模板ID',
    question_text VARCHAR(1000) NOT NULL COMMENT '问题内容（限1000字符）',
    question_type ENUM('single', 'multiple', 'text', 'rating') NOT NULL COMMENT '问题类型',
    sort_order    SMALLINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '排序序号（0-32767）',
    CONSTRAINT template_question_ibfk_1 FOREIGN KEY (template_id) REFERENCES template(id) ON DELETE CASCADE
) COMMENT '模板问题表' COLLATE = utf8mb4_unicode_ci;

CREATE TABLE template_option
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '选项唯一标识' PRIMARY KEY,
    question_id BIGINT UNSIGNED NOT NULL COMMENT '所属问题ID',
    option_text VARCHAR(500) NOT NULL COMMENT '选项内容（限500字符）',
    sort_order  SMALLINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '排序序号（0-32767）',
    CONSTRAINT template_option_ibfk_1 FOREIGN KEY (question_id) REFERENCES template_question(id) ON DELETE CASCADE
) COMMENT '模板选项表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_templateoption_order ON template_option (sort_order);
CREATE INDEX question_id ON template_option (question_id);
CREATE INDEX idx_templatequestion_order ON template_question (sort_order);
CREATE INDEX template_id ON template_question (template_id);

CREATE INDEX idx_user_email ON user (email);
CREATE INDEX idx_user_username ON user (username);

ALTER TABLE total_report CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

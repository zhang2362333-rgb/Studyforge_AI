SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `comments` ADD COLUMN `parent_comment_id` BIGINT UNSIGNED NULL AFTER `post_id`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'parent_comment_id');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `comments` ADD COLUMN `floor_no` INT UNSIGNED NOT NULL DEFAULT 0 AFTER `status`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'floor_no');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `comments` ADD COLUMN `like_count` INT UNSIGNED NOT NULL DEFAULT 0 AFTER `floor_no`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'like_count');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;

CREATE TABLE IF NOT EXISTS comment_likes (
    comment_like_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comment_likes_comment_user (comment_id, user_id),
    KEY idx_comment_likes_user_created (user_id, created_time DESC),
    CONSTRAINT fk_comment_likes_comment_id FOREIGN KEY (comment_id) REFERENCES comments (comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_likes_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Post comment likes';

UPDATE comments c
JOIN (
    SELECT
        comment_id,
        ROW_NUMBER() OVER (PARTITION BY post_id ORDER BY created_time ASC, comment_id ASC) AS floor_no
    FROM comments
) ranked ON ranked.comment_id = c.comment_id
SET c.floor_no = ranked.floor_no
WHERE c.floor_no = 0;

UPDATE comments c
LEFT JOIN (
    SELECT comment_id, COUNT(*) AS like_count
    FROM comment_likes
    GROUP BY comment_id
) likes ON likes.comment_id = c.comment_id
SET c.like_count = COALESCE(likes.like_count, 0);

SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `help_answers` ADD COLUMN `parent_answer_id` BIGINT UNSIGNED NULL AFTER `help_id`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'help_answers' AND COLUMN_NAME = 'parent_answer_id');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `help_answers` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT ''VISIBLE'' AFTER `is_accepted`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'help_answers' AND COLUMN_NAME = 'status');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `help_answers` ADD COLUMN `floor_no` INT UNSIGNED NOT NULL DEFAULT 0 AFTER `status`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'help_answers' AND COLUMN_NAME = 'floor_no');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `help_answers` ADD COLUMN `like_count` INT UNSIGNED NOT NULL DEFAULT 0 AFTER `floor_no`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'help_answers' AND COLUMN_NAME = 'like_count');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;

CREATE TABLE IF NOT EXISTS help_answer_likes (
    answer_like_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    answer_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_help_answer_likes_answer_user (answer_id, user_id),
    KEY idx_help_answer_likes_user_created (user_id, created_time DESC),
    CONSTRAINT fk_help_answer_likes_answer_id FOREIGN KEY (answer_id) REFERENCES help_answers (answer_id) ON DELETE CASCADE,
    CONSTRAINT fk_help_answer_likes_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Help answer likes';

UPDATE help_answers a
JOIN (
    SELECT
        answer_id,
        ROW_NUMBER() OVER (PARTITION BY help_id ORDER BY created_time ASC, answer_id ASC) AS floor_no
    FROM help_answers
) ranked ON ranked.answer_id = a.answer_id
SET a.floor_no = ranked.floor_no
WHERE a.floor_no = 0;

UPDATE help_answers a
LEFT JOIN (
    SELECT answer_id, COUNT(*) AS like_count
    FROM help_answer_likes
    GROUP BY answer_id
) likes ON likes.answer_id = a.answer_id
SET a.like_count = COALESCE(likes.like_count, 0);

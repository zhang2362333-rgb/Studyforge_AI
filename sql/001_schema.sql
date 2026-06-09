-- Import this schema with an explicit database selected, for example:
-- mysql -u studyforge -p studyforge_ai < sql/001_schema.sql
-- Legacy upgrade blocks below use information_schema + prepared DDL instead
-- of MariaDB-only ALTER TABLE ... ADD COLUMN IF NOT EXISTS syntax.

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    display_name VARCHAR(80) NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    bio VARCHAR(300) NULL,
    avatar_url VARCHAR(512) NULL,
    banner_url VARCHAR(512) NULL,
    community_level INT UNSIGNED NOT NULL DEFAULT 1,
    experience_points INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_reward_date DATE NULL,
    reputation_score INT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role_status (role, status)
) ENGINE=InnoDB COMMENT='User account and profile';

SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `display_name` VARCHAR(80) NULL AFTER `username`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'display_name');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `bio` VARCHAR(300) NULL AFTER `status`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'bio');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `avatar_url` VARCHAR(512) NULL AFTER `bio`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar_url');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `banner_url` VARCHAR(512) NULL AFTER `avatar_url`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'banner_url');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `community_level` INT UNSIGNED NOT NULL DEFAULT 1 AFTER `banner_url`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'community_level');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `experience_points` INT UNSIGNED NOT NULL DEFAULT 0 AFTER `community_level`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'experience_points');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `last_login_reward_date` DATE NULL AFTER `experience_points`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login_reward_date');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `users` ADD COLUMN `reputation_score` INT NOT NULL DEFAULT 0 AFTER `last_login_reward_date`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'reputation_score');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;

CREATE TABLE IF NOT EXISTS user_tokens (
    token_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    access_token VARCHAR(512) NOT NULL,
    expire_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_tokens_access_token (access_token),
    KEY idx_user_tokens_user_status (user_id, status),
    CONSTRAINT fk_user_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Access token store';

CREATE TABLE IF NOT EXISTS user_follows (
    follow_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT UNSIGNED NOT NULL,
    following_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_follows_pair (follower_id, following_id),
    KEY idx_user_follows_following (following_id, status, created_time DESC),
    KEY idx_user_follows_follower (follower_id, status, created_time DESC),
    CONSTRAINT fk_user_follows_follower_id FOREIGN KEY (follower_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_follows_following_id FOREIGN KEY (following_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='User follow relationships';

CREATE TABLE IF NOT EXISTS friend_requests (
    request_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT UNSIGNED NOT NULL,
    addressee_id BIGINT UNSIGNED NOT NULL,
    message VARCHAR(300) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_time DATETIME NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friend_requests_pair (requester_id, addressee_id),
    KEY idx_friend_requests_addressee_status (addressee_id, status, created_time DESC),
    KEY idx_friend_requests_requester_status (requester_id, status, created_time DESC),
    CONSTRAINT fk_friend_requests_requester_id FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_requests_addressee_id FOREIGN KEY (addressee_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Friend request inbox';

CREATE TABLE IF NOT EXISTS friendships (
    friendship_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_low_id BIGINT UNSIGNED NOT NULL,
    user_high_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friendships_pair (user_low_id, user_high_id),
    KEY idx_friendships_low_status (user_low_id, status, created_time DESC),
    KEY idx_friendships_high_status (user_high_id, status, created_time DESC),
    CONSTRAINT fk_friendships_user_low_id FOREIGN KEY (user_low_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_user_high_id FOREIGN KEY (user_high_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Accepted friend relationships';

CREATE TABLE IF NOT EXISTS friend_messages (
    message_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT UNSIGNED NOT NULL,
    receiver_id BIGINT UNSIGNED NOT NULL,
    content TEXT NOT NULL,
    read_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_friend_messages_pair_time (sender_id, receiver_id, created_time DESC),
    KEY idx_friend_messages_receiver_read (receiver_id, read_flag, created_time DESC),
    CONSTRAINT fk_friend_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_messages_receiver_id FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Messages between accepted friends';

CREATE TABLE IF NOT EXISTS user_experience_logs (
    log_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    experience_delta INT NOT NULL,
    source_id BIGINT UNSIGNED NULL,
    created_date DATE NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_experience_daily_action (user_id, action_type, created_date),
    KEY idx_user_experience_user_time (user_id, created_time DESC),
    CONSTRAINT fk_user_experience_logs_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Community experience changes';

CREATE TABLE IF NOT EXISTS integration_settings (
    setting_key VARCHAR(80) PRIMARY KEY,
    setting_value TEXT NULL,
    secret_flag TINYINT(1) NOT NULL DEFAULT 0,
    updated_by BIGINT UNSIGNED NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_integration_settings_secret (secret_flag),
    CONSTRAINT fk_integration_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='External AI and voice integration settings';

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(50) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_categories_code (category_code),
    KEY idx_categories_status_sort (status, sort_no)
) ENGINE=InnoDB COMMENT='Category dictionary';

CREATE TABLE IF NOT EXISTS category_i18n (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT UNSIGNED NOT NULL,
    language_code VARCHAR(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_i18n_category_lang (category_id, language_code),
    KEY idx_category_i18n_lang_name (language_code, name),
    CONSTRAINT fk_category_i18n_category_id FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Localized category names';

CREATE TABLE IF NOT EXISTS posts (
    post_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    original_language VARCHAR(16) NOT NULL DEFAULT 'zh_CN',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    cover_image_url VARCHAR(512) NULL,
    featured TINYINT(1) NOT NULL DEFAULT 0,
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    favorite_count INT UNSIGNED NOT NULL DEFAULT 0,
    comment_count INT UNSIGNED NOT NULL DEFAULT 0,
    view_count INT UNSIGNED NOT NULL DEFAULT 0,
    hot_score DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_posts_author_id (author_id),
    KEY idx_posts_category_id (category_id),
    KEY idx_posts_status_featured (status, featured),
    KEY idx_posts_hot_score (hot_score DESC),
    CONSTRAINT fk_posts_author_id FOREIGN KEY (author_id) REFERENCES users (user_id),
    CONSTRAINT fk_posts_category_id FOREIGN KEY (category_id) REFERENCES categories (category_id)
) ENGINE=InnoDB COMMENT='Post aggregate root';

SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `posts` ADD COLUMN `cover_image_url` VARCHAR(512) NULL AFTER `status`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'posts' AND COLUMN_NAME = 'cover_image_url');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;
SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `posts` ADD COLUMN `featured` TINYINT(1) NOT NULL DEFAULT 0 AFTER `cover_image_url`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'posts' AND COLUMN_NAME = 'featured');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;

CREATE TABLE IF NOT EXISTS post_i18n (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    language_code VARCHAR(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary TEXT NULL,
    content MEDIUMTEXT NOT NULL,
    content_format VARCHAR(20) NOT NULL DEFAULT 'MARKDOWN',
    ai_tags VARCHAR(500) NULL,
    source_type VARCHAR(20) NOT NULL DEFAULT 'ORIGINAL',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_i18n_post_lang (post_id, language_code),
    KEY idx_post_i18n_lang_post (language_code, post_id),
    FULLTEXT KEY ft_post_i18n_search (title, summary, content),
    CONSTRAINT fk_post_i18n_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Localized post content';

SET @studyforge_add_column_sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `post_i18n` ADD COLUMN `content_format` VARCHAR(20) NOT NULL DEFAULT ''MARKDOWN'' AFTER `content`', 'DO 0') FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'post_i18n' AND COLUMN_NAME = 'content_format');
PREPARE studyforge_add_column_stmt FROM @studyforge_add_column_sql; EXECUTE studyforge_add_column_stmt; DEALLOCATE PREPARE studyforge_add_column_stmt;

CREATE TABLE IF NOT EXISTS uploaded_files (
    file_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL,
    biz_type VARCHAR(30) NOT NULL DEFAULT 'POST_IMAGE',
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    content_type VARCHAR(100) NULL,
    file_size BIGINT UNSIGNED NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_uploaded_files_stored_filename (stored_filename),
    KEY idx_uploaded_files_user_created (user_id, created_time DESC),
    KEY idx_uploaded_files_biz_status (biz_type, status),
    CONSTRAINT fk_uploaded_files_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Uploaded image and attachment metadata';

CREATE TABLE IF NOT EXISTS comments (
    comment_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    parent_comment_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    language_code VARCHAR(16) NOT NULL DEFAULT 'zh_CN',
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    floor_no INT UNSIGNED NOT NULL DEFAULT 0,
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_comments_post_created (post_id, created_time DESC),
    KEY idx_comments_post_floor (post_id, floor_no),
    KEY idx_comments_parent_created (parent_comment_id, created_time ASC),
    KEY idx_comments_user_created (user_id, created_time DESC),
    CONSTRAINT fk_comments_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent_id FOREIGN KEY (parent_comment_id) REFERENCES comments (comment_id) ON DELETE SET NULL,
    CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Post comments';

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

CREATE TABLE IF NOT EXISTS post_likes (
    like_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_likes_post_user (post_id, user_id),
    KEY idx_post_likes_user_created (user_id, created_time DESC),
    CONSTRAINT fk_post_likes_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Post likes';

CREATE TABLE IF NOT EXISTS post_favorites (
    favorite_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_favorites_post_user (post_id, user_id),
    KEY idx_post_favorites_user_created (user_id, created_time DESC),
    CONSTRAINT fk_post_favorites_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_favorites_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Post favorites';

CREATE TABLE IF NOT EXISTS favorite_collections (
    collection_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(300) NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    sort_no INT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite_collections_user_name (user_id, name),
    KEY idx_favorite_collections_user_sort (user_id, sort_no, created_time DESC),
    CONSTRAINT fk_favorite_collections_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='User favorite folders';

CREATE TABLE IF NOT EXISTS favorite_collection_items (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    collection_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite_collection_items_collection_post (collection_id, post_id),
    KEY idx_favorite_collection_items_user_created (user_id, created_time DESC),
    KEY idx_favorite_collection_items_post (post_id),
    CONSTRAINT fk_favorite_collection_items_collection_id FOREIGN KEY (collection_id) REFERENCES favorite_collections (collection_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_collection_items_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_collection_items_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Posts saved into favorite folders';

CREATE TABLE IF NOT EXISTS post_view_history (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    view_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_post_view_history_user_time (user_id, view_time DESC),
    KEY idx_post_view_history_post_time (post_id, view_time DESC),
    CONSTRAINT fk_post_view_history_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_view_history_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Recent browsing history';

CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    reporter_id BIGINT UNSIGNED NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ai_risk_level VARCHAR(20) NULL,
    ai_suggestion TEXT NULL,
    processed_by BIGINT UNSIGNED NULL,
    processed_time DATETIME NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_reports_status_created (status, created_time DESC),
    KEY idx_reports_post_created (post_id, created_time DESC),
    CONSTRAINT fk_reports_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_reporter_id FOREIGN KEY (reporter_id) REFERENCES users (user_id),
    CONSTRAINT fk_reports_processed_by FOREIGN KEY (processed_by) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Report and moderation queue';

CREATE TABLE IF NOT EXISTS ai_logs (
    log_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL,
    post_id BIGINT UNSIGNED NULL,
    ai_type VARCHAR(50) NOT NULL,
    request_text MEDIUMTEXT NULL,
    response_text MEDIUMTEXT NULL,
    success TINYINT(1) NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ai_logs_type_created (ai_type, created_time DESC),
    KEY idx_ai_logs_user_created (user_id, created_time DESC),
    CONSTRAINT fk_ai_logs_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_ai_logs_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='AI call audit log';

CREATE TABLE IF NOT EXISTS voice_records (
    record_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL,
    post_id BIGINT UNSIGNED NULL,
    voice_type VARCHAR(20) NOT NULL,
    audio_url VARCHAR(512) NULL,
    recognized_text TEXT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_voice_records_type_created (voice_type, created_time DESC),
    KEY idx_voice_records_user_created (user_id, created_time DESC),
    CONSTRAINT fk_voice_records_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_voice_records_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Voice interaction records';

CREATE TABLE IF NOT EXISTS help_requests (
    help_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT UNSIGNED NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reward_points INT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_help_requests_user_created (user_id, created_time DESC),
    KEY idx_help_requests_status_created (status, created_time DESC),
    CONSTRAINT fk_help_requests_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_help_requests_category_id FOREIGN KEY (category_id) REFERENCES categories (category_id)
) ENGINE=InnoDB COMMENT='Study help requests';

CREATE TABLE IF NOT EXISTS help_answers (
    answer_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    help_id BIGINT UNSIGNED NOT NULL,
    parent_answer_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    content TEXT NOT NULL,
    is_accepted TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    floor_no INT UNSIGNED NOT NULL DEFAULT 0,
    like_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_help_answers_help_created (help_id, created_time DESC),
    KEY idx_help_answers_help_floor (help_id, floor_no),
    KEY idx_help_answers_parent_created (parent_answer_id, created_time ASC),
    KEY idx_help_answers_user_created (user_id, created_time DESC),
    CONSTRAINT fk_help_answers_help_id FOREIGN KEY (help_id) REFERENCES help_requests (help_id) ON DELETE CASCADE,
    CONSTRAINT fk_help_answers_parent_id FOREIGN KEY (parent_answer_id) REFERENCES help_answers (answer_id) ON DELETE SET NULL,
    CONSTRAINT fk_help_answers_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Answers for help requests';

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

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT UNSIGNED NOT NULL,
    actor_id BIGINT UNSIGNED NULL,
    notification_type VARCHAR(40) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT UNSIGNED NULL,
    post_id BIGINT UNSIGNED NULL,
    help_id BIGINT UNSIGNED NULL,
    comment_id BIGINT UNSIGNED NULL,
    answer_id BIGINT UNSIGNED NULL,
    friend_request_id BIGINT UNSIGNED NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(500) NULL,
    read_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME NULL,
    KEY idx_notifications_recipient_read_time (recipient_id, read_flag, created_time DESC),
    KEY idx_notifications_recipient_time (recipient_id, created_time DESC),
    KEY idx_notifications_actor_time (actor_id, created_time DESC),
    KEY idx_notifications_post (post_id),
    KEY idx_notifications_help (help_id),
    CONSTRAINT fk_notifications_recipient_id FOREIGN KEY (recipient_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor_id FOREIGN KEY (actor_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_post_id FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_help_id FOREIGN KEY (help_id) REFERENCES help_requests (help_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_comment_id FOREIGN KEY (comment_id) REFERENCES comments (comment_id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_answer_id FOREIGN KEY (answer_id) REFERENCES help_answers (answer_id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_friend_request_id FOREIGN KEY (friend_request_id) REFERENCES friend_requests (request_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='User notifications for social and learning interactions';

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    log_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT UNSIGNED NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT UNSIGNED NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    remark VARCHAR(500) NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_audit_logs_admin_created (admin_id, created_time DESC),
    KEY idx_admin_audit_logs_target (target_type, target_id),
    CONSTRAINT fk_admin_audit_logs_admin_id FOREIGN KEY (admin_id) REFERENCES users (user_id)
) ENGINE=InnoDB COMMENT='Administrative action log';

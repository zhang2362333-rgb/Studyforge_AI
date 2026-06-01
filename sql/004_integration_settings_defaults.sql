INSERT INTO integration_settings (setting_key, setting_value, secret_flag, updated_by)
VALUES
    ('ai.base_url', 'https://api.siliconflow.cn/v1', 0, NULL),
    ('ai.api_key', '', 1, NULL),
    ('ai.chat_model', 'deepseek-ai/DeepSeek-V4-Flash', 0, NULL),
    ('voice.base_url', 'https://api.siliconflow.cn/v1', 0, NULL),
    ('voice.api_key', '', 1, NULL),
    ('voice.model', 'FunAudioLLM/CosyVoice2-0.5B', 0, NULL),
    ('voice.name', 'FunAudioLLM/CosyVoice2-0.5B:alex', 0, NULL),
    ('image.base_url', 'https://api.hiyo.top/v1', 0, NULL),
    ('image.api_key', '', 1, NULL),
    ('image.model', 'gpt-image-2', 0, NULL),
    ('image.size', '1536x1024', 0, NULL)
ON DUPLICATE KEY UPDATE
    secret_flag = VALUES(secret_flag);

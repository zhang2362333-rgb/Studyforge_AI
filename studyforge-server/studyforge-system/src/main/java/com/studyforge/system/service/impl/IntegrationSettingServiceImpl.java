package com.studyforge.system.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.dto.IntegrationSettingUpdateRequest;
import com.studyforge.system.entity.IntegrationSetting;
import com.studyforge.system.mapper.IntegrationSettingMapper;
import com.studyforge.system.service.IntegrationSettingService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class IntegrationSettingServiceImpl implements IntegrationSettingService {
    private static final List<DefaultSetting> DEFAULT_SETTINGS = List.of(
            new DefaultSetting("ai.base_url", "https://api.siliconflow.cn/v1", 0),
            new DefaultSetting("ai.api_key", "", 1),
            new DefaultSetting("ai.chat_model", "deepseek-ai/DeepSeek-V4-Flash", 0),
            new DefaultSetting("voice.base_url", "https://api.siliconflow.cn/v1", 0),
            new DefaultSetting("voice.api_key", "", 1),
            new DefaultSetting("voice.model", "FunAudioLLM/CosyVoice2-0.5B", 0),
            new DefaultSetting("voice.name", "FunAudioLLM/CosyVoice2-0.5B:alex", 0),
            new DefaultSetting("image.base_url", "https://api.hiyo.top/v1", 0),
            new DefaultSetting("image.api_key", "", 1),
            new DefaultSetting("image.model", "gpt-image-2", 0),
            new DefaultSetting("image.size", "1536x1024", 0)
    );

    private final IntegrationSettingMapper integrationSettingMapper;

    public IntegrationSettingServiceImpl(IntegrationSettingMapper integrationSettingMapper) {
        this.integrationSettingMapper = integrationSettingMapper;
    }

    @Override
    public String getValue(String settingKey, String defaultValue) {
        IntegrationSetting setting = integrationSettingMapper.selectByKey(settingKey);
        if (setting == null || setting.getSettingValue() == null || setting.getSettingValue().isBlank()) {
            return defaultValue;
        }
        return setting.getSettingValue();
    }

    @Override
    public Map<String, String> getValues(String prefix) {
        Map<String, String> values = new LinkedHashMap<>();
        for (IntegrationSetting setting : list(false)) {
            if (prefix == null || setting.getSettingKey().startsWith(prefix)) {
                values.put(setting.getSettingKey(), setting.getSettingValue());
            }
        }
        return values;
    }

    @Override
    public List<IntegrationSetting> list(boolean maskSecrets) {
        Map<String, IntegrationSetting> mergedSettings = new LinkedHashMap<>();
        for (DefaultSetting defaultSetting : DEFAULT_SETTINGS) {
            mergedSettings.put(defaultSetting.settingKey(), toSetting(defaultSetting));
        }
        for (IntegrationSetting setting : integrationSettingMapper.selectAll()) {
            mergedSettings.put(setting.getSettingKey(), setting);
        }

        return mergedSettings.values()
                .stream()
                .map(setting -> maskSecrets && Integer.valueOf(1).equals(setting.getSecretFlag()) ? masked(setting) : setting)
                .toList();
    }

    @Override
    public void save(IntegrationSettingUpdateRequest request, Long adminId) {
        if (request == null || isBlank(request.settingKey())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "settingKey is required");
        }

        IntegrationSetting setting = new IntegrationSetting();
        setting.setSettingKey(request.settingKey().trim());
        setting.setSecretFlag(request.secretFlag() == null ? 0 : request.secretFlag());
        setting.setSettingValue(resolveSettingValue(setting.getSettingKey(), request.settingValue(), setting.getSecretFlag()));
        setting.setUpdatedBy(adminId);
        integrationSettingMapper.upsert(setting);
    }

    private String resolveSettingValue(String settingKey, String requestedValue, Integer secretFlag) {
        String nextValue = requestedValue == null ? "" : requestedValue.trim();
        if (!Integer.valueOf(1).equals(secretFlag) || !looksMasked(nextValue)) {
            return nextValue;
        }

        IntegrationSetting existing = integrationSettingMapper.selectByKey(settingKey);
        return existing == null ? "" : existing.getSettingValue();
    }

    private IntegrationSetting masked(IntegrationSetting source) {
        IntegrationSetting setting = new IntegrationSetting();
        setting.setSettingKey(source.getSettingKey());
        setting.setSettingValue(mask(source.getSettingValue()));
        setting.setSecretFlag(source.getSecretFlag());
        setting.setUpdatedBy(source.getUpdatedBy());
        setting.setCreatedTime(source.getCreatedTime());
        setting.setUpdatedTime(source.getUpdatedTime());
        return setting;
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= 8) {
            return "********";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private boolean looksMasked(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.contains("...") || value.chars().allMatch(character -> character == '*');
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private IntegrationSetting toSetting(DefaultSetting defaultSetting) {
        IntegrationSetting setting = new IntegrationSetting();
        setting.setSettingKey(defaultSetting.settingKey());
        setting.setSettingValue(defaultSetting.settingValue());
        setting.setSecretFlag(defaultSetting.secretFlag());
        return setting;
    }

    private record DefaultSetting(String settingKey, String settingValue, int secretFlag) {
    }
}

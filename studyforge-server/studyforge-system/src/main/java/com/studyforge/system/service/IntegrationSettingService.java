package com.studyforge.system.service;

import com.studyforge.system.dto.IntegrationSettingUpdateRequest;
import com.studyforge.system.entity.IntegrationSetting;
import java.util.List;
import java.util.Map;

public interface IntegrationSettingService {
    String getValue(String settingKey, String defaultValue);

    Map<String, String> getValues(String prefix);

    List<IntegrationSetting> list(boolean maskSecrets);

    void save(IntegrationSettingUpdateRequest request, Long adminId);
}

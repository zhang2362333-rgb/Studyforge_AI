package com.studyforge.system.dto;

public record IntegrationSettingUpdateRequest(String settingKey,
                                              String settingValue,
                                              Integer secretFlag) {
}

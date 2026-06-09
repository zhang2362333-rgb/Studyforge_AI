package com.studyforge.system.entity;

import java.time.LocalDateTime;

public class IntegrationSetting {
    private String settingKey;
    private String settingValue;
    private Integer secretFlag;
    private Long updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public Integer getSecretFlag() {
        return secretFlag;
    }

    public void setSecretFlag(Integer secretFlag) {
        this.secretFlag = secretFlag;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}

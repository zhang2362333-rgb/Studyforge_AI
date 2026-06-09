package com.studyforge.system.mapper;

import com.studyforge.system.entity.IntegrationSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IntegrationSettingMapper {
    IntegrationSetting selectByKey(@Param("settingKey") String settingKey);

    List<IntegrationSetting> selectAll();

    int upsert(@Param("setting") IntegrationSetting setting);
}

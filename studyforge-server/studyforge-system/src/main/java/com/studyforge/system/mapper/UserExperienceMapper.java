package com.studyforge.system.mapper;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Param;

public interface UserExperienceMapper {
    int insertIgnore(@Param("userId") Long userId,
                     @Param("actionType") String actionType,
                     @Param("experienceDelta") int experienceDelta,
                     @Param("sourceId") Long sourceId,
                     @Param("createdDate") LocalDate createdDate);
}

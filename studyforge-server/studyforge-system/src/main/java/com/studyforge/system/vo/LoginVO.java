package com.studyforge.system.vo;

import com.studyforge.common.enums.RoleType;

public record LoginVO(String accessToken,
                      Long userId,
                      String username,
                      String displayName,
                      RoleType role,
                      int communityLevel,
                      int experiencePoints,
                      boolean dailyRewardApplied,
                      int dailyExperienceDelta) {
}

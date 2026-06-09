package com.studyforge.system.dto;

public record UpdateProfileRequest(String username,
                                   String email,
                                   String displayName,
                                   String bio,
                                   String avatarUrl,
                                   String bannerUrl) {
}

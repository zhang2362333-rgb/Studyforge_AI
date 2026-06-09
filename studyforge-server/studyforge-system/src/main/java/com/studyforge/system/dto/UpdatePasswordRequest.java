package com.studyforge.system.dto;

public record UpdatePasswordRequest(String currentPassword, String newPassword) {
}

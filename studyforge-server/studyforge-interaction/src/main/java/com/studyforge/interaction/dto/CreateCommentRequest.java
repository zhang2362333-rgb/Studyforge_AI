package com.studyforge.interaction.dto;

public record CreateCommentRequest(String content, String languageCode, Long parentCommentId) {
}

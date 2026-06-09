package com.studyforge.content.dto;

public record CreatePostRequest(Long authorId,
                                Long categoryId,
                                String originalLanguage,
                                String coverImageUrl,
                                String title,
                                String summary,
                                String content) {
}

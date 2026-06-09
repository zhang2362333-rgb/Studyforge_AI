package com.studyforge.content.service;

import com.studyforge.content.dto.CreatePostRequest;

public interface PostCommandService {
    Long create(CreatePostRequest request);

    Long create(Long authorId, CreatePostRequest request);

    Long update(Long authorId, Long postId, CreatePostRequest request);
}

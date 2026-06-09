package com.studyforge.content.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.content.dto.CreatePostRequest;
import com.studyforge.content.entity.Post;
import com.studyforge.content.entity.PostI18n;
import com.studyforge.content.mapper.CategoryMapper;
import com.studyforge.content.mapper.PostI18nMapper;
import com.studyforge.content.mapper.PostMapper;
import com.studyforge.content.service.PostCommandService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommandServiceImpl implements PostCommandService {
    private static final String DEFAULT_LANGUAGE = "zh_CN";

    private final PostMapper postMapper;
    private final PostI18nMapper postI18nMapper;
    private final CategoryMapper categoryMapper;

    public PostCommandServiceImpl(PostMapper postMapper, PostI18nMapper postI18nMapper, CategoryMapper categoryMapper) {
        this.postMapper = postMapper;
        this.postI18nMapper = postI18nMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public Long create(CreatePostRequest request) {
        if (request == null || request.authorId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return create(request.authorId(), request);
    }

    @Override
    @Transactional
    public Long create(Long authorId, CreatePostRequest request) {
        if (authorId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (request == null || isBlank(request.title()) || isBlank(request.content())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "title and content are required");
        }

        Long categoryId = request.categoryId() == null ? 1L : request.categoryId();
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "category not found");
        }

        String language = normalizeLanguage(request.originalLanguage());
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setCategoryId(categoryId);
        post.setOriginalLanguage(language);
        post.setStatus("PUBLISHED");
        post.setCoverImageUrl(safeCoverUrl(request.coverImageUrl()));
        post.setFeatured(0);
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setHotScore(BigDecimal.ZERO);
        postMapper.insert(post);

        PostI18n content = new PostI18n();
        content.setPostId(post.getPostId());
        content.setLanguageCode(language);
        content.setTitle(request.title().trim());
        content.setSummary(summary(request));
        content.setContent(request.content().trim());
        content.setContentFormat("MARKDOWN");
        content.setAiTags(null);
        content.setSourceType("ORIGINAL");
        postI18nMapper.insert(content);

        return post.getPostId();
    }

    @Override
    @Transactional
    public Long update(Long authorId, Long postId, CreatePostRequest request) {
        if (authorId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (postId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "postId is required");
        }
        if (request == null || isBlank(request.title()) || isBlank(request.content())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "title and content are required");
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "post not found");
        }
        if (!authorId.equals(post.getAuthorId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "only the author can edit this post");
        }

        Long categoryId = request.categoryId() == null ? post.getCategoryId() : request.categoryId();
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "category not found");
        }

        String previousLanguage = post.getOriginalLanguage();
        String language = normalizeLanguage(request.originalLanguage());
        post.setCategoryId(categoryId);
        post.setOriginalLanguage(language);
        post.setCoverImageUrl(safeCoverUrl(request.coverImageUrl()));
        postMapper.updateById(post);

        PostI18n content = postI18nMapper.selectByPostIdAndLanguage(postId, previousLanguage);
        if (content == null) {
            content = postI18nMapper.selectByPostIdAndLanguage(postId, language);
        }
        if (content == null) {
            content = postI18nMapper.selectByPostId(postId)
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        if (content == null) {
            content = new PostI18n();
            content.setPostId(postId);
            content.setLanguageCode(language);
            content.setTitle(request.title().trim());
            content.setSummary(summary(request));
            content.setContent(request.content().trim());
            content.setContentFormat("MARKDOWN");
            content.setAiTags(null);
            content.setSourceType("ORIGINAL");
            postI18nMapper.insert(content);
        } else {
            content.setLanguageCode(language);
            content.setTitle(request.title().trim());
            content.setSummary(summary(request));
            content.setContent(request.content().trim());
            content.setContentFormat("MARKDOWN");
            content.setSourceType("ORIGINAL");
            postI18nMapper.updateById(content);
        }

        return postId;
    }

    private String normalizeLanguage(String languageCode) {
        if (isBlank(languageCode)) {
            return DEFAULT_LANGUAGE;
        }
        return languageCode.trim();
    }

    private String summary(CreatePostRequest request) {
        if (!isBlank(request.summary())) {
            return request.summary().trim();
        }
        String content = request.content()
                .trim()
                .replaceAll("!\\[[^\\]]*]\\([^)]*\\)", "")
                .replaceAll("[#*_`>\\-\\[\\]()]","")
                .replaceAll("\\s+", " ");
        return content.length() <= 120 ? content : content.substring(0, 120) + "...";
    }

    private String safeCoverUrl(String coverImageUrl) {
        if (isBlank(coverImageUrl)) {
            return null;
        }
        String value = coverImageUrl.trim();
        if (!value.startsWith("/api/v1/files/") && !value.startsWith("http://") && !value.startsWith("https://")) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "cover image url is invalid");
        }
        return value.length() <= 512 ? value : value.substring(0, 512);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

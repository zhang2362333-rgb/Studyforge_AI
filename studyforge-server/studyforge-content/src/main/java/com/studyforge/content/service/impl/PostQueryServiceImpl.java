package com.studyforge.content.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.content.entity.Category;
import com.studyforge.content.entity.Post;
import com.studyforge.content.entity.PostI18n;
import com.studyforge.content.mapper.CategoryMapper;
import com.studyforge.content.mapper.PostI18nMapper;
import com.studyforge.content.mapper.PostMapper;
import com.studyforge.content.service.PostQueryService;
import com.studyforge.content.vo.PostDetailVO;
import com.studyforge.content.vo.PostSummaryVO;
import com.studyforge.system.entity.User;
import com.studyforge.system.mapper.UserMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PostQueryServiceImpl implements PostQueryService {
    private static final String DEFAULT_LANGUAGE = "zh_CN";

    private final PostMapper postMapper;
    private final PostI18nMapper postI18nMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public PostQueryServiceImpl(PostMapper postMapper, PostI18nMapper postI18nMapper, CategoryMapper categoryMapper, UserMapper userMapper) {
        this.postMapper = postMapper;
        this.postI18nMapper = postI18nMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PostDetailVO getDetail(Long postId, String languageCode) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }

        PostI18n content = resolveContent(post, normalizeLanguage(languageCode));
        if (content == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "post content not found");
        }

        Category category = categoryMapper.selectById(post.getCategoryId());
        User author = userMapper.selectById(post.getAuthorId());
        return new PostDetailVO(
                post.getPostId(),
                post.getAuthorId(),
                authorName(author, post.getAuthorId()),
                author == null ? "" : value(author.getAvatarUrl()),
                content.getTitle(),
                content.getSummary(),
                content.getContent(),
                content.getLanguageCode(),
                category == null ? "UNKNOWN" : category.getCategoryCode(),
                post.getCoverImageUrl(),
                content.getContentFormat() == null ? "MARKDOWN" : content.getContentFormat(),
                safeInt(post.getLikeCount()),
                safeInt(post.getFavoriteCount()),
                safeInt(post.getCommentCount()),
                safeInt(post.getViewCount()),
                toDouble(post.getHotScore()),
                post.getCreatedTime(),
                post.getUpdatedTime()
        );
    }

    @Override
    public List<PostSummaryVO> getTrending(String languageCode, int limit) {
        int normalizedLimit = limit <= 0 ? 10 : Math.min(limit, 20);

        return postMapper.selectPublishedByHotScore(normalizedLimit)
                .stream()
                .map(this::toOriginalSummary)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<PostSummaryVO> list(String languageCode, String categoryCode, String keyword, int limit) {
        int normalizedLimit = normalizeLimit(limit, 30);
        return postMapper.selectPublished(emptyToNull(categoryCode), emptyToNull(keyword), normalizedLimit)
                .stream()
                .map(this::toOriginalSummary)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<PostSummaryVO> listByAuthor(Long authorId, String languageCode, int limit) {
        int normalizedLimit = normalizeLimit(limit, 30);
        return postMapper.selectPublishedByAuthor(authorId, normalizedLimit)
                .stream()
                .map(post -> toSummary(post, normalizeLanguage(languageCode)))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<PostSummaryVO> listFavorites(Long userId, String languageCode, int limit) {
        int normalizedLimit = normalizeLimit(limit, 30);
        return postMapper.selectFavoritesByUser(userId, normalizedLimit)
                .stream()
                .map(post -> toSummary(post, normalizeLanguage(languageCode)))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<PostSummaryVO> listFavoriteCollection(Long userId, Long collectionId, String languageCode, int limit) {
        int normalizedLimit = normalizeLimit(limit, 30);
        return postMapper.selectFavoriteCollectionByUser(userId, collectionId, normalizedLimit)
                .stream()
                .map(post -> toSummary(post, normalizeLanguage(languageCode)))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<PostSummaryVO> listHistory(Long userId, String languageCode, int limit) {
        int normalizedLimit = normalizeLimit(limit, 30);
        return postMapper.selectHistoryByUser(userId, normalizedLimit)
                .stream()
                .map(post -> toSummary(post, normalizeLanguage(languageCode)))
                .filter(Objects::nonNull)
                .toList();
    }

    private PostSummaryVO toSummary(Post post, String languageCode) {
        PostI18n content = resolveContent(post, languageCode);
        return toSummary(post, content);
    }

    private PostSummaryVO toOriginalSummary(Post post) {
        PostI18n content = resolveContent(post, normalizeLanguage(post.getOriginalLanguage()));
        return toSummary(post, content);
    }

    private PostSummaryVO toSummary(Post post, PostI18n content) {
        if (content == null) {
            return null;
        }

        return new PostSummaryVO(
                post.getPostId(),
                post.getAuthorId(),
                authorName(userMapper.selectById(post.getAuthorId()), post.getAuthorId()),
                authorAvatar(post.getAuthorId()),
                content.getTitle(),
                content.getSummary(),
                content.getLanguageCode(),
                categoryCode(post.getCategoryId()),
                post.getCoverImageUrl(),
                safeInt(post.getLikeCount()),
                safeInt(post.getFavoriteCount()),
                safeInt(post.getCommentCount()),
                safeInt(post.getViewCount()),
                toDouble(post.getHotScore()),
                post.getCreatedTime(),
                post.getUpdatedTime()
        );
    }

    private String authorName(User author, Long authorId) {
        if (author == null) {
            return "#" + authorId;
        }
        return author.getDisplayName() == null || author.getDisplayName().isBlank() ? author.getUsername() : author.getDisplayName();
    }

    private String authorAvatar(Long authorId) {
        User author = userMapper.selectById(authorId);
        return author == null ? "" : value(author.getAvatarUrl());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String categoryCode(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? "UNKNOWN" : category.getCategoryCode();
    }

    private PostI18n resolveContent(Post post, String languageCode) {
        PostI18n content = postI18nMapper.selectByPostIdAndLanguage(post.getPostId(), languageCode);
        if (content != null) {
            return content;
        }

        if (post.getOriginalLanguage() != null && !post.getOriginalLanguage().equals(languageCode)) {
            content = postI18nMapper.selectByPostIdAndLanguage(post.getPostId(), post.getOriginalLanguage());
            if (content != null) {
                return content;
            }
        }

        List<PostI18n> allContent = postI18nMapper.selectByPostId(post.getPostId());
        return allContent.isEmpty() ? null : allContent.get(0);
    }

    private String normalizeLanguage(String languageCode) {
        return languageCode == null || languageCode.isBlank() ? DEFAULT_LANGUAGE : languageCode;
    }

    private int normalizeLimit(int limit, int defaultLimit) {
        return limit <= 0 ? defaultLimit : Math.min(limit, 50);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}

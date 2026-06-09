package com.studyforge.interaction.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.content.entity.Post;
import com.studyforge.content.mapper.PostMapper;
import com.studyforge.interaction.dto.CreateCommentRequest;
import com.studyforge.interaction.entity.Comment;
import com.studyforge.interaction.entity.FavoriteCollection;
import com.studyforge.interaction.mapper.CommentMapper;
import com.studyforge.interaction.mapper.FavoriteCollectionMapper;
import com.studyforge.interaction.mapper.PostFavoriteMapper;
import com.studyforge.interaction.mapper.PostLikeMapper;
import com.studyforge.interaction.mapper.PostViewHistoryMapper;
import com.studyforge.interaction.service.InteractionCommandService;
import com.studyforge.interaction.vo.CommentVO;
import com.studyforge.interaction.vo.PostInteractionStateVO;
import com.studyforge.system.entity.User;
import com.studyforge.system.mapper.UserMapper;
import com.studyforge.system.service.NotificationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionCommandServiceImpl implements InteractionCommandService {
    private static final String DEFAULT_LANGUAGE = "zh_CN";
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<![\\p{L}\\p{N}_])@([\\p{L}\\p{N}_\\-.]{2,32})");

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostViewHistoryMapper postViewHistoryMapper;
    private final FavoriteCollectionMapper favoriteCollectionMapper;
    private final NotificationService notificationService;

    public InteractionCommandServiceImpl(PostMapper postMapper,
                                         CommentMapper commentMapper,
                                         UserMapper userMapper,
                                         PostLikeMapper postLikeMapper,
                                         PostFavoriteMapper postFavoriteMapper,
                                         PostViewHistoryMapper postViewHistoryMapper,
                                         FavoriteCollectionMapper favoriteCollectionMapper,
                                         NotificationService notificationService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postViewHistoryMapper = postViewHistoryMapper;
        this.favoriteCollectionMapper = favoriteCollectionMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PostInteractionStateVO like(Long postId, Long userId) {
        Post post = assertPost(postId);
        if (postLikeMapper.countByPostAndUser(postId, userId) > 0) {
            postLikeMapper.deleteByPostAndUser(postId, userId);
            postMapper.incrementLikeCount(postId, -1);
        } else if (postLikeMapper.insertIgnore(postId, userId) > 0) {
            postMapper.incrementLikeCount(postId, 1);
            notificationService.notifyPostLiked(post.getAuthorId(), userId, postId, postMapper.selectOriginalTitle(postId));
        }
        return state(postId, userId);
    }

    @Override
    @Transactional
    public PostInteractionStateVO favorite(Long postId, Long userId) {
        Post post = assertPost(postId);
        if (postFavoriteMapper.countByPostAndUser(postId, userId) > 0) {
            postFavoriteMapper.deleteByPostAndUser(postId, userId);
            favoriteCollectionMapper.deleteItemsByPostAndUser(postId, userId);
            postMapper.incrementFavoriteCount(postId, -1);
        } else if (postFavoriteMapper.insertIgnore(postId, userId) > 0) {
            favoriteCollectionMapper.insertIgnoreDefault(userId);
            FavoriteCollection defaultCollection = favoriteCollectionMapper.selectDefaultByUser(userId);
            if (defaultCollection != null) {
                favoriteCollectionMapper.insertIgnoreItem(defaultCollection.getCollectionId(), postId, userId);
            }
            postMapper.incrementFavoriteCount(postId, 1);
            notificationService.notifyPostFavorited(post.getAuthorId(), userId, postId, postMapper.selectOriginalTitle(postId));
        }
        return state(postId, userId);
    }

    @Override
    public PostInteractionStateVO state(Long postId, Long userId) {
        Post post = assertPost(postId);
        boolean liked = userId != null && postLikeMapper.countByPostAndUser(postId, userId) > 0;
        boolean favorited = userId != null && postFavoriteMapper.countByPostAndUser(postId, userId) > 0;
        return new PostInteractionStateVO(
                liked,
                favorited,
                safeInt(post.getLikeCount()),
                safeInt(post.getFavoriteCount()),
                safeInt(post.getCommentCount()),
                safeInt(post.getViewCount())
        );
    }

    @Override
    @Transactional
    public void recordView(Long postId, Long userId) {
        assertPost(postId);
        postViewHistoryMapper.insert(postId, userId);
        postMapper.incrementViewCount(postId);
    }

    @Override
    @Transactional
    public CommentVO comment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = assertPost(postId);
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "comment content is required");
        }
        Comment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentMapper.selectById(request.parentCommentId());
            if (parent == null || !postId.equals(parent.getPostId()) || !"VISIBLE".equals(parent.getStatus())) {
                throw new BizException(ErrorCode.NOT_FOUND, "parent comment not found");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentCommentId(parent == null ? null : parent.getCommentId());
        comment.setUserId(userId);
        comment.setLanguageCode(isBlank(request.languageCode()) ? DEFAULT_LANGUAGE : request.languageCode().trim());
        comment.setContent(request.content().trim());
        comment.setStatus("VISIBLE");
        comment.setFloorNo(commentMapper.nextFloorNo(postId));
        comment.setLikeCount(0);
        commentMapper.insert(comment);
        postMapper.incrementCommentCount(postId, 1);
        String postTitle = postMapper.selectOriginalTitle(postId);
        Set<Long> notifiedRecipients = new HashSet<>();
        if (parent == null) {
            notifyPostCommented(notifiedRecipients, post.getAuthorId(), userId, postId, comment.getCommentId(), postTitle, comment.getContent());
        } else {
            notifyPostCommentReplied(notifiedRecipients, parent.getUserId(), userId, postId, comment.getCommentId(), postTitle, comment.getContent());
        }
        notifyPostMentions(notifiedRecipients, userId, postId, comment.getCommentId(), postTitle, comment.getContent());
        Comment created = commentMapper.selectByIdForViewer(comment.getCommentId(), userId);
        return toVO(created == null ? comment : created, post, userId);
    }

    @Override
    public List<CommentVO> comments(Long postId, Long viewerId) {
        Post post = assertPost(postId);
        return commentMapper.selectByPostIdForViewer(postId, viewerId)
                .stream()
                .map(comment -> toVO(comment, post, viewerId))
                .toList();
    }

    @Override
    @Transactional
    public CommentVO likeComment(Long postId, Long commentId, Long userId) {
        Post post = assertPost(postId);
        Comment comment = requireComment(postId, commentId);
        if (!"VISIBLE".equals(comment.getStatus())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "deleted comment cannot be liked");
        }
        if (commentMapper.countLike(commentId, userId) > 0) {
            commentMapper.deleteLike(commentId, userId);
            commentMapper.incrementLikeCount(commentId, -1);
        } else if (commentMapper.insertLike(commentId, userId) > 0) {
            commentMapper.incrementLikeCount(commentId, 1);
            notificationService.notifyPostCommentLiked(comment.getUserId(), userId, postId, commentId, postMapper.selectOriginalTitle(postId));
        }
        return toVO(commentMapper.selectByIdForViewer(commentId, userId), post, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Post post = assertPost(postId);
        Comment comment = requireComment(postId, commentId);
        if (!userId.equals(comment.getUserId()) && !userId.equals(post.getAuthorId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "only the comment author or post owner can delete this comment");
        }
        if (commentMapper.markDeleted(commentId) > 0) {
            postMapper.incrementCommentCount(postId, -1);
        }
    }

    private Post assertPost(Long postId) {
        if (postId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "postId is required");
        }
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "post not found");
        }
        return post;
    }

    private Comment requireComment(Long postId, Long commentId) {
        if (commentId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "commentId is required");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !postId.equals(comment.getPostId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "comment not found");
        }
        return comment;
    }

    private CommentVO toVO(Comment comment, Post post, Long viewerId) {
        boolean deleted = "DELETED".equals(comment.getStatus());
        boolean canDelete = viewerId != null
                && !deleted
                && (viewerId.equals(comment.getUserId()) || viewerId.equals(post.getAuthorId()));
        return new CommentVO(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getUserId(),
                fallback(comment.getAuthorUsername(), "#" + comment.getUserId()),
                fallback(comment.getAuthorName(), fallback(comment.getAuthorUsername(), "#" + comment.getUserId())),
                comment.getAuthorAvatarUrl(),
                comment.getParentUserId(),
                comment.getParentAuthorUsername(),
                comment.getParentAuthorName(),
                comment.getLanguageCode(),
                deleted ? "这条评论已删除" : comment.getContent(),
                comment.getStatus(),
                safeInt(comment.getFloorNo()),
                safeInt(comment.getLikeCount()),
                Boolean.TRUE.equals(comment.getLikedByViewer()),
                canDelete,
                deleted,
                comment.getCreatedTime(),
                comment.getUpdatedTime()
        );
    }

    private void notifyPostMentions(Set<Long> notifiedRecipients,
                                    Long actorId,
                                    Long postId,
                                    Long commentId,
                                    String postTitle,
                                    String content) {
        for (Long recipientId : mentionedUserIds(content)) {
            if (notifiedRecipients.add(recipientId)) {
                notificationService.notifyPostCommentMentioned(recipientId, actorId, postId, commentId, postTitle, content);
            }
        }
    }

    private void notifyPostCommented(Set<Long> notifiedRecipients,
                                     Long recipientId,
                                     Long actorId,
                                     Long postId,
                                     Long commentId,
                                     String postTitle,
                                     String content) {
        if (notifiedRecipients.add(recipientId)) {
            notificationService.notifyPostCommented(recipientId, actorId, postId, commentId, postTitle, content);
        }
    }

    private void notifyPostCommentReplied(Set<Long> notifiedRecipients,
                                          Long recipientId,
                                          Long actorId,
                                          Long postId,
                                          Long commentId,
                                          String postTitle,
                                          String content) {
        if (notifiedRecipients.add(recipientId)) {
            notificationService.notifyPostCommentReplied(recipientId, actorId, postId, commentId, postTitle, content);
        }
    }

    private Set<Long> mentionedUserIds(String content) {
        Set<String> handles = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            handles.add(matcher.group(1).toLowerCase());
        }
        if (handles.isEmpty()) {
            return Set.of();
        }
        List<User> users = userMapper.selectMentionCandidates(new ArrayList<>(handles));
        Set<Long> userIds = new LinkedHashSet<>();
        for (User user : users) {
            userIds.add(user.getUserId());
        }
        return userIds;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

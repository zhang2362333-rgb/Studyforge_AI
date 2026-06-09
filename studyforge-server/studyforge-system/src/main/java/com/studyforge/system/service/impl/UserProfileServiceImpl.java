package com.studyforge.system.service.impl;

import com.studyforge.common.enums.UserStatus;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.dto.FriendMessageRequest;
import com.studyforge.system.dto.FriendRequestCreateRequest;
import com.studyforge.system.dto.FriendRequestReviewRequest;
import com.studyforge.system.dto.UpdateProfileRequest;
import com.studyforge.system.dto.UpdatePasswordRequest;
import com.studyforge.system.entity.User;
import com.studyforge.system.mapper.UserMapper;
import com.studyforge.system.mapper.UserSocialMapper;
import com.studyforge.system.service.NotificationService;
import com.studyforge.system.service.UserProfileService;
import com.studyforge.system.vo.FriendMessageVO;
import com.studyforge.system.vo.FriendRequestVO;
import com.studyforge.system.vo.SocialUserVO;
import com.studyforge.system.vo.UserActivityVO;
import com.studyforge.system.vo.UserProfileVO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserMapper userMapper;
    private final UserSocialMapper userSocialMapper;
    private final NotificationService notificationService;

    public UserProfileServiceImpl(UserMapper userMapper, UserSocialMapper userSocialMapper, NotificationService notificationService) {
        this.userMapper = userMapper;
        this.userSocialMapper = userSocialMapper;
        this.notificationService = notificationService;
    }

    @Override
    public UserProfileVO getProfile(Long viewerId, Long userId) {
        User user = requireActiveUser(userId);
        return toProfile(user, viewerId);
    }

    @Override
    @Transactional
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = requireActiveUser(userId);
        if (request != null) {
            String username = limit(text(request.username()), 50);
            String email = limit(text(request.email()), 100);
            if (username.isBlank() || email.isBlank()) {
                throw new BizException(ErrorCode.VALIDATION_ERROR, "username and email are required");
            }
            assertAccountAvailable(username, userId);
            assertAccountAvailable(email, userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setDisplayName(limit(text(request.displayName()), 80));
            user.setBio(limit(text(request.bio()), 300));
            user.setAvatarUrl(limit(text(request.avatarUrl()), 512));
            user.setBannerUrl(limit(text(request.bannerUrl()), 512));
            userMapper.updateById(user);
        }
        return getProfile(userId, userId);
    }

    @Override
    @Transactional
    public UserProfileVO updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = requireActiveUser(userId);
        if (request == null || text(request.currentPassword()).isBlank() || text(request.newPassword()).length() < 8) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "current password and a new password of at least 8 characters are required");
        }
        if (!hashPassword(request.currentPassword()).equals(user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "current password is incorrect");
        }
        userMapper.updatePassword(userId, hashPassword(request.newPassword()));
        return getProfile(userId, userId);
    }

    @Override
    @Transactional
    public UserProfileVO follow(Long viewerId, Long targetUserId) {
        if (viewerId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (viewerId.equals(targetUserId)) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "cannot follow yourself");
        }
        requireActiveUser(viewerId);
        requireActiveUser(targetUserId);
        userSocialMapper.upsertFollow(viewerId, targetUserId);
        return getProfile(viewerId, targetUserId);
    }

    @Override
    @Transactional
    public UserProfileVO unfollow(Long viewerId, Long targetUserId) {
        if (viewerId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (!viewerId.equals(targetUserId)) {
            userSocialMapper.inactivateFollow(viewerId, targetUserId);
        }
        return getProfile(viewerId, targetUserId);
    }

    @Override
    public List<SocialUserVO> listFollowers(Long viewerId, Long userId, int limit) {
        requireActiveUser(userId);
        return userSocialMapper.selectFollowers(userId, normalizeLimit(limit))
                .stream()
                .map(user -> toSocialUser(user, viewerId))
                .toList();
    }

    @Override
    public List<SocialUserVO> listFollowing(Long viewerId, Long userId, int limit) {
        requireActiveUser(userId);
        return userSocialMapper.selectFollowing(userId, normalizeLimit(limit))
                .stream()
                .map(user -> toSocialUser(user, viewerId))
                .toList();
    }

    @Override
    public List<SocialUserVO> listFriends(Long viewerId, Long userId, int limit) {
        requireActiveUser(userId);
        return userSocialMapper.selectFriends(userId, normalizeLimit(limit))
                .stream()
                .map(user -> toSocialUser(user, viewerId))
                .toList();
    }

    @Override
    public List<UserActivityVO> listActivities(Long viewerId, Long userId, String languageCode, int limit) {
        requireActiveUser(userId);
        String normalizedLanguage = text(languageCode).isBlank() ? "zh_CN" : text(languageCode);
        return userSocialMapper.selectActivities(userId, normalizedLanguage, normalizeLimit(limit))
                .stream()
                .map(this::toActivity)
                .toList();
    }

    @Override
    @Transactional
    public FriendRequestVO sendFriendRequest(Long requesterId, Long addresseeId, FriendRequestCreateRequest request) {
        if (requesterId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (requesterId.equals(addresseeId)) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "cannot add yourself as a friend");
        }
        requireActiveUser(requesterId);
        requireActiveUser(addresseeId);
        long low = Math.min(requesterId, addresseeId);
        long high = Math.max(requesterId, addresseeId);
        if (userSocialMapper.countFriendship(low, high) > 0) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "already friends");
        }
        if (userSocialMapper.selectPendingFriendRequest(addresseeId, requesterId) != null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "this user has already sent you a friend request");
        }
        boolean alreadyPending = userSocialMapper.selectPendingFriendRequest(requesterId, addresseeId) != null;
        userSocialMapper.upsertFriendRequest(requesterId, addresseeId, limit(text(request == null ? null : request.message()), 300));
        FriendRequestVO friendRequest = toFriendRequest(userSocialMapper.selectPendingFriendRequest(requesterId, addresseeId), requesterId);
        if (!alreadyPending) {
            notificationService.notifyFriendRequest(addresseeId, requesterId, friendRequest.requestId(), friendRequest.message());
        }
        return friendRequest;
    }

    @Override
    @Transactional
    public FriendRequestVO reviewFriendRequest(Long userId, Long requestId, FriendRequestReviewRequest request) {
        requireActiveUser(userId);
        Map<String, Object> row = userSocialMapper.selectFriendRequestById(requestId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "friend request not found");
        }
        if (!userId.equals(longValue(row.get("addresseeId")))) {
            throw new BizException(ErrorCode.FORBIDDEN, "only the recipient can review this request");
        }
        if (!"PENDING".equals(stringValue(row.get("status")))) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "friend request has already been handled");
        }

        String decision = request == null || request.decision() == null ? "" : request.decision().trim().toUpperCase(Locale.ROOT);
        if ("ACCEPT".equals(decision)) {
            Long requesterId = longValue(row.get("requesterId"));
            Long addresseeId = longValue(row.get("addresseeId"));
            userSocialMapper.updateFriendRequestStatus(requestId, "ACCEPTED");
            userSocialMapper.upsertFriendship(Math.min(requesterId, addresseeId), Math.max(requesterId, addresseeId));
        } else if ("REJECT".equals(decision)) {
            userSocialMapper.updateFriendRequestStatus(requestId, "REJECTED");
        } else {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "unsupported decision");
        }

        return toFriendRequest(userSocialMapper.selectFriendRequestById(requestId), userId);
    }

    @Override
    public List<FriendRequestVO> listIncomingFriendRequests(Long userId, String status, int limit) {
        requireActiveUser(userId);
        return userSocialMapper.selectIncomingFriendRequests(userId, normalizeStatus(status), normalizeLimit(limit))
                .stream()
                .map(row -> toFriendRequest(row, userId))
                .toList();
    }

    @Override
    public List<FriendRequestVO> listOutgoingFriendRequests(Long userId, String status, int limit) {
        requireActiveUser(userId);
        return userSocialMapper.selectOutgoingFriendRequests(userId, normalizeStatus(status), normalizeLimit(limit))
                .stream()
                .map(row -> toFriendRequest(row, userId))
                .toList();
    }

    @Override
    @Transactional
    public List<FriendMessageVO> listFriendMessages(Long userId, Long friendId, int limit) {
        requireFriendship(userId, friendId);
        userSocialMapper.markMessagesRead(userId, friendId);
        return userSocialMapper.selectFriendMessages(userId, friendId, normalizeLimit(limit))
                .stream()
                .map(this::toFriendMessage)
                .sorted((left, right) -> left.createdTime().compareTo(right.createdTime()))
                .toList();
    }

    @Override
    @Transactional
    public FriendMessageVO sendFriendMessage(Long userId, Long friendId, FriendMessageRequest request) {
        requireFriendship(userId, friendId);
        String content = limit(text(request == null ? null : request.content()), 2000);
        if (content.isBlank()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "message content is required");
        }
        userSocialMapper.insertFriendMessage(userId, friendId, content);
        return userSocialMapper.selectFriendMessages(userId, friendId, 1)
                .stream()
                .findFirst()
                .map(this::toFriendMessage)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "message not found"));
    }

    private User requireActiveUser(Long userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "userId is required");
        }
        User user = userMapper.selectById(userId);
        if (user == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "user not found");
        }
        return user;
    }

    private UserProfileVO toProfile(User user, Long viewerId) {
        int experience = safeInt(user.getExperiencePoints(), 0);
        int nextLevelExperience = safeInt(user.getCommunityLevel(), 1) * 100;
        boolean self = viewerId != null && viewerId.equals(user.getUserId());
        boolean followedByViewer = viewerId != null
                && !viewerId.equals(user.getUserId())
                && userSocialMapper.countFollow(viewerId, user.getUserId()) > 0;
        FriendState friendState = friendState(viewerId, user.getUserId());

        return new UserProfileVO(
                user.getUserId(),
                user.getUsername(),
                self ? user.getEmail() : "",
                displayName(user),
                value(user.getBio()),
                value(user.getAvatarUrl()),
                value(user.getBannerUrl()),
                safeInt(user.getCommunityLevel(), 1),
                experience,
                nextLevelExperience,
                safeInt(user.getReputationScore(), 0),
                userSocialMapper.countPosts(user.getUserId()),
                userSocialMapper.countFavorites(user.getUserId()),
                userSocialMapper.countHistory(user.getUserId()),
                userSocialMapper.countFollowers(user.getUserId()),
                userSocialMapper.countFollowing(user.getUserId()),
                userSocialMapper.countFriends(user.getUserId()),
                userSocialMapper.countComments(user.getUserId()),
                userSocialMapper.sumReceivedLikes(user.getUserId()),
                followedByViewer,
                friendState.status(),
                friendState.requestId(),
                self
        );
    }

    private SocialUserVO toSocialUser(User user, Long viewerId) {
        boolean followed = viewerId != null
                && !viewerId.equals(user.getUserId())
                && userSocialMapper.countFollow(viewerId, user.getUserId()) > 0;
        return new SocialUserVO(
                user.getUserId(),
                user.getUsername(),
                displayName(user),
                value(user.getAvatarUrl()),
                safeInt(user.getCommunityLevel(), 1),
                value(user.getBio()),
                followed
        );
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? 20 : Math.min(limit, 50);
    }

    private void assertAccountAvailable(String account, Long ownerId) {
        User existing = userMapper.selectByAccount(account);
        if (existing != null && !existing.getUserId().equals(ownerId)) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "username or email already exists");
        }
    }

    private void requireFriendship(Long userId, Long friendId) {
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        requireActiveUser(userId);
        requireActiveUser(friendId);
        if (userSocialMapper.countFriendship(Math.min(userId, friendId), Math.max(userId, friendId)) == 0) {
            throw new BizException(ErrorCode.FORBIDDEN, "friend relationship is required");
        }
    }

    private FriendState friendState(Long viewerId, Long targetUserId) {
        if (viewerId == null) {
            return new FriendState("NONE", null);
        }
        if (viewerId.equals(targetUserId)) {
            return new FriendState("SELF", null);
        }
        if (userSocialMapper.countFriendship(Math.min(viewerId, targetUserId), Math.max(viewerId, targetUserId)) > 0) {
            return new FriendState("FRIEND", null);
        }
        Map<String, Object> sent = userSocialMapper.selectPendingFriendRequest(viewerId, targetUserId);
        if (sent != null) {
            return new FriendState("PENDING_SENT", longValue(sent.get("requestId")));
        }
        Map<String, Object> received = userSocialMapper.selectPendingFriendRequest(targetUserId, viewerId);
        if (received != null) {
            return new FriendState("PENDING_RECEIVED", longValue(received.get("requestId")));
        }
        return new FriendState("NONE", null);
    }

    private FriendRequestVO toFriendRequest(Map<String, Object> row, Long viewerId) {
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "friend request not found");
        }
        return new FriendRequestVO(
                longValue(row.get("requestId")),
                toSocialUserFromRow(row, "requester", viewerId),
                toSocialUserFromRow(row, "addressee", viewerId),
                stringValue(row.get("message")),
                stringValue(row.get("status")),
                timeValue(row.get("createdTime")),
                timeValue(row.get("processedTime"))
        );
    }

    private SocialUserVO toSocialUserFromRow(Map<String, Object> row, String prefix, Long viewerId) {
        Long userId = longValue(row.get(prefix + "Id"));
        boolean followed = viewerId != null && !viewerId.equals(userId) && userSocialMapper.countFollow(viewerId, userId) > 0;
        return new SocialUserVO(
                userId,
                stringValue(row.get(prefix + "Username")),
                stringValue(row.get(prefix + "DisplayName")),
                stringValue(row.get(prefix + "AvatarUrl")),
                intValue(row.get(prefix + "CommunityLevel")),
                stringValue(row.get(prefix + "Bio")),
                followed
        );
    }

    private FriendMessageVO toFriendMessage(Map<String, Object> row) {
        return new FriendMessageVO(
                longValue(row.get("messageId")),
                longValue(row.get("senderId")),
                longValue(row.get("receiverId")),
                stringValue(row.get("senderDisplayName")),
                stringValue(row.get("senderAvatarUrl")),
                stringValue(row.get("receiverDisplayName")),
                stringValue(row.get("receiverAvatarUrl")),
                stringValue(row.get("content")),
                intValue(row.get("readFlag")) == 1,
                timeValue(row.get("createdTime"))
        );
    }

    private UserActivityVO toActivity(Map<String, Object> row) {
        return new UserActivityVO(
                stringValue(row.get("activityKey")),
                stringValue(row.get("activityType")),
                stringValue(row.get("targetType")),
                longValue(row.get("targetId")),
                longValue(row.get("postId")),
                longValue(row.get("helpId")),
                longValue(row.get("commentId")),
                longValue(row.get("answerId")),
                stringValue(row.get("title")),
                stringValue(row.get("summary")),
                stringValue(row.get("content")),
                stringValue(row.get("languageCode")),
                stringValue(row.get("categoryCode")),
                stringValue(row.get("coverImageUrl")),
                intValue(row.get("likeCount")),
                intValue(row.get("favoriteCount")),
                intValue(row.get("commentCount")),
                intValue(row.get("viewCount")),
                timeValue(row.get("createdTime"))
        );
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "ALL" : status.trim().toUpperCase(Locale.ROOT);
    }

    private String displayName(User user) {
        return user.getDisplayName() == null || user.getDisplayName().isBlank() ? user.getUsername() : user.getDisplayName();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private int safeInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private LocalDateTime timeValue(Object value) {
        if (value instanceof LocalDateTime time) {
            return time;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record FriendState(String status, Long requestId) {
    }
}

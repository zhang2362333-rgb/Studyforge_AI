package com.studyforge.system.service;

import com.studyforge.system.dto.FriendMessageRequest;
import com.studyforge.system.dto.FriendRequestCreateRequest;
import com.studyforge.system.dto.FriendRequestReviewRequest;
import com.studyforge.system.dto.UpdateProfileRequest;
import com.studyforge.system.dto.UpdatePasswordRequest;
import com.studyforge.system.vo.FriendMessageVO;
import com.studyforge.system.vo.FriendRequestVO;
import com.studyforge.system.vo.SocialUserVO;
import com.studyforge.system.vo.UserActivityVO;
import com.studyforge.system.vo.UserProfileVO;
import java.util.List;

public interface UserProfileService {
    UserProfileVO getProfile(Long viewerId, Long userId);

    UserProfileVO updateProfile(Long userId, UpdateProfileRequest request);

    UserProfileVO updatePassword(Long userId, UpdatePasswordRequest request);

    UserProfileVO follow(Long viewerId, Long targetUserId);

    UserProfileVO unfollow(Long viewerId, Long targetUserId);

    List<SocialUserVO> listFollowers(Long viewerId, Long userId, int limit);

    List<SocialUserVO> listFollowing(Long viewerId, Long userId, int limit);

    List<SocialUserVO> listFriends(Long viewerId, Long userId, int limit);

    List<UserActivityVO> listActivities(Long viewerId, Long userId, String languageCode, int limit);

    FriendRequestVO sendFriendRequest(Long requesterId, Long addresseeId, FriendRequestCreateRequest request);

    FriendRequestVO reviewFriendRequest(Long userId, Long requestId, FriendRequestReviewRequest request);

    List<FriendRequestVO> listIncomingFriendRequests(Long userId, String status, int limit);

    List<FriendRequestVO> listOutgoingFriendRequests(Long userId, String status, int limit);

    List<FriendMessageVO> listFriendMessages(Long userId, Long friendId, int limit);

    FriendMessageVO sendFriendMessage(Long userId, Long friendId, FriendMessageRequest request);
}

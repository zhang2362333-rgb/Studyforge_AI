package com.studyforge.system.mapper;

import com.studyforge.system.entity.User;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface UserSocialMapper {
    int countPosts(@Param("userId") Long userId);

    int countFavorites(@Param("userId") Long userId);

    int countHistory(@Param("userId") Long userId);

    int countComments(@Param("userId") Long userId);

    int sumReceivedLikes(@Param("userId") Long userId);

    int countFollowers(@Param("userId") Long userId);

    int countFollowing(@Param("userId") Long userId);

    int countMutualFriends(@Param("userId") Long userId);

    int countFriends(@Param("userId") Long userId);

    int countFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    int upsertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    int inactivateFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    List<User> selectFollowers(@Param("userId") Long userId, @Param("limit") int limit);

    List<User> selectFollowing(@Param("userId") Long userId, @Param("limit") int limit);

    List<User> selectFriends(@Param("userId") Long userId, @Param("limit") int limit);

    List<Map<String, Object>> selectActivities(@Param("userId") Long userId,
                                               @Param("languageCode") String languageCode,
                                               @Param("limit") int limit);

    int countFriendship(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    int upsertFriendship(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    int upsertFriendRequest(@Param("requesterId") Long requesterId,
                            @Param("addresseeId") Long addresseeId,
                            @Param("message") String message);

    Map<String, Object> selectFriendRequestById(@Param("requestId") Long requestId);

    Map<String, Object> selectPendingFriendRequest(@Param("requesterId") Long requesterId,
                                                   @Param("addresseeId") Long addresseeId);

    List<Map<String, Object>> selectIncomingFriendRequests(@Param("userId") Long userId,
                                                           @Param("status") String status,
                                                           @Param("limit") int limit);

    List<Map<String, Object>> selectOutgoingFriendRequests(@Param("userId") Long userId,
                                                           @Param("status") String status,
                                                           @Param("limit") int limit);

    int updateFriendRequestStatus(@Param("requestId") Long requestId, @Param("status") String status);

    int insertFriendMessage(@Param("senderId") Long senderId,
                            @Param("receiverId") Long receiverId,
                            @Param("content") String content);

    List<Map<String, Object>> selectFriendMessages(@Param("userId") Long userId,
                                                   @Param("friendId") Long friendId,
                                                   @Param("limit") int limit);

    int markMessagesRead(@Param("userId") Long userId, @Param("friendId") Long friendId);
}

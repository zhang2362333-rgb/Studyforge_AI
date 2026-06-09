package com.studyforge.webapi.user;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.content.service.PostQueryService;
import com.studyforge.content.vo.PostSummaryVO;
import com.studyforge.system.dto.FriendMessageRequest;
import com.studyforge.system.dto.FriendRequestCreateRequest;
import com.studyforge.system.dto.FriendRequestReviewRequest;
import com.studyforge.system.dto.UpdateProfileRequest;
import com.studyforge.system.dto.UpdatePasswordRequest;
import com.studyforge.system.service.AuthService;
import com.studyforge.system.service.UserProfileService;
import com.studyforge.system.vo.FriendMessageVO;
import com.studyforge.system.vo.FriendRequestVO;
import com.studyforge.system.vo.SocialUserVO;
import com.studyforge.system.vo.UserActivityVO;
import com.studyforge.system.vo.UserProfileVO;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final PostQueryService postQueryService;

    public UserController(AuthService authService, UserProfileService userProfileService, PostQueryService postQueryService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
        this.postQueryService = postQueryService;
    }

    @GetMapping("/me/profile")
    public ApiResponse<UserProfileVO> myProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.getProfile(userId, userId));
    }

    @PutMapping("/me/profile")
    public ApiResponse<UserProfileVO> updateMyProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                      @RequestBody UpdateProfileRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    public ApiResponse<UserProfileVO> updatePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @RequestBody UpdatePasswordRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.updatePassword(userId, request));
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileVO> profile(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                              @PathVariable("userId") Long userId) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(userProfileService.getProfile(viewerId, userId));
    }

    @GetMapping("/{userId}/posts")
    public ApiResponse<List<PostSummaryVO>> posts(@PathVariable("userId") Long userId,
                                                  @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                  @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return ApiResponse.success(postQueryService.listByAuthor(userId, languageCode, limit));
    }

    @GetMapping("/{userId}/activities")
    public ApiResponse<List<UserActivityVO>> activities(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                        @PathVariable("userId") Long userId,
                                                        @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                        @RequestParam(name = "limit", defaultValue = "40") int limit) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(userProfileService.listActivities(viewerId, userId, languageCode, limit));
    }

    @PostMapping("/{userId}/follow")
    public ApiResponse<UserProfileVO> follow(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                             @PathVariable("userId") Long userId) {
        Long viewerId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.follow(viewerId, userId));
    }

    @DeleteMapping("/{userId}/follow")
    public ApiResponse<UserProfileVO> unfollow(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                               @PathVariable("userId") Long userId) {
        Long viewerId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.unfollow(viewerId, userId));
    }

    @PostMapping("/{userId}/unfollow")
    public ApiResponse<UserProfileVO> unfollowCompatibility(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                            @PathVariable("userId") Long userId) {
        Long viewerId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.unfollow(viewerId, userId));
    }

    @GetMapping("/{userId}/followers")
    public ApiResponse<List<SocialUserVO>> followers(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                     @PathVariable("userId") Long userId,
                                                     @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(userProfileService.listFollowers(viewerId, userId, limit));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<List<SocialUserVO>> following(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                     @PathVariable("userId") Long userId,
                                                     @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(userProfileService.listFollowing(viewerId, userId, limit));
    }

    @GetMapping("/{userId}/friends")
    public ApiResponse<List<SocialUserVO>> friends(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                   @PathVariable("userId") Long userId,
                                                   @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(userProfileService.listFriends(viewerId, userId, limit));
    }

    @PostMapping("/{userId}/friend-requests")
    public ApiResponse<FriendRequestVO> sendFriendRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                          @PathVariable("userId") Long userId,
                                                          @RequestBody FriendRequestCreateRequest request) {
        Long requesterId = authService.requireUserId(authorization);
        return ApiResponse.success("sent", userProfileService.sendFriendRequest(requesterId, userId, request));
    }

    @GetMapping("/me/friend-requests/incoming")
    public ApiResponse<List<FriendRequestVO>> incomingFriendRequests(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                                     @RequestParam(name = "status", defaultValue = "PENDING") String status,
                                                                     @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.listIncomingFriendRequests(userId, status, limit));
    }

    @GetMapping("/me/friend-requests/outgoing")
    public ApiResponse<List<FriendRequestVO>> outgoingFriendRequests(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                                     @RequestParam(name = "status", defaultValue = "PENDING") String status,
                                                                     @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.listOutgoingFriendRequests(userId, status, limit));
    }

    @PostMapping("/me/friend-requests/{requestId}/review")
    public ApiResponse<FriendRequestVO> reviewFriendRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                            @PathVariable("requestId") Long requestId,
                                                            @RequestBody FriendRequestReviewRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.reviewFriendRequest(userId, requestId, request));
    }

    @GetMapping("/me/friends")
    public ApiResponse<List<SocialUserVO>> myFriends(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @RequestParam(name = "limit", defaultValue = "50") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.listFriends(userId, userId, limit));
    }

    @GetMapping("/me/friends/{friendId}/messages")
    public ApiResponse<List<FriendMessageVO>> messages(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                       @PathVariable("friendId") Long friendId,
                                                       @RequestParam(name = "limit", defaultValue = "50") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(userProfileService.listFriendMessages(userId, friendId, limit));
    }

    @PostMapping("/me/friends/{friendId}/messages")
    public ApiResponse<FriendMessageVO> sendMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @PathVariable("friendId") Long friendId,
                                                    @RequestBody FriendMessageRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("sent", userProfileService.sendFriendMessage(userId, friendId, request));
    }
}

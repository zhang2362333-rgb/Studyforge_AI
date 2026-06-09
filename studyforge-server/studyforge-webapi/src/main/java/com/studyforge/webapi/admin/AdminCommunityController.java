package com.studyforge.webapi.admin;

import com.studyforge.admin.dto.ModerationDecisionRequest;
import com.studyforge.admin.dto.PostFeaturedRequest;
import com.studyforge.admin.dto.PostStatusRequest;
import com.studyforge.admin.dto.UserStatusRequest;
import com.studyforge.admin.service.CommunityAdminService;
import com.studyforge.admin.vo.AdminOverviewVO;
import com.studyforge.admin.vo.AdminPostVO;
import com.studyforge.admin.vo.AdminReportVO;
import com.studyforge.admin.vo.AdminUserDetailVO;
import com.studyforge.admin.vo.AdminUserVO;
import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.common.enums.RoleType;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.entity.User;
import com.studyforge.system.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/community")
public class AdminCommunityController {
    private final CommunityAdminService communityAdminService;
    private final AuthService authService;

    public AdminCommunityController(CommunityAdminService communityAdminService, AuthService authService) {
        this.communityAdminService = communityAdminService;
        this.authService = authService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminOverviewVO> overview(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.overview());
    }

    @GetMapping("/posts")
    public ApiResponse<List<AdminPostVO>> posts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @RequestParam(name = "status", required = false) String status,
                                                @RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "limit", defaultValue = "50") int limit) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.listPosts(status, keyword, limit));
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<AdminPostVO> postDetail(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                               @PathVariable("postId") Long postId) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.postDetail(postId));
    }

    @PostMapping("/posts/{postId}/featured")
    public ApiResponse<AdminPostVO> setFeatured(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @PathVariable("postId") Long postId,
                                                @RequestBody PostFeaturedRequest request) {
        User admin = requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.setFeatured(admin.getUserId(), postId, request));
    }

    @PostMapping("/posts/{postId}/status")
    public ApiResponse<AdminPostVO> updatePostStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @PathVariable("postId") Long postId,
                                                     @RequestBody PostStatusRequest request) {
        User admin = requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.updatePostStatus(admin.getUserId(), postId, request));
    }

    @GetMapping("/reports")
    public ApiResponse<List<AdminReportVO>> reports(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @RequestParam(name = "status", required = false) String status,
                                                    @RequestParam(name = "limit", defaultValue = "50") int limit) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.listReports(status, limit));
    }

    @PostMapping("/reports/{reportId}/review")
    public ApiResponse<AdminReportVO> reviewReport(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                   @PathVariable("reportId") Long reportId,
                                                   @RequestBody ModerationDecisionRequest request) {
        User admin = requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.reviewReport(admin.getUserId(), reportId, request));
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserVO>> users(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @RequestParam(name = "status", required = false) String status,
                                                @RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "limit", defaultValue = "50") int limit) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.listUsers(status, keyword, limit));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<AdminUserDetailVO> userDetail(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @PathVariable("userId") Long userId) {
        requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.userDetail(userId));
    }

    @PostMapping("/users/{userId}/status")
    public ApiResponse<AdminUserVO> updateUserStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @PathVariable("userId") Long userId,
                                                     @RequestBody UserStatusRequest request) {
        User admin = requireAdminUser(authorization);
        return ApiResponse.success(communityAdminService.updateUserStatus(admin.getUserId(), userId, request));
    }

    private User requireAdminUser(String authorization) {
        User admin = authService.requireUser(authorization);
        if (!RoleType.ADMIN.equals(admin.getRole())) {
            throw new BizException(ErrorCode.FORBIDDEN, "admin permission is required");
        }
        return admin;
    }
}

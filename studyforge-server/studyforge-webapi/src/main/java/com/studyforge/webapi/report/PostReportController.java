package com.studyforge.webapi.report;

import com.studyforge.admin.dto.CreateReportRequest;
import com.studyforge.admin.service.CommunityAdminService;
import com.studyforge.admin.vo.ReportSubmissionVO;
import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.system.service.AuthService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/reports")
public class PostReportController {
    private final CommunityAdminService communityAdminService;
    private final AuthService authService;

    public PostReportController(CommunityAdminService communityAdminService, AuthService authService) {
        this.communityAdminService = communityAdminService;
        this.authService = authService;
    }

    @PostMapping
    public ApiResponse<ReportSubmissionVO> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @PathVariable("postId") Long postId,
                                                  @RequestBody CreateReportRequest request) {
        Long reporterId = authService.requireUserId(authorization);
        return ApiResponse.success("reported", communityAdminService.createReport(postId, reporterId, request));
    }
}

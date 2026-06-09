package com.studyforge.admin.service;

import com.studyforge.admin.dto.CreateReportRequest;
import com.studyforge.admin.dto.ModerationDecisionRequest;
import com.studyforge.admin.dto.PostFeaturedRequest;
import com.studyforge.admin.dto.PostStatusRequest;
import com.studyforge.admin.dto.UserStatusRequest;
import com.studyforge.admin.vo.AdminOverviewVO;
import com.studyforge.admin.vo.AdminPostVO;
import com.studyforge.admin.vo.AdminReportVO;
import com.studyforge.admin.vo.AdminUserDetailVO;
import com.studyforge.admin.vo.AdminUserVO;
import com.studyforge.admin.vo.ReportSubmissionVO;
import java.util.List;

public interface CommunityAdminService {
    AdminOverviewVO overview();

    List<AdminPostVO> listPosts(String status, String keyword, int limit);

    AdminPostVO postDetail(Long postId);

    AdminPostVO setFeatured(Long adminId, Long postId, PostFeaturedRequest request);

    AdminPostVO updatePostStatus(Long adminId, Long postId, PostStatusRequest request);

    ReportSubmissionVO createReport(Long postId, Long reporterId, CreateReportRequest request);

    List<AdminReportVO> listReports(String status, int limit);

    AdminReportVO reviewReport(Long adminId, Long reportId, ModerationDecisionRequest request);

    List<AdminUserVO> listUsers(String status, String keyword, int limit);

    AdminUserDetailVO userDetail(Long userId);

    AdminUserVO updateUserStatus(Long adminId, Long userId, UserStatusRequest request);
}

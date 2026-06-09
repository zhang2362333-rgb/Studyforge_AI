package com.studyforge.admin.service.impl;

import com.studyforge.admin.dto.CreateReportRequest;
import com.studyforge.admin.dto.ModerationDecisionRequest;
import com.studyforge.admin.dto.PostFeaturedRequest;
import com.studyforge.admin.dto.PostStatusRequest;
import com.studyforge.admin.dto.UserStatusRequest;
import com.studyforge.admin.mapper.AdminCommunityMapper;
import com.studyforge.admin.service.CommunityAdminService;
import com.studyforge.admin.vo.AdminOverviewVO;
import com.studyforge.admin.vo.AdminPostVO;
import com.studyforge.admin.vo.AdminReportVO;
import com.studyforge.admin.vo.AdminUserDetailVO;
import com.studyforge.admin.vo.AdminUserVO;
import com.studyforge.admin.vo.ReportSubmissionVO;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityAdminServiceImpl implements CommunityAdminService {
    private final AdminCommunityMapper adminCommunityMapper;

    public CommunityAdminServiceImpl(AdminCommunityMapper adminCommunityMapper) {
        this.adminCommunityMapper = adminCommunityMapper;
    }

    @Override
    public AdminOverviewVO overview() {
        Map<String, Object> row = adminCommunityMapper.selectOverview();
        return new AdminOverviewVO(
                intValue(row, "totalUsers"),
                intValue(row, "activeUsers"),
                intValue(row, "totalPosts"),
                intValue(row, "publishedPosts"),
                intValue(row, "archivedPosts"),
                intValue(row, "featuredPosts"),
                intValue(row, "pendingReports"),
                intValue(row, "processedReports")
        );
    }

    @Override
    public List<AdminPostVO> listPosts(String status, String keyword, int limit) {
        return adminCommunityMapper.selectPosts(normalizeStatus(status), emptyToNull(keyword), normalizeLimit(limit))
                .stream()
                .map(this::toPost)
                .toList();
    }

    @Override
    public AdminPostVO postDetail(Long postId) {
        Map<String, Object> row = adminCommunityMapper.selectPostById(postId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "post not found");
        }
        return toPost(row);
    }

    @Override
    @Transactional
    public AdminPostVO setFeatured(Long adminId, Long postId, PostFeaturedRequest request) {
        boolean featured = request != null && request.featured();
        adminCommunityMapper.updatePostFeatured(postId, featured ? 1 : 0);
        adminCommunityMapper.insertAudit(adminId, "post", postId, featured ? "FEATURE_POST" : "UNFEATURE_POST", remark(request == null ? null : request.remark()));
        return postDetail(postId);
    }

    @Override
    @Transactional
    public AdminPostVO updatePostStatus(Long adminId, Long postId, PostStatusRequest request) {
        String status = normalizePostStatus(request == null ? null : request.status());
        adminCommunityMapper.updatePostStatus(postId, status);
        adminCommunityMapper.insertAudit(adminId, "post", postId, "UPDATE_POST_STATUS", status + " / " + remark(request == null ? null : request.remark()));
        return postDetail(postId);
    }

    @Override
    @Transactional
    public ReportSubmissionVO createReport(Long postId, Long reporterId, CreateReportRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "report reason is required");
        }
        if (adminCommunityMapper.selectPostById(postId) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "post not found");
        }

        String reason = limit(request.reason().trim(), 500);
        Map<String, Object> report = new HashMap<>();
        report.put("postId", postId);
        report.put("reporterId", reporterId);
        report.put("reason", reason);
        report.put("aiRiskLevel", estimateRisk(reason));
        report.put("aiSuggestion", moderationSuggestion(reason));
        adminCommunityMapper.insertReport(report);
        return new ReportSubmissionVO(longValue(report.get("reportId")), "PENDING");
    }

    @Override
    public List<AdminReportVO> listReports(String status, int limit) {
        return adminCommunityMapper.selectReports(normalizeStatus(status), normalizeLimit(limit))
                .stream()
                .map(this::toReport)
                .toList();
    }

    @Override
    @Transactional
    public AdminReportVO reviewReport(Long adminId, Long reportId, ModerationDecisionRequest request) {
        Map<String, Object> report = adminCommunityMapper.selectReportById(reportId);
        if (report == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "report not found");
        }

        String decision = request == null || request.decision() == null ? "" : request.decision().trim().toUpperCase(Locale.ROOT);
        Long postId = longValue(report.get("postId"));
        String actionType;
        String nextReportStatus;

        if ("TAKE_DOWN".equals(decision)) {
            adminCommunityMapper.updatePostStatus(postId, "ARCHIVED");
            nextReportStatus = "ACCEPTED";
            actionType = "ACCEPT_REPORT_TAKE_DOWN";
        } else if ("RESTORE".equals(decision)) {
            adminCommunityMapper.updatePostStatus(postId, "PUBLISHED");
            nextReportStatus = "ACCEPTED";
            actionType = "ACCEPT_REPORT_RESTORE";
        } else if ("DISMISS".equals(decision)) {
            nextReportStatus = "REJECTED";
            actionType = "REJECT_REPORT";
        } else {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "unsupported decision");
        }

        adminCommunityMapper.updateReport(reportId, nextReportStatus, adminId);
        adminCommunityMapper.insertAudit(adminId, "report", reportId, actionType, remark(request == null ? null : request.remark()));
        return toReport(adminCommunityMapper.selectReportById(reportId));
    }

    @Override
    public List<AdminUserVO> listUsers(String status, String keyword, int limit) {
        return adminCommunityMapper.selectUsers(normalizeStatus(status), emptyToNull(keyword), normalizeLimit(limit))
                .stream()
                .map(this::toUser)
                .toList();
    }

    @Override
    public AdminUserDetailVO userDetail(Long userId) {
        Map<String, Object> row = adminCommunityMapper.selectUserDetailById(userId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "user not found");
        }
        return toUserDetail(row);
    }

    @Override
    @Transactional
    public AdminUserVO updateUserStatus(Long adminId, Long userId, UserStatusRequest request) {
        String status = normalizeUserStatus(request == null ? null : request.status());
        adminCommunityMapper.updateUserStatus(userId, status);
        adminCommunityMapper.insertAudit(adminId, "user", userId, "UPDATE_USER_STATUS", status + " / " + remark(request == null ? null : request.remark()));
        Map<String, Object> row = adminCommunityMapper.selectUserById(userId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "user not found");
        }
        return toUser(row);
    }

    private AdminPostVO toPost(Map<String, Object> row) {
        return new AdminPostVO(
                longValue(row.get("postId")),
                longValue(row.get("authorId")),
                stringValue(row.get("authorName")),
                stringValue(row.get("title")),
                stringValue(row.get("summary")),
                stringValue(row.get("content")),
                stringValue(row.get("languageCode")),
                stringValue(row.get("categoryCode")),
                stringValue(row.get("status")),
                intValue(row, "featured") == 1,
                stringValue(row.get("coverImageUrl")),
                intValue(row, "likeCount"),
                intValue(row, "favoriteCount"),
                intValue(row, "commentCount"),
                intValue(row, "viewCount"),
                doubleValue(row.get("hotScore")),
                timeValue(row.get("createdTime")),
                timeValue(row.get("updatedTime"))
        );
    }

    private AdminReportVO toReport(Map<String, Object> row) {
        return new AdminReportVO(
                longValue(row.get("reportId")),
                longValue(row.get("postId")),
                stringValue(row.get("postTitle")),
                stringValue(row.get("postStatus")),
                longValue(row.get("reporterId")),
                stringValue(row.get("reporterName")),
                stringValue(row.get("reason")),
                stringValue(row.get("status")),
                stringValue(row.get("aiRiskLevel")),
                stringValue(row.get("aiSuggestion")),
                longValue(row.get("processedBy")),
                stringValue(row.get("processedByName")),
                timeValue(row.get("processedTime")),
                timeValue(row.get("createdTime"))
        );
    }

    private AdminUserVO toUser(Map<String, Object> row) {
        return new AdminUserVO(
                longValue(row.get("userId")),
                stringValue(row.get("username")),
                stringValue(row.get("displayName")),
                stringValue(row.get("email")),
                stringValue(row.get("role")),
                stringValue(row.get("status")),
                intValue(row, "communityLevel"),
                intValue(row, "experiencePoints"),
                intValue(row, "reputationScore"),
                intValue(row, "postCount"),
                intValue(row, "commentCount"),
                intValue(row, "favoriteCount"),
                intValue(row, "followerCount"),
                timeValue(row.get("createdTime"))
        );
    }

    private AdminUserDetailVO toUserDetail(Map<String, Object> row) {
        return new AdminUserDetailVO(
                longValue(row.get("userId")),
                stringValue(row.get("username")),
                stringValue(row.get("displayName")),
                stringValue(row.get("email")),
                stringValue(row.get("role")),
                stringValue(row.get("status")),
                stringValue(row.get("bio")),
                stringValue(row.get("avatarUrl")),
                stringValue(row.get("bannerUrl")),
                intValue(row, "communityLevel"),
                intValue(row, "experiencePoints"),
                dateValue(row.get("lastLoginRewardDate")),
                intValue(row, "reputationScore"),
                intValue(row, "postCount"),
                intValue(row, "publishedPostCount"),
                intValue(row, "archivedPostCount"),
                intValue(row, "commentCount"),
                intValue(row, "likeCount"),
                intValue(row, "favoriteCount"),
                intValue(row, "collectionCount"),
                intValue(row, "historyCount"),
                intValue(row, "followerCount"),
                intValue(row, "followingCount"),
                intValue(row, "friendCount"),
                intValue(row, "incomingFriendRequestCount"),
                intValue(row, "outgoingFriendRequestCount"),
                intValue(row, "sentMessageCount"),
                intValue(row, "receivedMessageCount"),
                intValue(row, "helpRequestCount"),
                intValue(row, "helpAnswerCount"),
                intValue(row, "acceptedAnswerCount"),
                intValue(row, "reportCount"),
                intValue(row, "reportedPostCount"),
                intValue(row, "uploadCount"),
                intValue(row, "aiCallCount"),
                intValue(row, "aiSuccessCount"),
                intValue(row, "voiceRecordCount"),
                intValue(row, "activeTokenCount"),
                intValue(row, "experienceLogCount"),
                timeValue(row.get("lastPostTime")),
                timeValue(row.get("lastCommentTime")),
                timeValue(row.get("lastHelpTime")),
                timeValue(row.get("lastAiCallTime")),
                timeValue(row.get("lastVoiceTime")),
                timeValue(row.get("createdTime")),
                timeValue(row.get("updatedTime"))
        );
    }

    private String normalizePostStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if ("PUBLISHED".equals(value) || "ARCHIVED".equals(value) || "REPORTED".equals(value) || "DRAFT".equals(value)) {
            return value;
        }
        throw new BizException(ErrorCode.VALIDATION_ERROR, "unsupported post status");
    }

    private String normalizeUserStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if ("ACTIVE".equals(value) || "DISABLED".equals(value) || "LOCKED".equals(value)) {
            return value;
        }
        throw new BizException(ErrorCode.VALIDATION_ERROR, "unsupported user status");
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
    }

    private String estimateRisk(String reason) {
        String lower = reason.toLowerCase(Locale.ROOT);
        if (lower.contains("违法") || lower.contains("攻击") || lower.contains("诈骗") || lower.contains("spam") || lower.contains("abuse")) {
            return "HIGH";
        }
        if (lower.contains("不实") || lower.contains("广告") || lower.contains("引战") || lower.contains("off-topic")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String moderationSuggestion(String reason) {
        String risk = estimateRisk(reason);
        if ("HIGH".equals(risk)) {
            return "建议优先审核。若举报属实，应下架内容并记录处理原因。";
        }
        if ("MEDIUM".equals(risk)) {
            return "建议结合正文和评论判断。可先保留内容，必要时要求作者修改。";
        }
        return "建议人工复核举报理由。若内容未违反规则，可驳回举报。";
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? 30 : Math.min(limit, 100);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String remark(String value) {
        return value == null || value.isBlank() ? "" : limit(value.trim(), 500);
    }

    private String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private int intValue(Map<String, Object> row, String key) {
        return intValue(row.get(key));
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        if (value instanceof String text && ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text))) {
            return Boolean.parseBoolean(text) ? 1 : 0;
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

    private double doubleValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0.0 : Double.parseDouble(value.toString());
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

    private LocalDate dateValue(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return value == null ? null : LocalDate.parse(value.toString());
    }
}

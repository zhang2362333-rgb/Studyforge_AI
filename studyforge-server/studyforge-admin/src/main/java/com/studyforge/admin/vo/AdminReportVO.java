package com.studyforge.admin.vo;

import java.time.LocalDateTime;

public record AdminReportVO(Long reportId,
                            Long postId,
                            String postTitle,
                            String postStatus,
                            Long reporterId,
                            String reporterName,
                            String reason,
                            String status,
                            String aiRiskLevel,
                            String aiSuggestion,
                            Long processedBy,
                            String processedByName,
                            LocalDateTime processedTime,
                            LocalDateTime createdTime) {
}

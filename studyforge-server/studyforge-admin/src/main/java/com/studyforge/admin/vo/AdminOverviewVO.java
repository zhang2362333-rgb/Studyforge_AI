package com.studyforge.admin.vo;

public record AdminOverviewVO(int totalUsers,
                              int activeUsers,
                              int totalPosts,
                              int publishedPosts,
                              int archivedPosts,
                              int featuredPosts,
                              int pendingReports,
                              int processedReports) {
}

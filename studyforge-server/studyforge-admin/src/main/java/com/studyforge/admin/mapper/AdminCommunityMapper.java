package com.studyforge.admin.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AdminCommunityMapper {
    Map<String, Object> selectOverview();

    List<Map<String, Object>> selectPosts(@Param("status") String status,
                                          @Param("keyword") String keyword,
                                          @Param("limit") int limit);

    Map<String, Object> selectPostById(@Param("postId") Long postId);

    int updatePostFeatured(@Param("postId") Long postId, @Param("featured") int featured);

    int updatePostStatus(@Param("postId") Long postId, @Param("status") String status);

    int insertReport(Map<String, Object> report);

    Map<String, Object> selectReportById(@Param("reportId") Long reportId);

    List<Map<String, Object>> selectReports(@Param("status") String status, @Param("limit") int limit);

    int updateReport(@Param("reportId") Long reportId,
                     @Param("status") String status,
                     @Param("processedBy") Long processedBy);

    List<Map<String, Object>> selectUsers(@Param("status") String status,
                                          @Param("keyword") String keyword,
                                          @Param("limit") int limit);

    Map<String, Object> selectUserById(@Param("userId") Long userId);

    Map<String, Object> selectUserDetailById(@Param("userId") Long userId);

    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    int insertAudit(@Param("adminId") Long adminId,
                    @Param("targetType") String targetType,
                    @Param("targetId") Long targetId,
                    @Param("actionType") String actionType,
                    @Param("remark") String remark);
}

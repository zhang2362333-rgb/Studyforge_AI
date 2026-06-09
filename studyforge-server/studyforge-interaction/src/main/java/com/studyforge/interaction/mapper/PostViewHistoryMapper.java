package com.studyforge.interaction.mapper;

import org.apache.ibatis.annotations.Param;

public interface PostViewHistoryMapper {
    int insert(@Param("postId") Long postId, @Param("userId") Long userId);
}

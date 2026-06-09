package com.studyforge.interaction.mapper;

import org.apache.ibatis.annotations.Param;

public interface PostLikeMapper {
    int countByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    int insertIgnore(@Param("postId") Long postId, @Param("userId") Long userId);

    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}

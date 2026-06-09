package com.studyforge.content.mapper;

import com.studyforge.content.entity.PostI18n;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PostI18nMapper {
    List<PostI18n> selectByPostId(@Param("postId") Long postId);

    PostI18n selectByPostIdAndLanguage(@Param("postId") Long postId, @Param("languageCode") String languageCode);

    int insert(PostI18n postI18n);

    int updateById(PostI18n postI18n);
}

package com.studyforge.interaction.mapper;

import com.studyforge.interaction.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CommentMapper {
    Comment selectById(@Param("commentId") Long commentId);

    Comment selectByIdForViewer(@Param("commentId") Long commentId, @Param("viewerId") Long viewerId);

    List<Comment> selectByPostIdForViewer(@Param("postId") Long postId, @Param("viewerId") Long viewerId);

    int nextFloorNo(@Param("postId") Long postId);

    int insert(Comment comment);

    int markDeleted(@Param("commentId") Long commentId);

    int incrementLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    int countLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int insertLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);
}

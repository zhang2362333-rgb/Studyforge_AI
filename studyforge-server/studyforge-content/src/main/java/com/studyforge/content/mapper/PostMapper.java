package com.studyforge.content.mapper;

import com.studyforge.content.entity.Post;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PostMapper {
    Post selectById(@Param("postId") Long postId);

    String selectOriginalTitle(@Param("postId") Long postId);

    List<Post> selectPublishedByHotScore(@Param("limit") int limit);

    List<Post> selectPublished(@Param("categoryCode") String categoryCode,
                               @Param("keyword") String keyword,
                               @Param("limit") int limit);

    List<Post> selectPublishedByAuthor(@Param("authorId") Long authorId, @Param("limit") int limit);

    List<Post> selectFavoritesByUser(@Param("userId") Long userId, @Param("limit") int limit);

    List<Post> selectFavoriteCollectionByUser(@Param("userId") Long userId,
                                              @Param("collectionId") Long collectionId,
                                              @Param("limit") int limit);

    List<Post> selectHistoryByUser(@Param("userId") Long userId, @Param("limit") int limit);

    int insert(Post post);

    int updateById(Post post);

    int incrementLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    int incrementFavoriteCount(@Param("postId") Long postId, @Param("delta") int delta);

    int incrementCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    int incrementViewCount(@Param("postId") Long postId);

    int deleteById(@Param("postId") Long postId);
}

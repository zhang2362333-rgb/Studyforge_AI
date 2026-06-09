package com.studyforge.interaction.mapper;

import com.studyforge.interaction.entity.FavoriteCollection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FavoriteCollectionMapper {
    FavoriteCollection selectById(@Param("collectionId") Long collectionId);

    FavoriteCollection selectDefaultByUser(@Param("userId") Long userId);

    List<FavoriteCollection> selectByUser(@Param("userId") Long userId);

    int insert(FavoriteCollection collection);

    int insertIgnoreDefault(@Param("userId") Long userId);

    int insertIgnoreItem(@Param("collectionId") Long collectionId,
                         @Param("postId") Long postId,
                         @Param("userId") Long userId);

    int deleteItem(@Param("collectionId") Long collectionId,
                   @Param("postId") Long postId,
                   @Param("userId") Long userId);

    int deleteItemsByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}

package com.studyforge.interaction.service;

import com.studyforge.interaction.dto.CreateFavoriteCollectionRequest;
import com.studyforge.interaction.vo.FavoriteCollectionVO;
import java.util.List;

public interface FavoriteCollectionService {
    List<FavoriteCollectionVO> listMine(Long userId);

    FavoriteCollectionVO create(Long userId, CreateFavoriteCollectionRequest request);

    FavoriteCollectionVO addPost(Long userId, Long collectionId, Long postId);

    FavoriteCollectionVO removePost(Long userId, Long collectionId, Long postId);

    void requireOwner(Long userId, Long collectionId);
}

package com.studyforge.interaction.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.content.entity.Post;
import com.studyforge.content.mapper.PostMapper;
import com.studyforge.interaction.dto.CreateFavoriteCollectionRequest;
import com.studyforge.interaction.entity.FavoriteCollection;
import com.studyforge.interaction.mapper.FavoriteCollectionMapper;
import com.studyforge.interaction.mapper.PostFavoriteMapper;
import com.studyforge.interaction.service.FavoriteCollectionService;
import com.studyforge.interaction.vo.FavoriteCollectionVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteCollectionServiceImpl implements FavoriteCollectionService {
    private final FavoriteCollectionMapper favoriteCollectionMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostMapper postMapper;

    public FavoriteCollectionServiceImpl(FavoriteCollectionMapper favoriteCollectionMapper,
                                         PostFavoriteMapper postFavoriteMapper,
                                         PostMapper postMapper) {
        this.favoriteCollectionMapper = favoriteCollectionMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional
    public List<FavoriteCollectionVO> listMine(Long userId) {
        ensureDefaultCollection(userId);
        return favoriteCollectionMapper.selectByUser(userId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteCollectionVO create(Long userId, CreateFavoriteCollectionRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "collection name is required");
        }

        FavoriteCollection collection = new FavoriteCollection();
        collection.setUserId(userId);
        collection.setName(limit(request.name().trim(), 80));
        collection.setDescription(limit(emptyToBlank(request.description()), 300));
        collection.setVisibility("PUBLIC".equalsIgnoreCase(request.visibility()) ? "PUBLIC" : "PRIVATE");
        collection.setSortNo(100);
        favoriteCollectionMapper.insert(collection);
        return toVO(favoriteCollectionMapper.selectById(collection.getCollectionId()));
    }

    @Override
    @Transactional
    public FavoriteCollectionVO addPost(Long userId, Long collectionId, Long postId) {
        FavoriteCollection collection = requireOwnedCollection(userId, collectionId);
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "post not found");
        }

        if (postFavoriteMapper.countByPostAndUser(postId, userId) == 0 && postFavoriteMapper.insertIgnore(postId, userId) > 0) {
            postMapper.incrementFavoriteCount(postId, 1);
        }
        favoriteCollectionMapper.insertIgnoreItem(collection.getCollectionId(), postId, userId);
        return toVO(favoriteCollectionMapper.selectById(collectionId));
    }

    @Override
    @Transactional
    public FavoriteCollectionVO removePost(Long userId, Long collectionId, Long postId) {
        FavoriteCollection collection = requireOwnedCollection(userId, collectionId);
        favoriteCollectionMapper.deleteItem(collection.getCollectionId(), postId, userId);
        return toVO(favoriteCollectionMapper.selectById(collectionId));
    }

    @Override
    public void requireOwner(Long userId, Long collectionId) {
        requireOwnedCollection(userId, collectionId);
    }

    private void ensureDefaultCollection(Long userId) {
        favoriteCollectionMapper.insertIgnoreDefault(userId);
    }

    private FavoriteCollection requireOwnedCollection(Long userId, Long collectionId) {
        if (collectionId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "collectionId is required");
        }
        FavoriteCollection collection = favoriteCollectionMapper.selectById(collectionId);
        if (collection == null || !userId.equals(collection.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "collection not found");
        }
        return collection;
    }

    private FavoriteCollectionVO toVO(FavoriteCollection collection) {
        return new FavoriteCollectionVO(
                collection.getCollectionId(),
                collection.getUserId(),
                collection.getName(),
                emptyToBlank(collection.getDescription()),
                collection.getVisibility(),
                collection.getItemCount() == null ? 0 : collection.getItemCount(),
                collection.getCreatedTime()
        );
    }

    private String emptyToBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

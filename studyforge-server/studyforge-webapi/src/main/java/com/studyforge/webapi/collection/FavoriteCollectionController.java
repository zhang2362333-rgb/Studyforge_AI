package com.studyforge.webapi.collection;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.content.service.PostQueryService;
import com.studyforge.content.vo.PostSummaryVO;
import com.studyforge.interaction.dto.CreateFavoriteCollectionRequest;
import com.studyforge.interaction.service.FavoriteCollectionService;
import com.studyforge.interaction.vo.FavoriteCollectionVO;
import com.studyforge.system.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/collections")
public class FavoriteCollectionController {
    private final AuthService authService;
    private final FavoriteCollectionService favoriteCollectionService;
    private final PostQueryService postQueryService;

    public FavoriteCollectionController(AuthService authService,
                                        FavoriteCollectionService favoriteCollectionService,
                                        PostQueryService postQueryService) {
        this.authService = authService;
        this.favoriteCollectionService = favoriteCollectionService;
        this.postQueryService = postQueryService;
    }

    @GetMapping("/me")
    public ApiResponse<List<FavoriteCollectionVO>> mine(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(favoriteCollectionService.listMine(userId));
    }

    @PostMapping
    public ApiResponse<FavoriteCollectionVO> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @RequestBody CreateFavoriteCollectionRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("created", favoriteCollectionService.create(userId, request));
    }

    @GetMapping("/{collectionId}/posts")
    public ApiResponse<List<PostSummaryVO>> posts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @PathVariable("collectionId") Long collectionId,
                                                  @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                  @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long userId = authService.requireUserId(authorization);
        favoriteCollectionService.requireOwner(userId, collectionId);
        return ApiResponse.success(postQueryService.listFavoriteCollection(userId, collectionId, languageCode, limit));
    }

    @PostMapping("/{collectionId}/posts/{postId}")
    public ApiResponse<FavoriteCollectionVO> addPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @PathVariable("collectionId") Long collectionId,
                                                     @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(favoriteCollectionService.addPost(userId, collectionId, postId));
    }

    @DeleteMapping("/{collectionId}/posts/{postId}")
    public ApiResponse<FavoriteCollectionVO> removePost(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                        @PathVariable("collectionId") Long collectionId,
                                                        @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(favoriteCollectionService.removePost(userId, collectionId, postId));
    }
}

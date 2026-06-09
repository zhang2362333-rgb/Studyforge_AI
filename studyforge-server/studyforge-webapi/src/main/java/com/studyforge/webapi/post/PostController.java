package com.studyforge.webapi.post;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.content.dto.CreatePostRequest;
import com.studyforge.content.service.PostCommandService;
import com.studyforge.content.service.PostQueryService;
import com.studyforge.content.vo.PostDetailVO;
import com.studyforge.content.vo.PostSummaryVO;
import com.studyforge.system.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final AuthService authService;

    public PostController(PostQueryService postQueryService, PostCommandService postCommandService, AuthService authService) {
        this.postQueryService = postQueryService;
        this.postCommandService = postCommandService;
        this.authService = authService;
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailVO> detail(@PathVariable("postId") Long postId,
                                            @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode) {
        return ApiResponse.success(postQueryService.getDetail(postId, languageCode));
    }

    @GetMapping
    public ApiResponse<List<PostSummaryVO>> list(@RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                 @RequestParam(name = "categoryCode", required = false) String categoryCode,
                                                 @RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return ApiResponse.success(postQueryService.list(languageCode, categoryCode, keyword, limit));
    }

    @GetMapping("/trending")
    public ApiResponse<List<PostSummaryVO>> trending(@RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                     @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ApiResponse.success(postQueryService.getTrending(languageCode, limit));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @RequestBody CreatePostRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("created", postCommandService.create(userId, request));
    }

    @PutMapping("/{postId}")
    public ApiResponse<Long> update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @PathVariable("postId") Long postId,
                                    @RequestBody CreatePostRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("updated", postCommandService.update(userId, postId, request));
    }

    @GetMapping("/me/favorites")
    public ApiResponse<List<PostSummaryVO>> favorites(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                      @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                      @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(postQueryService.listFavorites(userId, languageCode, limit));
    }

    @GetMapping("/me/history")
    public ApiResponse<List<PostSummaryVO>> history(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @RequestParam(name = "languageCode", defaultValue = "zh_CN") String languageCode,
                                                    @RequestParam(name = "limit", defaultValue = "30") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(postQueryService.listHistory(userId, languageCode, limit));
    }
}

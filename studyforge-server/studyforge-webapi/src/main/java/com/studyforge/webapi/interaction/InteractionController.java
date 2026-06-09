package com.studyforge.webapi.interaction;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.interaction.dto.CreateCommentRequest;
import com.studyforge.interaction.service.InteractionCommandService;
import com.studyforge.interaction.vo.CommentVO;
import com.studyforge.interaction.vo.PostInteractionStateVO;
import com.studyforge.system.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}")
public class InteractionController {
    private final InteractionCommandService interactionCommandService;
    private final AuthService authService;

    public InteractionController(InteractionCommandService interactionCommandService, AuthService authService) {
        this.interactionCommandService = interactionCommandService;
        this.authService = authService;
    }

    @GetMapping("/interaction")
    public ApiResponse<PostInteractionStateVO> state(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(interactionCommandService.state(postId, userId));
    }

    @PostMapping("/likes")
    public ApiResponse<PostInteractionStateVO> like(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(interactionCommandService.like(postId, userId));
    }

    @PostMapping("/favorites")
    public ApiResponse<PostInteractionStateVO> favorite(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                        @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(interactionCommandService.favorite(postId, userId));
    }

    @PostMapping("/views")
    public ApiResponse<Void> view(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @PathVariable("postId") Long postId) {
        Long userId = authService.requireUserId(authorization);
        interactionCommandService.recordView(postId, userId);
        return ApiResponse.success(null);
    }

    @GetMapping("/comments")
    public ApiResponse<List<CommentVO>> comments(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                 @PathVariable("postId") Long postId) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(interactionCommandService.comments(postId, viewerId));
    }

    @PostMapping("/comments")
    public ApiResponse<CommentVO> comment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                          @PathVariable("postId") Long postId,
                                          @RequestBody CreateCommentRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("commented", interactionCommandService.comment(postId, userId, request));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ApiResponse<CommentVO> likeComment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                              @PathVariable("postId") Long postId,
                                              @PathVariable("commentId") Long commentId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(interactionCommandService.likeComment(postId, commentId, userId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                           @PathVariable("postId") Long postId,
                                           @PathVariable("commentId") Long commentId) {
        Long userId = authService.requireUserId(authorization);
        interactionCommandService.deleteComment(postId, commentId, userId);
        return ApiResponse.success(null);
    }
}

package com.studyforge.interaction.service;

import com.studyforge.interaction.dto.CreateCommentRequest;
import com.studyforge.interaction.vo.CommentVO;
import com.studyforge.interaction.vo.PostInteractionStateVO;
import java.util.List;

public interface InteractionCommandService {
    PostInteractionStateVO like(Long postId, Long userId);

    PostInteractionStateVO favorite(Long postId, Long userId);

    PostInteractionStateVO state(Long postId, Long userId);

    void recordView(Long postId, Long userId);

    CommentVO comment(Long postId, Long userId, CreateCommentRequest request);

    List<CommentVO> comments(Long postId, Long viewerId);

    CommentVO likeComment(Long postId, Long commentId, Long userId);

    void deleteComment(Long postId, Long commentId, Long userId);
}

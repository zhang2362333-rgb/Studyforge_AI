import { http, unwrap } from '@/api/http';
import type { CommentItem, PostInteractionState, ReportSubmission } from '@/types/api';

export function getInteractionState(postId: number | string) {
  return unwrap<PostInteractionState>(http.get(`/posts/${postId}/interaction`));
}

export function toggleLike(postId: number | string) {
  return unwrap<PostInteractionState>(http.post(`/posts/${postId}/likes`));
}

export function toggleFavorite(postId: number | string) {
  return unwrap<PostInteractionState>(http.post(`/posts/${postId}/favorites`));
}

export function recordPostView(postId: number | string) {
  return unwrap<void>(http.post(`/posts/${postId}/views`));
}

export function getComments(postId: number | string) {
  return unwrap<CommentItem[]>(http.get(`/posts/${postId}/comments`));
}

export function createComment(postId: number | string, content: string, languageCode: string, parentCommentId?: number | null) {
  return unwrap<CommentItem>(
    http.post(`/posts/${postId}/comments`, {
      content,
      languageCode,
      parentCommentId
    })
  );
}

export function toggleCommentLike(postId: number | string, commentId: number | string) {
  return unwrap<CommentItem>(http.post(`/posts/${postId}/comments/${commentId}/likes`));
}

export async function deleteComment(postId: number | string, commentId: number | string) {
  await unwrap<void>(http.delete(`/posts/${postId}/comments/${commentId}`));
  return true;
}

export function reportPost(postId: number | string, reason: string) {
  return unwrap<ReportSubmission>(
    http.post(`/posts/${postId}/reports`, {
      reason
    })
  );
}

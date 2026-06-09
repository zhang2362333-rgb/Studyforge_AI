import { http, unwrap } from '@/api/http';
import type { PostDetail, PostSummary } from '@/types/api';

export interface TrendingQuery {
  languageCode: string;
  limit?: number;
}

export function getTrendingPosts(query: TrendingQuery) {
  return unwrap<PostSummary[]>(
    http.get('/posts/trending', {
      params: {
        languageCode: query.languageCode,
        limit: query.limit ?? 10
      }
    })
  );
}

export function getPostDetail(postId: number | string, languageCode: string) {
  return unwrap<PostDetail>(
    http.get(`/posts/${postId}`, {
      params: {
        languageCode
      }
    })
  );
}

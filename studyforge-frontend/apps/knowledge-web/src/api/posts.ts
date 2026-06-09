import { http, unwrap } from '@/api/http';
import type { CreatePostRequest, PostDetail, PostSummary } from '@/types/api';

export interface TrendingQuery {
  languageCode: string;
  limit?: number;
}

export interface ListPostQuery extends TrendingQuery {
  categoryCode?: string;
  keyword?: string;
}

export function getTrendingPosts(query: TrendingQuery) {
  return unwrap<PostSummary[]>(
    http.get('/posts/trending', {
      params: {
        languageCode: query.languageCode,
        limit: query.limit ?? 12
      }
    })
  );
}

export function getPosts(query: ListPostQuery) {
  return unwrap<PostSummary[]>(
    http.get('/posts', {
      params: {
        languageCode: query.languageCode,
        categoryCode: query.categoryCode,
        keyword: query.keyword,
        limit: query.limit ?? 30
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

export function createPost(payload: CreatePostRequest) {
  return unwrap<number>(http.post('/posts', payload));
}

export function updatePost(postId: number | string, payload: CreatePostRequest) {
  return unwrap<number>(http.put(`/posts/${postId}`, payload));
}

export function getFavoritePosts(languageCode: string) {
  return unwrap<PostSummary[]>(
    http.get('/posts/me/favorites', {
      params: {
        languageCode,
        limit: 30
      }
    })
  );
}

export function getHistoryPosts(languageCode: string) {
  return unwrap<PostSummary[]>(
    http.get('/posts/me/history', {
      params: {
        languageCode,
        limit: 30
      }
    })
  );
}

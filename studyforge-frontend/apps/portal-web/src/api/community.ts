import { http, unwrap } from '@/api/http';
import type { AdminOverview, AdminPost, AdminReport, AdminUser, AdminUserDetail } from '@/types/api';

export interface AdminListQuery {
  status?: string;
  keyword?: string;
  limit?: number;
}

export function getAdminOverview() {
  return unwrap<AdminOverview>(http.get('/admin/community/overview'));
}

export function getAdminPosts(query: AdminListQuery = {}) {
  return unwrap<AdminPost[]>(
    http.get('/admin/community/posts', {
      params: {
        status: query.status,
        keyword: query.keyword,
        limit: query.limit ?? 50
      }
    })
  );
}

export function getAdminPostDetail(postId: number | string) {
  return unwrap<AdminPost>(http.get(`/admin/community/posts/${postId}`));
}

export function updatePostFeatured(postId: number | string, featured: boolean, remark = '') {
  return unwrap<AdminPost>(
    http.post(`/admin/community/posts/${postId}/featured`, {
      featured,
      remark
    })
  );
}

export function updatePostStatus(postId: number | string, status: string, remark = '') {
  return unwrap<AdminPost>(
    http.post(`/admin/community/posts/${postId}/status`, {
      status,
      remark
    })
  );
}

export function getReports(query: AdminListQuery = {}) {
  return unwrap<AdminReport[]>(
    http.get('/admin/community/reports', {
      params: {
        status: query.status,
        limit: query.limit ?? 50
      }
    })
  );
}

export function reviewReport(reportId: number | string, decision: string, remark = '') {
  return unwrap<AdminReport>(
    http.post(`/admin/community/reports/${reportId}/review`, {
      decision,
      remark
    })
  );
}

export function getAdminUsers(query: AdminListQuery = {}) {
  return unwrap<AdminUser[]>(
    http.get('/admin/community/users', {
      params: {
        status: query.status,
        keyword: query.keyword,
        limit: query.limit ?? 50
      }
    })
  );
}

export function getAdminUserDetail(userId: number | string) {
  return unwrap<AdminUserDetail>(http.get(`/admin/community/users/${userId}`));
}

export function updateUserStatus(userId: number | string, status: string, remark = '') {
  return unwrap<AdminUser>(
    http.post(`/admin/community/users/${userId}/status`, {
      status,
      remark
    })
  );
}

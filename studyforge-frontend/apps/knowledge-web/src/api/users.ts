import { http, unwrap } from '@/api/http';
import type { FriendMessage, FriendRequest, PostSummary, SocialUser, UserActivity, UserProfile } from '@/types/api';

export interface UpdateProfilePayload {
  username: string;
  email: string;
  displayName: string;
  bio: string;
  avatarUrl?: string;
  bannerUrl?: string;
}

export interface UpdatePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export function getMyProfile() {
  return unwrap<UserProfile>(http.get('/users/me/profile'));
}

export function updateMyProfile(payload: UpdateProfilePayload) {
  return unwrap<UserProfile>(http.put('/users/me/profile', payload));
}

export function updateMyPassword(payload: UpdatePasswordPayload) {
  return unwrap<UserProfile>(http.put('/users/me/password', payload));
}

export function getUserProfile(userId: number | string) {
  return unwrap<UserProfile>(http.get(`/users/${userId}/profile`));
}

export function getUserPosts(userId: number | string, languageCode: string) {
  return unwrap<PostSummary[]>(
    http.get(`/users/${userId}/posts`, {
      params: {
        languageCode,
        limit: 30
      }
    })
  );
}

export function getUserActivities(userId: number | string, languageCode: string) {
  return unwrap<UserActivity[]>(
    http.get(`/users/${userId}/activities`, {
      params: {
        languageCode,
        limit: 40
      }
    })
  );
}

export function followUser(userId: number | string) {
  return unwrap<UserProfile>(http.post(`/users/${userId}/follow`));
}

export function unfollowUser(userId: number | string) {
  return unwrap<UserProfile>(http.delete(`/users/${userId}/follow`));
}

export function getFollowers(userId: number | string) {
  return unwrap<SocialUser[]>(http.get(`/users/${userId}/followers`, { params: { limit: 30 } }));
}

export function getFollowing(userId: number | string) {
  return unwrap<SocialUser[]>(http.get(`/users/${userId}/following`, { params: { limit: 30 } }));
}

export function getFriends(userId: number | string) {
  return unwrap<SocialUser[]>(http.get(`/users/${userId}/friends`, { params: { limit: 50 } }));
}

export function getMyFriends() {
  return unwrap<SocialUser[]>(http.get('/users/me/friends', { params: { limit: 50 } }));
}

export function sendFriendRequest(userId: number | string, message: string) {
  return unwrap<FriendRequest>(http.post(`/users/${userId}/friend-requests`, { message }));
}

export function getIncomingFriendRequests(status = 'PENDING') {
  return unwrap<FriendRequest[]>(http.get('/users/me/friend-requests/incoming', { params: { status, limit: 50 } }));
}

export function getOutgoingFriendRequests(status = 'PENDING') {
  return unwrap<FriendRequest[]>(http.get('/users/me/friend-requests/outgoing', { params: { status, limit: 50 } }));
}

export function reviewFriendRequest(requestId: number | string, decision: 'ACCEPT' | 'REJECT') {
  return unwrap<FriendRequest>(http.post(`/users/me/friend-requests/${requestId}/review`, { decision }));
}

export function getFriendMessages(friendId: number | string) {
  return unwrap<FriendMessage[]>(http.get(`/users/me/friends/${friendId}/messages`, { params: { limit: 80 } }));
}

export function sendFriendMessage(friendId: number | string, content: string) {
  return unwrap<FriendMessage>(http.post(`/users/me/friends/${friendId}/messages`, { content }));
}

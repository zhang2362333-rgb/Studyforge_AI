export interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
  requestId?: string | null;
}

export interface HealthStatus {
  service: string;
  status: string;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface LoginSession {
  accessToken: string;
  userId: number;
  username: string;
  role: 'USER' | 'ADMIN' | string;
}

export interface PostSummary {
  postId: number;
  title: string;
  summary: string;
  languageCode: string;
  categoryCode: string;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  viewCount: number;
  hotScore: number;
}

export interface PostDetail extends PostSummary {
  authorId: number;
  content: string;
}

export interface AdminOverview {
  totalUsers: number;
  activeUsers: number;
  totalPosts: number;
  publishedPosts: number;
  archivedPosts: number;
  featuredPosts: number;
  pendingReports: number;
  processedReports: number;
}

export interface AdminPost {
  postId: number;
  authorId: number;
  authorName: string;
  title: string;
  summary: string;
  content: string;
  languageCode: string;
  categoryCode: string;
  status: 'PUBLISHED' | 'ARCHIVED' | 'REPORTED' | 'DRAFT' | string;
  featured: boolean;
  coverImageUrl: string | null;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  viewCount: number;
  hotScore: number;
  createdTime: string | null;
  updatedTime: string | null;
}

export interface AdminReport {
  reportId: number;
  postId: number;
  postTitle: string;
  postStatus: string;
  reporterId: number;
  reporterName: string;
  reason: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | string;
  aiRiskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | string;
  aiSuggestion: string;
  processedBy: number | null;
  processedByName: string;
  processedTime: string | null;
  createdTime: string | null;
}

export interface AdminUser {
  userId: number;
  username: string;
  displayName: string;
  email: string;
  role: 'USER' | 'ADMIN' | string;
  status: 'ACTIVE' | 'DISABLED' | 'LOCKED' | string;
  communityLevel: number;
  experiencePoints: number;
  reputationScore: number;
  postCount: number;
  commentCount: number;
  favoriteCount: number;
  followerCount: number;
  createdTime: string | null;
}

export interface AdminUserDetail extends AdminUser {
  bio: string;
  avatarUrl: string;
  bannerUrl: string;
  publishedPostCount: number;
  archivedPostCount: number;
  likeCount: number;
  collectionCount: number;
  historyCount: number;
  followingCount: number;
  friendCount: number;
  incomingFriendRequestCount: number;
  outgoingFriendRequestCount: number;
  sentMessageCount: number;
  receivedMessageCount: number;
  helpRequestCount: number;
  helpAnswerCount: number;
  acceptedAnswerCount: number;
  reportCount: number;
  reportedPostCount: number;
  uploadCount: number;
  aiCallCount: number;
  aiSuccessCount: number;
  voiceRecordCount: number;
  activeTokenCount: number;
  experienceLogCount: number;
  lastLoginRewardDate: string | number[] | null;
  lastPostTime: string | number[] | null;
  lastCommentTime: string | number[] | null;
  lastHelpTime: string | number[] | null;
  lastAiCallTime: string | number[] | null;
  lastVoiceTime: string | number[] | null;
  updatedTime: string | number[] | null;
}

export interface IntegrationSetting {
  settingKey: string;
  settingValue: string;
  secretFlag: number;
  updatedBy: number | null;
  createdTime: string | null;
  updatedTime: string | null;
}

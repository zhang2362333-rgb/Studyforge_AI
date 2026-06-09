export interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
  requestId?: string | null;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginSession {
  accessToken: string;
  userId: number;
  username: string;
  displayName?: string;
  role: 'USER' | 'ADMIN' | string;
  communityLevel?: number;
  experiencePoints?: number;
  dailyRewardApplied?: boolean;
  dailyExperienceDelta?: number;
}

export interface PostSummary {
  postId: number;
  authorId: number;
  authorName: string;
  authorAvatarUrl: string;
  title: string;
  summary: string;
  languageCode: string;
  categoryCode: string;
  coverImageUrl: string | null;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  viewCount: number;
  hotScore: number;
  createdTime: string | number[] | null;
  updatedTime: string | number[] | null;
}

export interface PostDetail extends PostSummary {
  authorId: number;
  content: string;
  contentFormat: 'MARKDOWN' | 'TEXT' | string;
}

export interface CreatePostRequest {
  categoryId: number;
  originalLanguage: string;
  coverImageUrl?: string | null;
  title: string;
  summary: string;
  content: string;
}

export interface UploadedFile {
  fileId: number;
  originalFilename: string;
  filename: string;
  url: string;
  contentType: string | null;
  size: number;
}

export interface PostInteractionState {
  liked: boolean;
  favorited: boolean;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  viewCount: number;
}

export interface CommentItem {
  commentId: number;
  postId: number;
  parentCommentId: number | null;
  userId: number;
  authorUsername: string;
  authorName: string;
  authorAvatarUrl: string;
  parentUserId: number | null;
  parentAuthorUsername: string;
  parentAuthorName: string;
  languageCode: string;
  content: string;
  status: string;
  floorNo: number;
  likeCount: number;
  likedByViewer: boolean;
  canDelete: boolean;
  deleted: boolean;
  createdTime: string | number[] | null;
  updatedTime: string | number[] | null;
}

export interface ReportSubmission {
  reportId: number;
  status: string;
}

export interface AiResult {
  type: string;
  languageCode: string;
  text: string;
}

export interface AiCoverResult {
  coverImageUrl: string;
  visualBrief: string;
}

export interface AiLogItem {
  logId: number;
  postId: number;
  aiType: string;
  responseText: string;
  success: number;
  createdTime: string;
}

export interface VoiceResult {
  audioDataUrl: string;
  format: string;
}

export interface HelpRequest {
  helpId: number;
  userId: number;
  title: string;
  description: string;
  categoryId: number | null;
  status: string;
  rewardPoints: number;
  createdTime: string | number[] | null;
}

export interface HelpAnswer {
  answerId: number;
  helpId: number;
  parentAnswerId: number | null;
  userId: number;
  authorUsername: string;
  authorName: string;
  authorAvatarUrl: string;
  parentUserId: number | null;
  parentAuthorUsername: string;
  parentAuthorName: string;
  content: string;
  accepted: number;
  status: string;
  floorNo: number;
  likeCount: number;
  likedByViewer: boolean;
  canDelete: boolean;
  deleted: boolean;
  createdTime: string | number[] | null;
  updatedTime: string | number[] | null;
}

export interface TopicCategory {
  code: string;
  name: string;
  description: string;
  accent: string;
}

export interface UserProfile {
  userId: number;
  username: string;
  email: string;
  displayName: string;
  bio: string;
  avatarUrl: string;
  bannerUrl: string;
  communityLevel: number;
  experiencePoints: number;
  nextLevelExperience: number;
  reputationScore: number;
  postCount: number;
  favoriteCount: number;
  historyCount: number;
  followerCount: number;
  followingCount: number;
  friendCount: number;
  commentCount: number;
  receivedLikeCount: number;
  followedByViewer: boolean;
  friendStatus: 'SELF' | 'NONE' | 'PENDING_SENT' | 'PENDING_RECEIVED' | 'FRIEND' | string;
  friendRequestId: number | null;
  self: boolean;
}

export interface SocialUser {
  userId: number;
  username: string;
  displayName: string;
  avatarUrl: string;
  communityLevel: number;
  bio: string;
  followedByViewer: boolean;
}

export interface UserActivity {
  activityKey: string;
  activityType: 'POST_PUBLISHED' | 'HELP_ASKED' | 'HELP_ANSWERED' | 'COMMENTED' | 'LIKED_POST' | 'FAVORITED_POST' | string;
  targetType: 'POST' | 'HELP' | string;
  targetId: number | null;
  postId: number | null;
  helpId: number | null;
  commentId: number | null;
  answerId: number | null;
  title: string;
  summary: string;
  content: string;
  languageCode: string;
  categoryCode: string;
  coverImageUrl: string | null;
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  viewCount: number;
  createdTime: string | number[] | null;
}

export interface FriendRequest {
  requestId: number;
  requester: SocialUser;
  addressee: SocialUser;
  message: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | string;
  createdTime: string | number[] | null;
  processedTime: string | number[] | null;
}

export interface FriendMessage {
  messageId: number;
  senderId: number;
  receiverId: number;
  senderName: string;
  senderAvatarUrl: string;
  receiverName: string;
  receiverAvatarUrl: string;
  content: string;
  read: boolean;
  createdTime: string | number[] | null;
}

export interface FavoriteCollection {
  collectionId: number;
  userId: number;
  name: string;
  description: string;
  visibility: 'PUBLIC' | 'PRIVATE' | string;
  itemCount: number;
  createdTime: string;
}

export interface NotificationItem {
  notificationId: number;
  recipientId: number;
  actorId: number | null;
  actorName: string;
  actorAvatarUrl: string;
  notificationType:
    | 'POST_LIKED'
    | 'POST_FAVORITED'
    | 'POST_COMMENTED'
    | 'COMMENT_REPLIED'
    | 'COMMENT_LIKED'
    | 'COMMENT_MENTIONED'
    | 'HELP_ANSWERED'
    | 'HELP_ANSWER_REPLIED'
    | 'HELP_ANSWER_LIKED'
    | 'HELP_ANSWER_MENTIONED'
    | 'FRIEND_REQUEST'
    | string;
  targetType: 'POST' | 'HELP' | 'USER' | string;
  targetId: number | null;
  postId: number | null;
  helpId: number | null;
  commentId: number | null;
  answerId: number | null;
  friendRequestId: number | null;
  title: string;
  content: string;
  read: boolean;
  createdTime: string | number[] | null;
  readTime: string | number[] | null;
}

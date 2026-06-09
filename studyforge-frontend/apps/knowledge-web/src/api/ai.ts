import { http, unwrap } from '@/api/http';
import type { AiCoverResult, AiLogItem, AiResult } from '@/types/api';

export function generateSummary(postId: number | string, contentLanguageCode: string, promptLanguageCode: string) {
  return unwrap<AiResult>(
    http.post(`/ai/posts/${postId}/summary`, {
      contentLanguageCode,
      promptLanguageCode
    })
  );
}

export function generateReviewCards(postId: number | string, contentLanguageCode: string, promptLanguageCode: string) {
  return unwrap<AiResult>(
    http.post(`/ai/posts/${postId}/review-cards`, {
      contentLanguageCode,
      promptLanguageCode
    })
  );
}

export function askPostQuestion(postId: number | string, question: string, contentLanguageCode: string, promptLanguageCode: string) {
  return unwrap<AiResult>(
    http.post(`/ai/posts/${postId}/questions`, {
      question,
      answerLanguage: promptLanguageCode,
      contentLanguageCode,
      promptLanguageCode
    })
  );
}

export function formatMarkdown(content: string, contentLanguageCode: string, promptLanguageCode: string) {
  return unwrap<AiResult>(
    http.post('/ai/markdown/format', {
      content,
      contentLanguageCode,
      promptLanguageCode
    })
  );
}

export function generateCover(payload: {
  title: string;
  summary: string;
  content: string;
  languageCode: string;
  promptLanguageCode: string;
}) {
  return unwrap<AiCoverResult>(http.post('/ai/covers/generate', payload));
}

export function getMyReviewCards() {
  return unwrap<AiLogItem[]>(http.get('/ai/me/review-cards', { params: { limit: 20 } }));
}

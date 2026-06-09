import { http, unwrap } from '@/api/http';
import type { HelpAnswer, HelpRequest } from '@/types/api';

export function getHelpRequests(status = 'ALL') {
  return unwrap<HelpRequest[]>(http.get('/help', { params: { status, limit: 40 } }));
}

export function createHelpRequest(payload: {
  title: string;
  description: string;
  categoryId?: number | null;
  rewardPoints?: number;
}) {
  return unwrap<number>(http.post('/help', payload));
}

export function getHelpAnswers(helpId: number | string) {
  return unwrap<HelpAnswer[]>(http.get(`/help/${helpId}/answers`));
}

export function createHelpAnswer(helpId: number | string, content: string, parentAnswerId?: number | null) {
  return unwrap<HelpAnswer>(http.post(`/help/${helpId}/answers`, { content, parentAnswerId }));
}

export function toggleHelpAnswerLike(helpId: number | string, answerId: number | string) {
  return unwrap<HelpAnswer>(http.post(`/help/${helpId}/answers/${answerId}/likes`));
}

export async function deleteHelpAnswer(helpId: number | string, answerId: number | string) {
  await unwrap<void>(http.delete(`/help/${helpId}/answers/${answerId}`));
  return true;
}

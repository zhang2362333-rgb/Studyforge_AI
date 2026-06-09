import { http, unwrap } from '@/api/http';
import type { VoiceResult } from '@/types/api';

export function textToSpeech(postId: number | string | null, text: string, languageCode: string) {
  return unwrap<VoiceResult>(
    http.post('/voice/tts', {
      postId,
      text,
      languageCode
    })
  );
}

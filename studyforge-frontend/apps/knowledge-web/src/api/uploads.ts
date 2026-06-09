import { http, unwrap } from '@/api/http';
import type { UploadedFile } from '@/types/api';

export function uploadImage(file: File) {
  const body = new FormData();
  body.append('file', file);

  return unwrap<UploadedFile>(http.post('/uploads/images', body));
}

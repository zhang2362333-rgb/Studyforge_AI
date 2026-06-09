import { http, unwrap } from '@/api/http';
import type { LoginRequest, LoginSession } from '@/types/api';

export function login(payload: LoginRequest) {
  return unwrap<LoginSession>(http.post('/auth/login', payload));
}

export function logout() {
  return unwrap<void>(http.post('/auth/logout'));
}

import { http, unwrap } from '@/api/http';
import type { LoginRequest, LoginSession, RegisterRequest } from '@/types/api';

export function login(payload: LoginRequest) {
  return unwrap<LoginSession>(http.post('/auth/login', payload));
}

export function register(payload: RegisterRequest) {
  return unwrap<number>(http.post('/auth/register', payload));
}

export function logout() {
  return unwrap<void>(http.post('/auth/logout'));
}

import axios, { AxiosError, type AxiosResponse } from 'axios';
import { readStoredSession } from '@/stores/authStorage';
import type { ApiEnvelope } from '@/types/api';

export class ApiError extends Error {
  code: number;
  requestId?: string | null;

  constructor(message: string, code = -1, requestId?: string | null) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.requestId = requestId;
  }
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 200_000,
  headers: {
    'Content-Type': 'application/json'
  }
});

http.interceptors.request.use((config) => {
  const session = readStoredSession();

  if (session?.accessToken) {
    config.headers.Authorization = `Bearer ${session.accessToken}`;
    config.headers['X-User-Role'] = session.role;
  }

  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data as Partial<ApiEnvelope<unknown>>;

    if (typeof body?.code === 'number' && body.code !== 0) {
      throw new ApiError(body.message || '请求没有成功', body.code, body.requestId);
    }

    return response;
  },
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    const responseBody = error.response?.data;

    if (responseBody && typeof responseBody.code === 'number') {
      return Promise.reject(new ApiError(responseBody.message, responseBody.code, responseBody.requestId));
    }

    return Promise.reject(new ApiError(error.message || '网络连接失败'));
  }
);

export async function unwrap<T>(request: Promise<AxiosResponse<ApiEnvelope<T>>>): Promise<T> {
  const response = await request;
  return response.data.data;
}

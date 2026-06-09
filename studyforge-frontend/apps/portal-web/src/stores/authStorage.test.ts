import { beforeEach, describe, expect, it } from 'vitest';
import {
  AUTH_STORAGE_KEY,
  clearStoredSession,
  readStoredSession,
  writeStoredSession
} from './authStorage';
import type { LoginSession } from '@/types/api';

const session: LoginSession = {
  accessToken: 'tok-123',
  userId: 1,
  username: 'alice',
  role: 'ADMIN'
};

describe('authStorage', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('无存储时返回 null', () => {
    expect(readStoredSession()).toBeNull();
  });

  it('写入后可读回相同会话', () => {
    writeStoredSession(session);
    expect(readStoredSession()).toEqual(session);
  });

  it('clear 后读取返回 null', () => {
    writeStoredSession(session);
    clearStoredSession();
    expect(readStoredSession()).toBeNull();
  });

  it('损坏的 JSON 被容错处理：返回 null 并清除该键', () => {
    window.localStorage.setItem(AUTH_STORAGE_KEY, '{ not valid json');
    expect(readStoredSession()).toBeNull();
    // 损坏数据应被主动清除，避免反复解析失败
    expect(window.localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull();
  });

  it('写入使用约定的存储键', () => {
    writeStoredSession(session);
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
    expect(raw).not.toBeNull();
    expect(JSON.parse(raw as string)).toEqual(session);
  });
});

import { defineStore } from 'pinia';
import * as authApi from '@/api/auth';
import { clearStoredSession, readStoredSession, writeStoredSession } from '@/stores/authStorage';
import type { LoginRequest, LoginSession } from '@/types/api';

interface AuthState {
  initialized: boolean;
  loading: boolean;
  session: LoginSession | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    initialized: false,
    loading: false,
    session: null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.session?.accessToken),
    username: (state) => state.session?.username || '未登录',
    role: (state) => state.session?.role || 'GUEST'
  },
  actions: {
    hydrate() {
      if (this.initialized) {
        return;
      }

      this.session = readStoredSession();
      this.initialized = true;
    },
    async login(payload: LoginRequest) {
      this.loading = true;

      try {
        const session = await authApi.login(payload);
        this.session = session;
        writeStoredSession(session);
        return session;
      } finally {
        this.loading = false;
      }
    },
    async logout() {
      try {
        if (this.session?.accessToken) {
          await authApi.logout();
        }
      } finally {
        this.session = null;
        clearStoredSession();
      }
    }
  }
});

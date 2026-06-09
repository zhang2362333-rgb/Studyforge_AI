import { defineStore } from 'pinia';
import * as authApi from '@/api/auth';
import { clearStoredSession, readStoredSession, writeStoredSession } from '@/stores/sessionStorage';
import type { LoginRequest, LoginSession, RegisterRequest } from '@/types/api';
import type { UserProfile } from '@/types/api';

interface SessionState {
  initialized: boolean;
  loading: boolean;
  session: LoginSession | null;
}

export const useSessionStore = defineStore('session', {
  state: (): SessionState => ({
    initialized: false,
    loading: false,
    session: null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.session?.accessToken),
    username: (state) => state.session?.username || '访客',
    displayName: (state) => state.session?.displayName || state.session?.username || '访客',
    userId: (state) => state.session?.userId ?? null
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
    async register(payload: RegisterRequest) {
      this.loading = true;

      try {
        await authApi.register(payload);
        const session = await authApi.login({
          account: payload.username,
          password: payload.password
        });
        this.session = session;
        writeStoredSession(session);
        return session;
      } finally {
        this.loading = false;
      }
    },
    updateFromProfile(profile: UserProfile) {
      if (!this.session) {
        return;
      }
      this.session = {
        ...this.session,
        username: profile.username,
        displayName: profile.displayName,
        communityLevel: profile.communityLevel,
        experiencePoints: profile.experiencePoints
      };
      writeStoredSession(this.session);
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

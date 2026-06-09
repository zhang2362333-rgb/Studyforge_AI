import { defineStore } from 'pinia';

const PREFERENCES_STORAGE_KEY = 'studyforge.portal.preferences';

type LanguageCode = 'zh_CN' | 'en_US';

interface StoredPreferences {
  languageCode?: LanguageCode;
}

function readLanguageCode(): LanguageCode {
  if (typeof window === 'undefined') {
    return 'zh_CN';
  }

  const raw = window.localStorage.getItem(PREFERENCES_STORAGE_KEY);

  if (!raw) {
    return 'zh_CN';
  }

  try {
    const parsed = JSON.parse(raw) as StoredPreferences;
    return parsed.languageCode === 'en_US' ? 'en_US' : 'zh_CN';
  } catch {
    window.localStorage.removeItem(PREFERENCES_STORAGE_KEY);
    return 'zh_CN';
  }
}

function persist(languageCode: LanguageCode) {
  window.localStorage.setItem(PREFERENCES_STORAGE_KEY, JSON.stringify({ languageCode }));
}

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    languageCode: readLanguageCode()
  }),
  actions: {
    setLanguageCode(languageCode: LanguageCode) {
      this.languageCode = languageCode;
      persist(languageCode);
    }
  }
});

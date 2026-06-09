import { defineStore } from 'pinia';

const STORAGE_KEY = 'studyforge.knowledge.preferences';

export type LanguageCode = 'zh_CN' | 'en_US';

function readLanguageCode(): LanguageCode {
  if (typeof window === 'undefined') {
    return 'zh_CN';
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return 'zh_CN';
  }

  try {
    const parsed = JSON.parse(raw) as { languageCode?: LanguageCode };
    return parsed.languageCode === 'en_US' ? 'en_US' : 'zh_CN';
  } catch {
    window.localStorage.removeItem(STORAGE_KEY);
    return 'zh_CN';
  }
}

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    languageCode: readLanguageCode()
  }),
  actions: {
    setLanguageCode(languageCode: LanguageCode) {
      this.languageCode = languageCode;
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify({ languageCode }));
    }
  }
});

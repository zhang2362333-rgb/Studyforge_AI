import type { LanguageCode } from '@/stores/preferences';

export type DateInput = string | number[] | Date | null | undefined;

export function toDate(value: DateInput | unknown) {
  if (!value) {
    return null;
  }

  if (value instanceof Date) {
    return Number.isFinite(value.getTime()) ? value : null;
  }

  if (Array.isArray(value)) {
    const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0, nano = 0] = value.map(Number);
    const date = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1000000));
    return Number.isFinite(date.getTime()) ? date : null;
  }

  const date = new Date(String(value));
  return Number.isFinite(date.getTime()) ? date : null;
}

export function formatDateTime(value: DateInput | unknown, languageCode: LanguageCode = 'zh_CN') {
  const date = toDate(value);
  if (!date) {
    return '';
  }

  return new Intl.DateTimeFormat(languageCode === 'en_US' ? 'en-US' : 'zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

export function formatShortDateTime(value: DateInput | unknown, languageCode: LanguageCode = 'zh_CN') {
  const date = toDate(value);
  if (!date) {
    return '';
  }

  return new Intl.DateTimeFormat(languageCode === 'en_US' ? 'en-US' : 'zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

export function formatRelativeTime(value: DateInput | unknown, languageCode: LanguageCode = 'zh_CN') {
  const date = toDate(value);
  if (!date) {
    return '';
  }

  const now = Date.now();
  const diffSeconds = Math.round((date.getTime() - now) / 1000);
  const absSeconds = Math.abs(diffSeconds);
  const locale = languageCode === 'en_US' ? 'en-US' : 'zh-CN';

  if (absSeconds < 60) {
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(diffSeconds, 'second');
  }
  if (absSeconds < 3600) {
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(Math.round(diffSeconds / 60), 'minute');
  }
  if (absSeconds < 86400) {
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(Math.round(diffSeconds / 3600), 'hour');
  }
  if (absSeconds < 604800) {
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(Math.round(diffSeconds / 86400), 'day');
  }

  return formatShortDateTime(date, languageCode);
}

import { describe, expect, it } from 'vitest';
import { formatDateTime, formatRelativeTime, toDate } from './date';

describe('toDate', () => {
  it('对空值返回 null', () => {
    expect(toDate(null)).toBeNull();
    expect(toDate(undefined)).toBeNull();
    expect(toDate('')).toBeNull();
  });

  it('原样返回有效 Date', () => {
    const d = new Date('2026-06-02T10:00:00Z');
    expect(toDate(d)).toBe(d);
  });

  it('对无效 Date 返回 null', () => {
    expect(toDate(new Date('not-a-date'))).toBeNull();
  });

  it('解析 ISO 字符串', () => {
    const d = toDate('2026-06-02T10:00:00Z');
    expect(d).toBeInstanceOf(Date);
    expect(d?.getUTCFullYear()).toBe(2026);
  });

  it('解析后端常见的数组形式 [year, month, day, ...]', () => {
    // 月份为 1-based，6 表示六月
    const d = toDate([2026, 6, 2, 10, 30]);
    expect(d).toBeInstanceOf(Date);
    expect(d?.getFullYear()).toBe(2026);
    expect(d?.getMonth()).toBe(5); // 0-based
    expect(d?.getDate()).toBe(2);
    expect(d?.getHours()).toBe(10);
    expect(d?.getMinutes()).toBe(30);
  });

  it('对无法解析的字符串返回 null', () => {
    expect(toDate('completely-invalid')).toBeNull();
  });
});

describe('formatDateTime', () => {
  it('对空值返回空字符串', () => {
    expect(formatDateTime(null)).toBe('');
  });

  it('对有效日期返回非空格式化字符串', () => {
    expect(formatDateTime('2026-06-02T10:00:00Z')).not.toBe('');
  });
});

describe('formatRelativeTime', () => {
  it('对空值返回空字符串', () => {
    expect(formatRelativeTime(null)).toBe('');
  });

  it('刚刚的时间给出秒级相对描述（中文）', () => {
    const justNow = new Date(Date.now() - 5_000);
    const text = formatRelativeTime(justNow, 'zh_CN');
    expect(text).not.toBe('');
  });

  it('几分钟前给出分钟级相对描述', () => {
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
    const text = formatRelativeTime(fiveMinutesAgo, 'en_US');
    expect(text.toLowerCase()).toContain('minute');
  });

  it('超过一周回退到短日期格式', () => {
    const longAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const text = formatRelativeTime(longAgo, 'zh_CN');
    // 短日期格式包含数字，不应是 "ago" 这类相对描述
    expect(text).toMatch(/\d/);
  });
});

import { describe, expect, it } from 'vitest';
import { renderMarkdown } from './markdown';

describe('renderMarkdown', () => {
  it('对空输入返回空字符串', () => {
    expect(renderMarkdown('')).toBe('');
  });

  it('渲染基础 Markdown 为 HTML', () => {
    const html = renderMarkdown('# 标题\n\n一段**加粗**文字。');
    expect(html).toContain('<h1>');
    expect(html).toContain('<strong>');
  });

  it('外链补充 target=_blank 与安全 rel', () => {
    const html = renderMarkdown('[link](https://example.com)');
    expect(html).toContain('target="_blank"');
    expect(html).toContain('rel="noreferrer noopener"');
  });

  it('转义内联 script 标签，不产生可执行 script 元素（XSS 防护）', () => {
    const html = renderMarkdown('正常文本 <script>alert(1)</script> 结束');
    // 不应出现真实的 <script> 元素，原始标签被转义为文本
    expect(html).not.toContain('<script>');
    expect(html).not.toContain('<script ');
    expect(html).toContain('&lt;script&gt;');
  });

  it('转义内联 img onerror，不产生可执行图片元素（XSS 防护）', () => {
    const html = renderMarkdown('<img src=x onerror="alert(1)">');
    // 不应产生真实的 <img> 元素，事件处理器无法触发
    expect(html).not.toContain('<img');
    expect(html).toContain('&lt;img');
  });

  it('将 [ ] 渲染为待办复选框', () => {
    const html = renderMarkdown('- [ ] 待办项');
    expect(html).toContain('type="checkbox"');
    expect(html).toContain('disabled');
  });

  it('将 [x] 渲染为已勾选复选框', () => {
    const html = renderMarkdown('- [x] 已完成');
    expect(html).toContain('checked');
  });

  it('禁用原始 HTML 注入（html:false）', () => {
    const html = renderMarkdown('<div class="raw">原始</div>');
    // markdown-it html:false 会转义原始 HTML 标签
    expect(html).not.toContain('<div class="raw">');
  });
});

import DOMPurify from 'dompurify';
import MarkdownIt from 'markdown-it';

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: true
});

const defaultLinkOpen = markdown.renderer.rules.link_open;

markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  const token = tokens[index];
  const hrefIndex = token.attrIndex('href');

  if (hrefIndex >= 0) {
    token.attrSet('target', '_blank');
    token.attrSet('rel', 'noreferrer noopener');
  }

  return defaultLinkOpen ? defaultLinkOpen(tokens, index, options, env, self) : self.renderToken(tokens, index, options);
};

export function renderMarkdown(source: string) {
  const rendered = markdown
    .render(source || '')
    .replace(/<li>\[ \]\s+/g, '<li class="task-list-item"><input type="checkbox" disabled> ')
    .replace(/<li>\[(x|X)\]\s+/g, '<li class="task-list-item"><input type="checkbox" checked disabled> ');

  return DOMPurify.sanitize(rendered, {
    USE_PROFILES: {
      html: true
    },
    ADD_TAGS: ['input'],
    ADD_ATTR: ['target', 'rel', 'class', 'type', 'checked', 'disabled']
  });
}

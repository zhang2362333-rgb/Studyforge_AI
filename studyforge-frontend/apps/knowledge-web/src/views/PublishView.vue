<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Bold,
  Code2,
  Eye,
  FileText,
  Heading2,
  ImagePlus,
  Images,
  Languages,
  Link,
  ListChecks,
  ListOrdered,
  PenLine,
  Quote,
  Save,
  Send,
  SplitSquareHorizontal,
  Sparkles,
  Table2,
  Text,
  X
} from '@lucide/vue';
import { formatMarkdown as requestMarkdownFormat, generateCover } from '@/api/ai';
import { createPost, getPostDetail, updatePost } from '@/api/posts';
import { uploadImage } from '@/api/uploads';
import LoadingState from '@/components/LoadingState.vue';
import MentionTextarea from '@/components/MentionTextarea.vue';
import MarkdownRenderer from '@/components/MarkdownRenderer.vue';
import { usePreferencesStore } from '@/stores/preferences';
import { useSessionStore } from '@/stores/session';

type EditorMode = 'write' | 'preview' | 'split';
type MentionTextareaExpose = {
  focus: () => void;
  setSelectionRange: (start: number, end: number) => void;
  readonly selectionStart: number;
  readonly selectionEnd: number;
};

const DRAFT_KEY = 'studyforge.publish.markdown.draft';
const MAX_IMAGE_SIZE = 8 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif']);

const router = useRouter();
const route = useRoute();
const sessionStore = useSessionStore();
const preferencesStore = usePreferencesStore();
const loading = ref(false);
const loadingPost = ref(false);
const uploading = ref(false);
const formatting = ref(false);
const generatingCover = ref(false);
const errorMessage = ref('');
const savedMessage = ref('');
const coverGeneratedMessage = ref('');
const mode = ref<EditorMode>('split');
const editorRef = ref<MentionTextareaExpose | null>(null);
const coverDragActive = ref(false);
const editorDragActive = ref(false);
const restoring = ref(false);

const form = reactive({
  title: '',
  summary: '',
  content: '',
  coverImageUrl: '',
  categoryId: 1,
  originalLanguage: preferencesStore.languageCode
});

const categories = computed(() => [
  { id: 1, label: preferencesStore.languageCode === 'en_US' ? 'Technology' : '技术实践' },
  { id: 2, label: preferencesStore.languageCode === 'en_US' ? 'Business' : '商业观察' },
  { id: 3, label: preferencesStore.languageCode === 'en_US' ? 'Productivity' : '效率方法' },
  { id: 4, label: preferencesStore.languageCode === 'en_US' ? 'Career' : '职业成长' },
  { id: 5, label: preferencesStore.languageCode === 'en_US' ? 'Finance' : '财务入门' }
]);

const templates = [
  {
    name: '方法笔记',
    description: '适合整理步骤、经验和复盘',
    content:
      '## 我遇到的问题\n\n\n## 我尝试的方法\n\n1. \n2. \n3. \n\n## 最有用的结论\n\n> \n\n## 下次可以直接复用的清单\n\n- [ ] \n- [ ] \n'
  },
  {
    name: '技术文章',
    description: '适合写代码、架构和工具实践',
    content:
      '## 背景\n\n\n## 方案\n\n\n```java\n// 在这里放关键代码\n```\n\n## 为什么这样做\n\n\n## 注意点\n\n- \n'
  },
  {
    name: '阅读卡片',
    description: '适合沉淀书籍、课程、论文',
    content:
      '## 这篇内容讲了什么\n\n\n## 三个要点\n\n- \n- \n- \n\n## 我想继续追问\n\n1. \n2. \n\n## 复习提示\n\n'
  }
];

const wordCount = computed(() => form.content.replace(/\s+/g, '').length);
const readingMinutes = computed(() => Math.max(1, Math.ceil(wordCount.value / 450)));
const hasDraft = computed(() => Boolean(form.title || form.summary || form.content || form.coverImageUrl));
const isEditMode = computed(() => route.name === 'post-edit');
const editPostId = computed(() => (typeof route.params.postId === 'string' ? route.params.postId : ''));
const currentDraftKey = computed(() => (isEditMode.value && editPostId.value ? `${DRAFT_KEY}.${editPostId.value}` : DRAFT_KEY));
const pageTitle = computed(() => (isEditMode.value ? '编辑这篇帖子' : '写一篇可以沉淀的学习帖子'));
const pageDescription = computed(() =>
  isEditMode.value
    ? '修改正文、摘要、封面和主题后保存，文章会继续保留原有的阅读、收藏和讨论记录。'
    : '用 Markdown 写正文，也可以通过工具栏插入标题、链接、图片、代码块和表格。右侧预览就是发布后的文章效果。'
);

function categoryIdFromCode(categoryCode: string) {
  const map: Record<string, number> = {
    TECHNOLOGY: 1,
    BUSINESS: 2,
    PRODUCTIVITY: 3,
    CAREER: 4,
    FINANCE: 5
  };
  return map[categoryCode] ?? 1;
}

function saveDraft() {
  localStorage.setItem(currentDraftKey.value, JSON.stringify(form));
  savedMessage.value = '草稿已保存';
  window.setTimeout(() => {
    savedMessage.value = '';
  }, 1400);
}

function clearDraft() {
  localStorage.removeItem(currentDraftKey.value);
}

function loadDraft() {
  const raw = localStorage.getItem(currentDraftKey.value);
  if (!raw) {
    return;
  }

  try {
    const draft = JSON.parse(raw) as Partial<typeof form>;
    form.title = draft.title ?? '';
    form.summary = draft.summary ?? '';
    form.content = draft.content ?? '';
    form.coverImageUrl = draft.coverImageUrl ?? '';
    form.categoryId = draft.categoryId ?? 1;
    form.originalLanguage = draft.originalLanguage ?? preferencesStore.languageCode;
  } catch {
    clearDraft();
  }
}

async function loadEditablePost() {
  if (!editPostId.value) {
    return;
  }
  if (!sessionStore.isAuthenticated) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }

  loadingPost.value = true;
  restoring.value = true;
  errorMessage.value = '';

  try {
    const detail = await getPostDetail(editPostId.value, preferencesStore.languageCode);
    if (detail.authorId !== sessionStore.userId) {
      errorMessage.value = '只有作者本人可以编辑这篇帖子。';
      return;
    }
    form.title = detail.title;
    form.summary = detail.summary;
    form.content = detail.content;
    form.coverImageUrl = detail.coverImageUrl ?? '';
    form.categoryId = categoryIdFromCode(detail.categoryCode);
    form.originalLanguage = detail.languageCode;
    loadDraft();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '这篇帖子暂时无法编辑';
  } finally {
    restoring.value = false;
    loadingPost.value = false;
  }
}

async function loadInitialState() {
  restoring.value = true;
  errorMessage.value = '';
  if (isEditMode.value) {
    restoring.value = false;
    await loadEditablePost();
    return;
  }
  form.title = '';
  form.summary = '';
  form.content = '';
  form.coverImageUrl = '';
  form.categoryId = 1;
  form.originalLanguage = preferencesStore.languageCode;
  loadDraft();
  restoring.value = false;
}

function insertMarkdown(before: string, after = '', placeholder = '文本') {
  const textarea = editorRef.value;
  const start = textarea?.selectionStart ?? form.content.length;
  const end = textarea?.selectionEnd ?? form.content.length;
  const selected = form.content.slice(start, end) || placeholder;
  const next = `${before}${selected}${after}`;
  form.content = form.content.slice(0, start) + next + form.content.slice(end);

  requestAnimationFrame(() => {
    editorRef.value?.focus();
    const cursor = start + before.length + selected.length;
    editorRef.value?.setSelectionRange(cursor, cursor);
  });
}

function selectedText() {
  const textarea = editorRef.value;
  if (!textarea) {
    return '';
  }
  return form.content.slice(textarea.selectionStart, textarea.selectionEnd);
}

function insertBlock(block: string) {
  const prefix = form.content && !form.content.endsWith('\n') ? '\n\n' : '';
  form.content += `${prefix}${block}`;
  requestAnimationFrame(() => editorRef.value?.focus());
}

function insertCodeBlock() {
  insertBlock('```java\n// code\n```');
}

function insertTaskList() {
  insertBlock('- [ ] 待完成\n- [ ] 下一步');
}

function insertOrderedList() {
  insertBlock('1. 第一步\n2. 第二步\n3. 第三步');
}

function insertTable() {
  insertBlock('| 项目 | 说明 |\n| --- | --- |\n|  |  |');
}

function insertLink() {
  const text = window.prompt('链接文字', selectedText() || '链接文本');
  if (text === null) {
    return;
  }
  const url = window.prompt('链接地址', 'https://');
  if (!url) {
    return;
  }
  insertMarkdown('[', `](${url.trim()})`, text.trim() || '链接文本');
}

function insertImageByUrl() {
  const url = window.prompt('图片地址', 'https://');
  if (!url) {
    return;
  }
  const alt = window.prompt('图片说明', '图片') || '图片';
  insertBlock(`![${alt.trim() || '图片'}](${url.trim()})`);
}

function applyTemplate(content: string) {
  const prefix = form.content.trim() ? '\n\n---\n\n' : '';
  form.content += `${prefix}${content}`;
}

async function handleCoverUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) {
    return;
  }
  await uploadAndUse(file, 'cover');
  (event.target as HTMLInputElement).value = '';
}

async function handleInlineImageUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) {
    return;
  }
  await uploadAndUse(file, 'inline');
  (event.target as HTMLInputElement).value = '';
}

async function handleCoverDrop(event: DragEvent) {
  coverDragActive.value = false;
  const file = firstImage(event.dataTransfer?.files);
  if (file) {
    await uploadAndUse(file, 'cover');
  }
}

async function handleEditorDrop(event: DragEvent) {
  editorDragActive.value = false;
  const file = firstImage(event.dataTransfer?.files);
  if (file) {
    await uploadAndUse(file, 'inline');
  }
}

async function handleEditorPaste(event: ClipboardEvent) {
  const file = firstImage(event.clipboardData?.files);
  if (!file) {
    return;
  }
  event.preventDefault();
  await uploadAndUse(file, 'inline');
}

function handleEditorKeydown(event: KeyboardEvent) {
  if (!event.metaKey && !event.ctrlKey) {
    return;
  }

  const key = event.key.toLowerCase();
  if (key === 'b') {
    event.preventDefault();
    insertMarkdown('**', '**', '重点');
  }
  if (key === 'k') {
    event.preventDefault();
    insertLink();
  }
}

function firstImage(files?: FileList | null) {
  if (!files) {
    return null;
  }
  return Array.from(files).find((file) => file.type.startsWith('image/')) ?? null;
}

function imageValidationMessage(file: File) {
  if (file.size > MAX_IMAGE_SIZE) {
    return '图片不能超过 8MB';
  }
  if (file.type && !ALLOWED_IMAGE_TYPES.has(file.type)) {
    return '只支持 JPG、PNG、WebP 和 GIF 图片';
  }
  return '';
}

async function uploadAndUse(file: File, target: 'cover' | 'inline') {
  if (!sessionStore.isAuthenticated) {
    errorMessage.value = '请先登录再上传图片';
    return;
  }

  const validationMessage = imageValidationMessage(file);
  if (validationMessage) {
    errorMessage.value = validationMessage;
    return;
  }

  uploading.value = true;
  errorMessage.value = '';

  try {
    const uploaded = await uploadImage(file);
    if (target === 'cover') {
      form.coverImageUrl = uploaded.url;
    } else {
      insertBlock(`![${file.name}](${uploaded.url})`);
    }
    saveDraft();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '图片暂时上传不了';
  } finally {
    uploading.value = false;
  }
}

function removeCover() {
  form.coverImageUrl = '';
  saveDraft();
}

async function formatWithAi() {
  if (!sessionStore.isAuthenticated) {
    errorMessage.value = '请先登录再使用 AI 排版';
    return;
  }

  const source = form.content.trim();
  if (!source) {
    errorMessage.value = '先写入一段正文，再使用 AI 排版';
    return;
  }
  if (source.length > 12000) {
    errorMessage.value = 'AI 排版一次最多处理 12000 个字符';
    return;
  }

  formatting.value = true;
  errorMessage.value = '';

  try {
    const result = await requestMarkdownFormat(source, form.originalLanguage, preferencesStore.languageCode);
    form.content = result.text.trim();
    mode.value = 'split';
    saveDraft();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'AI 排版暂时没有成功';
  } finally {
    formatting.value = false;
  }
}

async function generateCoverWithAi() {
  if (!sessionStore.isAuthenticated) {
    errorMessage.value = '请先登录再生成封面';
    return;
  }

  const title = form.title.trim();
  const summary = form.summary.trim();
  const content = form.content.trim();
  if (!title && !summary && !content) {
    errorMessage.value = '先写标题、摘要或正文，再生成封面';
    return;
  }
  if (content.length > 12000) {
    errorMessage.value = '生成封面一次最多参考 12000 个字符';
    return;
  }

  generatingCover.value = true;
  coverGeneratedMessage.value = '';
  errorMessage.value = '';

  try {
    const result = await generateCover({
      title,
      summary,
      content,
      languageCode: form.originalLanguage,
      promptLanguageCode: preferencesStore.languageCode
    });
    form.coverImageUrl = result.coverImageUrl;
    coverGeneratedMessage.value = result.visualBrief ? `封面已生成：${result.visualBrief}` : '封面已生成';
    saveDraft();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'AI 封面暂时没有生成成功';
  } finally {
    generatingCover.value = false;
  }
}

async function submit() {
  if (!sessionStore.isAuthenticated) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = {
      categoryId: Number(form.categoryId),
      originalLanguage: form.originalLanguage,
      coverImageUrl: form.coverImageUrl || null,
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim()
    };
    const postId = isEditMode.value && editPostId.value ? await updatePost(editPostId.value, payload) : await createPost(payload);
    clearDraft();
    await router.push(`/posts/${postId}`);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : isEditMode.value ? '暂时保存不了，请稍后再试' : '暂时发布不了，请稍后再试';
  } finally {
    loading.value = false;
  }
}

onMounted(loadInitialState);

watch(
  () => [route.name, route.params.postId],
  () => loadInitialState()
);

watch(
  form,
  () => {
    if (!restoring.value) {
      localStorage.setItem(currentDraftKey.value, JSON.stringify(form));
    }
  },
  { deep: true }
);
</script>

<template>
  <section class="editor-page markdown-editor-page">
    <div class="page-heading">
      <span class="section-kicker">Markdown Post</span>
      <h1>{{ pageTitle }}</h1>
      <p>{{ pageDescription }}</p>
    </div>

    <LoadingState v-if="loadingPost" label="正在读取帖子内容" />

    <form v-else class="editor-layout markdown-editor-layout" @submit.prevent="submit">
      <div class="editor-main markdown-editor-main">
        <div class="post-composer-head">
          <label>
            <span>标题</span>
            <input v-model="form.title" required maxlength="120" placeholder="例如：一次把 Markdown 学习笔记写清楚" />
          </label>

          <label>
            <span>摘要</span>
            <textarea v-model="form.summary" rows="3" maxlength="300" placeholder="用一两句话说明这篇内容能帮读者解决什么问题" />
          </label>
        </div>

        <div class="cover-uploader">
          <div v-if="form.coverImageUrl" class="cover-preview">
            <img :src="form.coverImageUrl" alt="" />
            <button type="button" title="移除封面" @click="removeCover">
              <X :size="16" />
            </button>
          </div>
          <div class="cover-actions">
            <label
              class="upload-drop"
              :class="{ active: coverDragActive }"
              @dragenter.prevent="coverDragActive = true"
              @dragover.prevent="coverDragActive = true"
              @dragleave.prevent="coverDragActive = false"
              @drop.prevent="handleCoverDrop"
            >
              <ImagePlus :size="20" />
              <strong>{{ form.coverImageUrl ? '更换封面' : '上传封面' }}</strong>
              <span>点击选择，或把 JPG、PNG、WebP、GIF 拖到这里</span>
              <input accept="image/*" type="file" @change="handleCoverUpload" />
            </label>
            <button class="ai-cover-button" type="button" :disabled="generatingCover || uploading || formatting" @click="generateCoverWithAi">
              <Images :size="19" />
              <strong>{{ generatingCover ? '生成中' : 'AI 生成封面' }}</strong>
              <span>根据标题、摘要和正文生成博客风格封面</span>
            </button>
          </div>
        </div>

        <div class="editor-toolbar" aria-label="Markdown 工具栏">
          <button class="ai-format-button" type="button" title="AI 排版" :disabled="formatting || uploading || generatingCover" @click="formatWithAi">
            <Sparkles :size="17" />
            <span>{{ formatting ? '排版中' : 'AI 排版' }}</span>
          </button>
          <button type="button" title="二级标题" @click="insertMarkdown('## ', '', '标题')">
            <Heading2 :size="17" />
          </button>
          <button type="button" title="加粗" @click="insertMarkdown('**', '**', '重点')">
            <Bold :size="17" />
          </button>
          <button type="button" title="引用" @click="insertMarkdown('> ', '', '引用内容')">
            <Quote :size="17" />
          </button>
          <button type="button" title="链接" @click="insertLink">
            <Link :size="17" />
          </button>
          <button type="button" title="代码块" @click="insertCodeBlock">
            <Code2 :size="17" />
          </button>
          <button type="button" title="任务列表" @click="insertTaskList">
            <ListChecks :size="17" />
          </button>
          <button type="button" title="有序列表" @click="insertOrderedList">
            <ListOrdered :size="17" />
          </button>
          <button type="button" title="表格" @click="insertTable">
            <Table2 :size="17" />
          </button>
          <button type="button" title="图片链接" @click="insertImageByUrl">
            <ImagePlus :size="17" />
          </button>
          <label class="toolbar-upload" title="上传并插入图片">
            <ImagePlus :size="17" />
            <input accept="image/*" type="file" @change="handleInlineImageUpload" />
          </label>
        </div>

        <div class="editor-mode-tabs">
          <button type="button" :class="{ active: mode === 'write' }" @click="mode = 'write'">
            <Text :size="16" />
            <span>编辑</span>
          </button>
          <button type="button" :class="{ active: mode === 'split' }" @click="mode = 'split'">
            <SplitSquareHorizontal :size="16" />
            <span>分屏</span>
          </button>
          <button type="button" :class="{ active: mode === 'preview' }" @click="mode = 'preview'">
            <Eye :size="16" />
            <span>预览</span>
          </button>
        </div>

        <div class="markdown-workbench" :class="`mode-${mode}`">
          <label v-show="mode !== 'preview'" class="markdown-source">
            <span>正文 Markdown</span>
            <MentionTextarea
              ref="editorRef"
              v-model="form.content"
              required
              spellcheck="false"
              :class="{ 'drag-active': editorDragActive }"
              placeholder="可以直接写 Markdown，也可以用上方工具栏插入格式。"
              @dragenter.prevent="editorDragActive = true"
              @dragover.prevent="editorDragActive = true"
              @dragleave.prevent="editorDragActive = false"
              @drop.prevent="handleEditorDrop"
              @paste="handleEditorPaste"
              @keydown="handleEditorKeydown"
            />
          </label>

          <section v-show="mode !== 'write'" class="markdown-preview-panel">
            <div class="preview-head">
              <span>发布预览</span>
              <small>{{ wordCount }} 字 · 约 {{ readingMinutes }} 分钟读完</small>
            </div>
            <article class="preview-document">
              <img v-if="form.coverImageUrl" class="preview-cover" :src="form.coverImageUrl" alt="" />
              <h1>{{ form.title || '未命名帖子' }}</h1>
              <p v-if="form.summary" class="article-summary">{{ form.summary }}</p>
              <MarkdownRenderer :content="form.content || '从这里开始写正文。'" />
            </article>
          </section>
        </div>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <p v-if="coverGeneratedMessage" class="form-success cover-generated-message">{{ coverGeneratedMessage }}</p>
        <p v-if="savedMessage" class="form-success">{{ savedMessage }}</p>

        <div class="form-actions composer-actions">
          <button class="secondary-button" type="button" :disabled="!hasDraft" @click="saveDraft">
            <Save :size="17" />
            <span>保存草稿</span>
          </button>
          <button class="primary-button" type="submit" :disabled="loading || uploading || formatting || generatingCover">
            <Send :size="17" />
            <span>{{
              loading
                ? isEditMode
                  ? '正在保存'
                  : '正在发布'
                : uploading
                  ? '正在上传'
                  : generatingCover
                    ? '正在生成封面'
                    : formatting
                      ? '正在排版'
                      : isEditMode
                        ? '保存修改'
                        : '发布到知识流'
            }}</span>
          </button>
        </div>
      </div>

      <aside class="editor-side markdown-editor-side">
        <section class="side-panel">
          <div class="panel-title">
            <PenLine :size="18" />
            <span>发布设置</span>
          </div>
          <label>
            <span>主题</span>
            <select v-model="form.categoryId">
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.label }}
              </option>
            </select>
          </label>
          <label>
            <span>内容语言</span>
            <select v-model="form.originalLanguage">
              <option value="zh_CN">中文</option>
              <option value="en_US">English</option>
            </select>
          </label>
        </section>

        <section class="side-panel preset-panel">
          <div class="panel-title">
            <FileText :size="18" />
            <span>文章预设</span>
          </div>
          <button v-for="template in templates" :key="template.name" type="button" @click="applyTemplate(template.content)">
            <strong>{{ template.name }}</strong>
            <span>{{ template.description }}</span>
          </button>
        </section>

        <section class="side-panel">
          <div class="panel-title">
            <Languages :size="18" />
            <span>语言规则</span>
          </div>
          <p>帖子展示以你写作的语言为准。切换站点语言时，如果没有对应版本，会展示原文，不会自动翻译你的内容。</p>
        </section>
      </aside>
    </form>
  </section>
</template>

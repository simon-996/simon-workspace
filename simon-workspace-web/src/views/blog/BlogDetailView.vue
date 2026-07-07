<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useMessage, NButton, NIcon, NInput, NSpin } from 'naive-ui'
import { ArrowLeft, Eye, MessageCircle, Send } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import {
  createBlogComment,
  fetchBlogComments,
  fetchBlogPostDetail,
  type BlogComment,
  type BlogPostDetail,
} from '../../api/workspace'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()
const auth = useAuthStore()
const theme = useThemeStore()
const post = ref<BlogPostDetail | null>(null)
const comments = ref<BlogComment[]>([])
const comment = ref('')
const loading = ref(false)
const submitting = ref(false)

onMounted(() => {
  void auth.restore()
  void load()
})

async function load() {
  loading.value = true
  try {
    const id = String(route.params.id)
    const [postData, commentData] = await Promise.all([
      fetchBlogPostDetail(id),
      fetchBlogComments(id),
    ])
    post.value = postData
    comments.value = commentData
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.postLoadFailed'))
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!comment.value.trim() || !post.value) return
  submitting.value = true
  try {
    await createBlogComment(post.value.id, comment.value.trim())
    comment.value = ''
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('blog.messages.commentFailed'))
  } finally {
    submitting.value = false
  }
}

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : '-'
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <n-spin :show="loading">
      <article v-if="post" class="post-detail">
        <n-button quaternary class="back-button" @click="router.push('/blog')">
          <template #icon>
            <n-icon :component="ArrowLeft" />
          </template>
          {{ t('blog.detail.back') }}
        </n-button>

        <header class="article-header">
          <span class="article-kicker">{{ post.category?.name || t('blog.list.uncategorized') }}</span>
          <h1>{{ post.title }}</h1>
          <p v-if="post.summary">{{ post.summary }}</p>
          <div class="article-meta">
            <span>
              <n-icon :component="Eye" />
              {{ post.viewCount }}
            </span>
            <span class="comment-count">
              <n-icon :component="MessageCircle" />
              {{ comments.length }}
            </span>
            <time>{{ formatDate(post.publishedTime) }}</time>
          </div>
        </header>

        <section class="article-body">
          <MdPreview :model-value="post.contentMd" :theme="theme.isDark ? 'dark' : 'light'" preview-theme="github" />
        </section>

        <section class="comments">
          <header class="comments-head">
            <h2>{{ t('blog.detail.comments') }}</h2>
            <span class="comment-count">{{ comments.length }}</span>
          </header>

          <div v-if="auth.isAuthenticated" class="comment-box">
            <n-input
              v-model:value="comment"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 7 }"
              :placeholder="t('blog.detail.commentPlaceholder')"
            />
            <div class="comment-submit-row">
              <span>{{ comment.trim().length }}</span>
              <n-button type="primary" :loading="submitting" :disabled="!comment.trim()" @click="submitComment">
                <template #icon>
                  <n-icon :component="Send" />
                </template>
                {{ t('blog.detail.send') }}
              </n-button>
            </div>
          </div>

          <p v-else class="signin-hint">{{ t('blog.detail.signInToComment') }}</p>
          <p v-if="!comments.length" class="comment-empty">{{ t('blog.detail.noComments') }}</p>

          <div v-if="comments.length" class="comment-list">
            <div v-for="item in comments" :key="item.id" class="comment-item">
              <div>
                <strong>{{ item.authorName }}</strong>
                <span>{{ item.createdTime ? item.createdTime.slice(0, 16).replace('T', ' ') : '' }}</span>
              </div>
              <p>{{ item.content }}</p>
            </div>
          </div>
        </section>
      </article>
    </n-spin>
  </main>
</template>

<style scoped>
.blog-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.post-detail {
  width: min(880px, calc(100% - 32px));
  margin: 0 auto;
  padding: 10px 0 72px;
}

.back-button {
  margin-bottom: 6px;
}

.article-header {
  border-bottom: 1px solid var(--sw-border);
  margin-bottom: 24px;
  padding: 18px 0 24px;
}

.article-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border: 1px solid color-mix(in srgb, var(--sw-accent) 30%, transparent);
  border-radius: 999px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  font-size: 12px;
  font-weight: 800;
  padding: 0 11px;
}

.post-detail h1 {
  max-width: 14ch;
  margin: 14px 0 12px;
  color: var(--sw-text);
  font-size: clamp(32px, 6vw, 58px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1.02;
}

.article-header p,
.signin-hint,
.comment-empty {
  color: var(--sw-muted);
  font-size: 14px;
  line-height: 1.7;
}

.article-header p {
  max-width: 70ch;
  margin: 0;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.article-meta span,
.article-meta time,
.comments-head .comment-count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  border: 1px solid var(--sw-border);
  border-radius: 999px;
  background: var(--sw-panel-bg-strong);
  color: var(--sw-muted);
  font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  padding: 0 10px;
}

.article-meta .n-icon {
  color: var(--sw-accent);
  font-size: 15px;
}

.article-body {
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  box-shadow: var(--sw-shadow-soft);
}

.article-body :deep(.md-editor-preview-wrapper) {
  padding: 28px;
}

.article-body :deep(.md-editor-preview) {
  color: var(--sw-text);
  font-family:
    Outfit, Geist, Satoshi, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont,
    "Segoe UI", "Microsoft YaHei", sans-serif;
}

.article-body :deep(.md-editor-preview h1),
.article-body :deep(.md-editor-preview h2),
.article-body :deep(.md-editor-preview h3) {
  color: var(--sw-text);
  letter-spacing: 0;
}

.article-body :deep(.md-editor-preview p),
.article-body :deep(.md-editor-preview li) {
  color: var(--sw-table-cell-text);
  line-height: 1.78;
}

.comments {
  border-top: 1px solid var(--sw-border);
  margin-top: 36px;
  padding-top: 24px;
}

.comments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.comments h2 {
  margin: 0;
  color: var(--sw-text);
  font-size: 22px;
  font-weight: 800;
}

.comment-box {
  display: grid;
  gap: 10px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  box-shadow: var(--sw-shadow-soft);
  margin-bottom: 18px;
  padding: 10px;
}

.comment-submit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.comment-submit-row span {
  color: var(--sw-faint);
  font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
}

.signin-hint,
.comment-empty {
  margin: 0 0 14px;
}

.comment-empty {
  display: grid;
  min-height: 96px;
  place-items: center;
  border: 1px dashed var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
}

.comment-list {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  box-shadow: var(--sw-shadow-soft);
}

.comment-item {
  padding: 16px 18px;
}

.comment-item + .comment-item {
  border-top: 1px solid var(--sw-border-soft);
}

.comment-item div {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}

.comment-item strong {
  color: var(--sw-text);
  font-size: 14px;
  font-weight: 800;
}

.comment-item span {
  color: var(--sw-muted);
  font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
}

.comment-item p {
  margin: 8px 0 0;
  color: var(--sw-table-cell-text);
  line-height: 1.65;
}

@media (max-width: 920px) {
  .post-detail {
    padding: 0 0 48px;
  }
}

@media (max-width: 620px) {
  .post-detail {
    width: min(100% - 24px, 880px);
  }

  .post-detail h1 {
    max-width: none;
  }

  .article-body :deep(.md-editor-preview-wrapper) {
    padding: 20px;
  }
}
</style>

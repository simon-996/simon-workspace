<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useMessage, NButton, NIcon, NInput, NSpin } from 'naive-ui'
import { ArrowLeft, Send } from '@vicons/tabler'

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
    message.error(error instanceof Error ? error.message : 'Failed to load post')
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
    message.error(error instanceof Error ? error.message : 'Failed to comment')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <n-spin :show="loading">
      <article v-if="post" class="post-detail">
        <n-button quaternary @click="router.push('/blog')">
          <template #icon>
            <n-icon :component="ArrowLeft" />
          </template>
          Blog
        </n-button>
        <header>
          <span>{{ post.category?.name || 'Uncategorized' }}</span>
          <h1>{{ post.title }}</h1>
          <p>{{ post.summary }}</p>
        </header>

        <MdPreview :model-value="post.contentMd" :theme="theme.isDark ? 'dark' : 'light'" preview-theme="github" />

        <section class="comments">
          <h2>Comments</h2>
          <div v-if="auth.isAuthenticated" class="comment-box">
            <n-input v-model:value="comment" type="textarea" placeholder="Write a comment" />
            <n-button type="primary" :loading="submitting" @click="submitComment">
              <template #icon>
                <n-icon :component="Send" />
              </template>
              Send
            </n-button>
          </div>
          <p v-else class="signin-hint">Sign in to comment.</p>
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <strong>{{ item.authorName }}</strong>
            <span>{{ item.createdTime ? item.createdTime.slice(0, 16).replace('T', ' ') : '' }}</span>
            <p>{{ item.content }}</p>
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
  padding: 96px 0 72px;
}

.post-detail header {
  border-bottom: 1px solid var(--sw-border);
  margin-bottom: 24px;
  padding: 22px 0 26px;
}

.post-detail header span {
  color: #16708f;
  font-size: 13px;
  font-weight: 800;
}

.post-detail h1 {
  margin: 10px 0;
  font-size: clamp(34px, 6vw, 64px);
  line-height: 1;
}

.post-detail header p,
.signin-hint {
  color: var(--sw-muted);
}

.comments {
  border-top: 1px solid var(--sw-border);
  margin-top: 36px;
  padding-top: 24px;
}

.comments h2 {
  margin: 0 0 14px;
}

.comment-box {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.comment-box .n-button {
  justify-self: end;
}

.comment-item {
  border-top: 1px solid var(--sw-border);
  padding: 14px 0;
}

.comment-item span {
  margin-left: 8px;
  color: var(--sw-muted);
  font-size: 12px;
}

.comment-item p {
  margin: 8px 0 0;
}
</style>

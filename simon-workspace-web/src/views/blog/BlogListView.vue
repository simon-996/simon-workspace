<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NIcon, NInput, NSpin } from 'naive-ui'
import { Edit, MessageCircle, Refresh, Search } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import { fetchBlogCategories, fetchBlogPosts, type BlogCategory, type BlogPostSummary } from '../../api/workspace'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()
const loading = ref(false)
const keyword = ref('')
const selectedCategory = ref<string | null>(null)
const posts = ref<BlogPostSummary[]>([])
const categories = ref<BlogCategory[]>([])

const canWrite = computed(() => auth.hasPermission('blog:post:create'))

onMounted(() => {
  void auth.restore()
  void load()
})

async function load() {
  loading.value = true
  try {
    const [categoryData, postData] = await Promise.all([
      fetchBlogCategories(),
      fetchBlogPosts({
        keyword: keyword.value.trim(),
        categoryId: selectedCategory.value,
      }),
    ])
    categories.value = categoryData
    posts.value = postData
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'Failed to load blog')
  } finally {
    loading.value = false
  }
}

function openPost(post: BlogPostSummary) {
  void router.push(`/blog/${post.id}`)
}
</script>

<template>
  <main class="blog-shell">
    <AppHeader />
    <section class="blog-hero">
      <div>
        <span>Blog</span>
        <h1>Notes, code, and field records.</h1>
      </div>
      <n-button v-if="canWrite" type="primary" @click="router.push('/blog/new')">
        <template #icon>
          <n-icon :component="Edit" />
        </template>
        Write
      </n-button>
    </section>

    <section class="blog-toolbar">
      <n-input v-model:value="keyword" clearable placeholder="Search posts" @keyup.enter="load">
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <div class="category-tabs">
        <button :class="{ active: selectedCategory === null }" @click="selectedCategory = null; load()">All</button>
        <button
          v-for="category in categories"
          :key="category.id"
          :class="{ active: selectedCategory === category.id }"
          @click="selectedCategory = category.id; load()"
        >
          {{ category.name }}
        </button>
      </div>
      <n-button secondary @click="load">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
      </n-button>
    </section>

    <n-spin :show="loading">
      <section v-if="posts.length" class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card" @click="openPost(post)">
          <div>
            <span>{{ post.category?.name || 'Uncategorized' }}</span>
            <time>{{ post.publishedTime ? post.publishedTime.slice(0, 10) : '-' }}</time>
          </div>
          <h2>{{ post.title }}</h2>
          <p>{{ post.summary || 'No summary.' }}</p>
          <footer>
            <strong v-for="tag in post.tags" :key="tag.id">#{{ tag.name }}</strong>
            <span>
              <n-icon :component="MessageCircle" />
              {{ post.commentCount }}
            </span>
          </footer>
        </article>
      </section>
      <section v-else class="empty-blog">
        <strong>No posts yet.</strong>
      </section>
    </n-spin>
  </main>
</template>

<style scoped>
.blog-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.blog-hero,
.blog-toolbar,
.post-list,
.empty-blog {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
}

.blog-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  padding: 112px 0 28px;
}

.blog-hero span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

.blog-hero h1 {
  max-width: 760px;
  margin: 8px 0 0;
  font-size: clamp(36px, 6vw, 72px);
  line-height: 0.98;
}

.blog-toolbar {
  display: grid;
  grid-template-columns: minmax(180px, 340px) 1fr auto;
  gap: 12px;
  align-items: center;
  border-top: 1px solid var(--sw-border);
  border-bottom: 1px solid var(--sw-border);
  padding: 14px 0;
}

.category-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.category-tabs button {
  border: 1px solid var(--sw-border);
  border-radius: 999px;
  background: var(--sw-surface-solid);
  color: var(--sw-muted);
  cursor: pointer;
  padding: 7px 12px;
  white-space: nowrap;
}

.category-tabs button.active {
  border-color: #16708f;
  color: #16708f;
}

.post-list {
  display: grid;
  gap: 12px;
  padding: 22px 0 72px;
}

.post-card {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  cursor: pointer;
  padding: 22px;
  transition: transform 180ms ease, border-color 180ms ease;
}

.post-card:hover {
  border-color: #85afbc;
  transform: translateY(-2px);
}

.post-card div,
.post-card footer {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--sw-muted);
  font-size: 13px;
}

.post-card h2 {
  margin: 10px 0 8px;
  font-size: 24px;
}

.post-card p {
  margin: 0 0 18px;
  color: var(--sw-muted);
}

.post-card footer {
  justify-content: space-between;
}

.post-card footer strong {
  color: #16708f;
  font-size: 13px;
}

.empty-blog {
  display: grid;
  place-items: center;
  min-height: 320px;
  color: var(--sw-muted);
}

@media (max-width: 760px) {
  .blog-hero,
  .blog-toolbar {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .blog-hero {
    display: grid;
    padding-top: 92px;
  }
}
</style>

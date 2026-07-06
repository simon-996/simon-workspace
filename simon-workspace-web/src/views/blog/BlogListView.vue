<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NIcon, NInput, NSpin } from 'naive-ui'
import { Edit, MessageCircle, Refresh, Search } from '@vicons/tabler'

import AppHeader from '../../components/AppHeader.vue'
import { fetchBlogCategories, fetchBlogPosts, type BlogCategory, type BlogPostSummary } from '../../api/workspace'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()
const { t } = useI18n()
const loading = ref(false)
const keyword = ref('')
const selectedCategory = ref<string | null>(null)
const posts = ref<BlogPostSummary[]>([])
const categories = ref<BlogCategory[]>([])

const canWrite = computed(() => auth.hasPermission('blog:post:create'))
const selectedCategoryName = computed(() => {
  if (!selectedCategory.value) return t('blog.list.all')
  return categories.value.find((category) => category.id === selectedCategory.value)?.name || t('blog.list.all')
})

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
    message.error(error instanceof Error ? error.message : t('blog.messages.loadFailed'))
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
        <span>{{ t('blog.list.kicker') }}</span>
      </div>
      <n-button v-if="canWrite" type="primary" @click="router.push('/blog/new')">
        <template #icon>
          <n-icon :component="Edit" />
        </template>
        {{ t('blog.list.write') }}
      </n-button>
    </section>

    <section class="blog-layout">
      <aside class="blog-sidebar">
        <n-input v-model:value="keyword" clearable :placeholder="t('blog.list.searchPlaceholder')" @keyup.enter="load">
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
        <div class="filter-title">{{ t('blog.list.categories') }}</div>
        <div class="category-tabs">
          <button :class="{ active: selectedCategory === null }" @click="selectedCategory = null; load()">
            {{ t('blog.list.all') }}
          </button>
        <button
          v-for="category in categories"
          :key="category.id"
          :class="{ active: selectedCategory === category.id }"
          @click="selectedCategory = category.id; load()"
        >
          {{ category.name }}
        </button>
      </div>
        <div class="selected-filter">{{ selectedCategoryName }}</div>
        <n-button secondary block @click="load">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
          {{ t('common.actions.refresh') }}
      </n-button>
      </aside>

      <n-spin :show="loading" class="blog-content">
        <section v-if="loading && !posts.length" class="blog-skeleton">
          <span v-for="index in 4" :key="index" />
        </section>
        <section v-else-if="posts.length" class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card" @click="openPost(post)">
          <div>
              <span>{{ post.category?.name || t('blog.list.uncategorized') }}</span>
            <time>{{ post.publishedTime ? post.publishedTime.slice(0, 10) : '-' }}</time>
          </div>
          <h2>{{ post.title }}</h2>
            <p>{{ post.summary || t('blog.list.noSummary') }}</p>
          <footer>
              <div class="tag-row">
                <strong v-for="tag in post.tags" :key="tag.id">#{{ tag.name }}</strong>
              </div>
            <span>
              <n-icon :component="MessageCircle" />
              {{ post.commentCount }}
            </span>
          </footer>
        </article>
      </section>
      <section v-else class="empty-blog">
          <strong>{{ t('blog.list.emptyTitle') }}</strong>
      </section>
    </n-spin>
    </section>
  </main>
</template>

<style scoped>
.blog-shell {
  min-height: 100vh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.blog-hero,
.blog-layout {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
}

.blog-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  padding: 98px 0 22px;
}

.blog-hero span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  text-transform: uppercase;
}

.blog-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 24px;
  border-top: 1px solid var(--sw-border);
  padding: 18px 0 72px;
}

.blog-sidebar {
  position: sticky;
  top: 82px;
  display: grid;
  align-content: start;
  gap: 12px;
  height: fit-content;
}

.filter-title,
.selected-filter {
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 800;
}

.category-tabs {
  display: grid;
  gap: 8px;
}

.category-tabs button {
  border: 1px solid var(--sw-border);
  border-radius: 999px;
  background: var(--sw-surface-solid);
  color: var(--sw-muted);
  cursor: pointer;
  padding: 7px 12px;
  text-align: left;
  white-space: nowrap;
}

.category-tabs button.active {
  border-color: #16708f;
  color: #16708f;
}

.post-list {
  display: grid;
  gap: 12px;
}

.post-card {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  cursor: pointer;
  padding: 18px 20px;
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
  font-size: 20px;
  line-height: 1.25;
}

.post-card p {
  margin: 0 0 14px;
  color: var(--sw-muted);
  font-size: 14px;
  line-height: 1.7;
}

.post-card footer {
  justify-content: space-between;
}

.post-card footer strong {
  color: #16708f;
  font-size: 13px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-blog {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  min-height: 320px;
  color: var(--sw-muted);
}

.empty-blog strong {
  color: var(--sw-text);
  font-size: 18px;
}

.blog-skeleton {
  display: grid;
  gap: 12px;
}

.blog-skeleton span {
  height: 116px;
  border-radius: 8px;
  background: linear-gradient(90deg, rgba(143, 162, 172, 0.14), rgba(255, 255, 255, 0.4), rgba(143, 162, 172, 0.14));
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -200% 0;
  }
}

@media (max-width: 760px) {
  .blog-hero,
  .blog-layout {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .blog-hero {
    display: grid;
    padding-top: 92px;
  }

  .blog-sidebar {
    position: static;
  }

  .category-tabs {
    display: flex;
    overflow-x: auto;
  }
}
</style>

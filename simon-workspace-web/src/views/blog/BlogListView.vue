<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useMessage, NButton, NIcon, NInput, NSpin } from 'naive-ui'
import { Edit, Eye, MessageCircle, Refresh, Search, Tags } from '@vicons/tabler'

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

function selectCategory(categoryId: string | null) {
  selectedCategory.value = categoryId
  void load()
}

function openPost(post: BlogPostSummary) {
  void router.push(`/blog/${post.id}`)
}

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : '-'
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
      <aside class="blog-sidebar" :aria-label="t('blog.list.categories')">
        <form class="search-panel" @submit.prevent="load">
          <n-input v-model:value="keyword" clearable :placeholder="t('blog.list.searchPlaceholder')">
            <template #prefix>
              <n-icon :component="Search" />
            </template>
          </n-input>
          <n-button circle secondary attr-type="submit" :aria-label="t('common.actions.search')">
            <template #icon>
              <n-icon :component="Search" />
            </template>
          </n-button>
        </form>

        <div class="category-card">
          <div class="filter-title">
            <n-icon :component="Tags" />
            <span>{{ selectedCategoryName }}</span>
          </div>
          <div class="category-tabs">
            <button :class="{ active: selectedCategory === null }" @click="selectCategory(null)">
              <span>{{ t('blog.list.all') }}</span>
              <strong class="category-count">{{ posts.length }}</strong>
            </button>
            <button
              v-for="category in categories"
              :key="category.id"
              :class="{ active: selectedCategory === category.id }"
              @click="selectCategory(category.id)"
            >
              <span>{{ category.name }}</span>
              <strong class="category-count">{{ category.postCount ?? 0 }}</strong>
            </button>
          </div>
        </div>

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

        <template v-else-if="posts.length">
          <section class="post-list">
            <article
              v-for="post in posts"
              :key="post.id"
              class="post-card"
              tabindex="0"
              @click="openPost(post)"
              @keydown.enter.prevent="openPost(post)"
              @keydown.space.prevent="openPost(post)"
            >
              <h2>{{ post.title }}</h2>
              <p>{{ post.summary || t('blog.list.noSummary') }}</p>
              <footer>
                <div class="post-taxonomy">
                  <span class="post-category">{{ post.category?.name || t('blog.list.uncategorized') }}</span>
                  <div class="tag-row">
                    <strong v-for="tag in post.tags.slice(0, 3)" :key="tag.id">#{{ tag.name }}</strong>
                  </div>
                </div>
                <span class="post-stats">
                  <n-icon :component="Eye" />
                  {{ post.viewCount }}
                  <n-icon :component="MessageCircle" />
                  {{ post.commentCount }}
                  <time>{{ formatDate(post.publishedTime) }}</time>
                </span>
              </footer>
            </article>
          </section>
        </template>

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
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
}

.blog-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  padding: 10px 0 18px;
}

.blog-hero span {
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.blog-layout {
  display: grid;
  grid-template-columns: 264px minmax(0, 1fr);
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

.search-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 38px;
  gap: 8px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  padding: 8px;
  box-shadow: var(--sw-shadow-soft);
  backdrop-filter: blur(18px);
}

.category-card {
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  box-shadow: var(--sw-shadow-soft);
  padding: 10px;
  backdrop-filter: blur(18px);
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 800;
  padding: 4px 6px 10px;
}

.filter-title .n-icon {
  color: var(--sw-accent);
  font-size: 17px;
}

.category-tabs {
  display: grid;
  gap: 6px;
}

.category-tabs button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--sw-muted);
  cursor: pointer;
  padding: 9px 10px;
  text-align: left;
  transition:
    background-color var(--sw-motion-standard),
    border-color var(--sw-motion-standard),
    color var(--sw-motion-standard),
    transform var(--sw-motion-standard);
}

.category-tabs button:hover {
  background: var(--sw-surface-muted);
  color: var(--sw-text);
  transform: translate3d(2px, 0, 0);
}

.category-tabs button.active {
  border-color: color-mix(in srgb, var(--sw-accent) 34%, transparent);
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
}

.category-tabs button:active {
  transform: translate3d(2px, 1px, 0);
}

.category-count {
  display: inline-grid;
  min-width: 28px;
  height: 22px;
  place-items: center;
  border-radius: 999px;
  background: var(--sw-surface-solid);
  color: inherit;
  font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
}

.blog-content {
  min-width: 0;
}

.post-list {
  display: grid;
  gap: 12px;
}

.post-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  box-shadow: var(--sw-shadow-soft);
  cursor: pointer;
  padding: 18px 20px;
  transition:
    background-color var(--sw-motion-standard),
    border-color var(--sw-motion-standard),
    box-shadow var(--sw-motion-standard),
    transform var(--sw-motion-standard);
}

.post-card::before {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, color-mix(in srgb, var(--sw-accent) 13%, transparent), transparent 42%);
  content: "";
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--sw-motion-standard);
}

.post-card:hover,
.post-card:focus-visible {
  border-color: color-mix(in srgb, var(--sw-accent) 42%, var(--sw-border));
  background: var(--sw-panel-bg-hover);
  box-shadow: var(--sw-shadow);
  transform: translate3d(0, -3px, 0);
}

.post-card:hover::before,
.post-card:focus-visible::before {
  opacity: 1;
}

.post-card:active {
  transform: translate3d(0, -1px, 0) scale(0.995);
}

.post-card > * {
  position: relative;
}

.post-card footer,
.post-stats,
.post-taxonomy {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--sw-muted);
  font-size: 13px;
}

.post-category {
  color: var(--sw-accent);
  font-weight: 800;
  white-space: nowrap;
}

.post-card h2 {
  margin: 0 0 8px;
  color: var(--sw-text);
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0;
  line-height: 1.22;
}

.post-card p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0 0 16px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--sw-muted);
  font-size: 14px;
  line-height: 1.65;
}

.post-card footer {
  justify-content: space-between;
}

.post-taxonomy {
  flex-wrap: wrap;
  min-width: 0;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.tag-row strong {
  border-radius: 999px;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  padding: 4px 9px;
  font-size: 12px;
  font-weight: 800;
}

.post-stats {
  flex-shrink: 0;
  font-family: "JetBrains Mono", "SFMono-Regular", Consolas, monospace;
}

.post-stats time {
  color: var(--sw-faint);
}

.empty-blog {
  display: grid;
  min-height: 360px;
  place-items: center;
  align-content: center;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg-strong);
  color: var(--sw-muted);
  box-shadow: var(--sw-shadow-soft);
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
  height: 126px;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--sw-border-soft), var(--sw-surface-muted), var(--sw-border-soft));
  background-size: 200% 100%;
  animation: sw-shimmer 1.35s ease-in-out infinite;
}

@media (prefers-reduced-motion: reduce) {
  .blog-skeleton span {
    animation: none;
  }

  .post-card,
  .post-card::before,
  .category-tabs button {
    transition: none;
  }
}

@media (max-width: 860px) {
  .blog-hero,
  .blog-layout {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .blog-hero {
    display: grid;
    padding-top: 10px;
  }

  .blog-sidebar {
    position: static;
  }

  .category-tabs {
    display: flex;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .category-tabs button {
    min-width: max-content;
  }
}

@media (max-width: 620px) {
  .blog-hero,
  .blog-layout {
    width: min(100% - 24px, 1180px);
  }

  .post-card footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

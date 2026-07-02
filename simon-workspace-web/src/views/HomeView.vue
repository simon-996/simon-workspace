<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NIcon, NSpin } from 'naive-ui'
import {
  Book,
  Briefcase,
  ChevronRight,
  CircleCheck,
  Files,
  Mail,
  Menu2,
  UserCircle,
} from '@vicons/tabler'

import { fetchPublicSiteConfig, type SiteConfig } from '../api/site'

const navOpen = ref(false)
const loading = ref(false)
const error = ref('')
const site = ref<SiteConfig>({
  id: 'fallback',
  siteTitle: 'Simon Workspace',
  ownerName: 'Simon',
  heroTitle: '个人主页、博客和教学工作台',
  heroSubtitle: '记录教学、开发和项目实践；公开页面给访客阅读，工作台留给授权账号使用。',
  ownerRole: '软件教师 / 独立开发者',
  contactEmail: null,
  githubUrl: 'https://github.com/simon-996',
  profileVisible: true,
  blogVisible: true,
  projectsVisible: true,
  workspaceEntryVisible: false,
})

const visibleSections = computed(() => [
  site.value.profileVisible,
  site.value.blogVisible,
  site.value.projectsVisible,
].filter(Boolean).length)

onMounted(() => {
  void loadSite()
})

async function loadSite() {
  loading.value = true
  error.value = ''
  try {
    site.value = await fetchPublicSiteConfig()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公开配置加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="home-page">
    <nav class="top-nav">
      <a class="brand" href="/">
        <span>S</span>
        {{ site.siteTitle }}
      </a>
      <button class="menu-button" type="button" aria-label="Toggle navigation" @click="navOpen = !navOpen">
        <n-icon :component="Menu2" />
      </button>
      <div class="nav-links" :class="{ open: navOpen }">
        <a v-if="site.profileVisible" href="/#profile">关于</a>
        <a v-if="site.blogVisible" href="/#blog">博客</a>
        <a v-if="site.projectsVisible" href="/#projects">项目</a>
        <router-link v-if="site.workspaceEntryVisible" to="/workspace">工作台</router-link>
      </div>
    </nav>

    <section class="hero-section">
      <div class="hero-copy">
        <p class="eyebrow">{{ site.ownerName }}</p>
        <h1>{{ site.heroTitle }}</h1>
        <p class="summary">{{ site.heroSubtitle }}</p>
        <div class="actions">
          <n-button
            v-if="site.blogVisible"
            class="primary-action"
            type="primary"
            size="large"
            tag="a"
            href="/#blog"
          >
            阅读博客
          </n-button>
          <n-button
            v-if="site.workspaceEntryVisible"
            class="ghost-action"
            ghost
            size="large"
            tag="router-link"
            to="/workspace"
          >
            进入工作台
          </n-button>
        </div>
        <p class="ready-state">
          <span />
          {{ site.ownerRole || 'Personal site' }}
        </p>
      </div>

      <aside class="portrait-panel" aria-label="站点概览">
        <n-spin :show="loading">
          <div class="portrait-card">
            <div class="portrait-mark">
              <n-icon :component="UserCircle" />
            </div>
            <div>
              <span>Owner</span>
              <strong>{{ site.ownerName }}</strong>
              <p>{{ site.ownerRole || '个人主页维护者' }}</p>
            </div>
            <ul>
              <li>
                <n-icon :component="CircleCheck" />
                <span>{{ visibleSections }} 个公开模块</span>
              </li>
              <li v-if="site.contactEmail">
                <n-icon :component="Mail" />
                <a :href="`mailto:${site.contactEmail}`">{{ site.contactEmail }}</a>
              </li>
              <li v-if="site.githubUrl">
                <n-icon :component="Files" />
                <a :href="site.githubUrl" target="_blank" rel="noreferrer">GitHub</a>
              </li>
            </ul>
            <p v-if="error" class="load-note">{{ error }}，当前显示默认内容。</p>
          </div>
        </n-spin>
      </aside>
    </section>

    <section class="public-grid" aria-label="公开内容入口">
      <article v-if="site.profileVisible" id="profile">
        <n-icon :component="UserCircle" />
        <span>Profile</span>
        <h2>关于 {{ site.ownerName }}</h2>
        <p>这里会承载个人介绍、教学方向、技术栈和公开联系方式。当前先提供可配置骨架，后续内容可以继续细化。</p>
      </article>

      <article v-if="site.blogVisible" id="blog">
        <n-icon :component="Book" />
        <span>Blog</span>
        <h2>博客入口</h2>
        <p>后续文章发布后，访客会在这里看到已公开的技术笔记、教学复盘和项目记录。</p>
      </article>

      <article v-if="site.projectsVisible" id="projects">
        <n-icon :component="Briefcase" />
        <span>Projects</span>
        <h2>项目展示</h2>
        <p>这里会展示公开项目、作品说明和链接。隐藏开关关闭后，访客不会看到这个模块。</p>
      </article>
    </section>

    <section class="closing-band">
      <div>
        <p class="section-kicker">Access</p>
        <h2>公开内容给访客，工作台给授权账号。</h2>
      </div>
      <router-link to="/login" class="login-link">
        登录
        <n-icon :component="ChevronRight" />
      </router-link>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100dvh;
  overflow-x: hidden;
  background:
    linear-gradient(90deg, rgba(32, 164, 216, 0.06) 1px, transparent 1px),
    linear-gradient(180deg, rgba(33, 70, 92, 0.06) 1px, transparent 1px),
    #f6f8fa;
  background-size: 104px 104px;
  color: #14202b;
}

.top-nav {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 28px 0 18px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #14202b;
  font-size: 15px;
  font-weight: 800;
}

.brand span {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #1688b9;
  color: #ffffff;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 26px;
  color: #5c6d7b;
  font-size: 13px;
  font-weight: 800;
}

.nav-links a {
  transition: color 180ms ease;
}

.nav-links a:hover {
  color: #1688b9;
}

.menu-button {
  display: none;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  color: #14202b;
  cursor: pointer;
  font-size: 24px;
  padding: 8px 10px;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 0.92fr) minmax(360px, 0.68fr);
  align-items: center;
  gap: 72px;
  width: min(1180px, calc(100% - 48px));
  min-height: calc(100dvh - 96px);
  margin: 0 auto;
  padding: 42px 0 64px;
}

.hero-copy {
  max-width: 720px;
}

.eyebrow,
.section-kicker,
.portrait-card span,
.public-grid article > span {
  color: #1688b9;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1 {
  max-width: 760px;
  margin: 18px 0 0;
  color: #111a23;
  font-size: clamp(42px, 6.2vw, 84px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1.02;
}

.summary {
  max-width: 650px;
  margin: 26px 0 0;
  color: #516271;
  font-size: 18px;
  line-height: 1.7;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.actions :deep(.n-button) {
  --n-border-radius: 8px !important;
  min-width: 156px;
  height: 48px;
  font-weight: 800;
}

.ghost-action {
  color: #1688b9;
}

.ready-state {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 22px 0 0;
  color: #526574;
  font-size: 14px;
  font-weight: 700;
}

.ready-state span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #4b9f6b;
}

.portrait-panel {
  min-width: 0;
}

.portrait-card {
  display: grid;
  gap: 22px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 28px;
  box-shadow: 0 20px 42px rgba(44, 74, 94, 0.08);
}

.portrait-mark {
  display: grid;
  place-items: center;
  width: 92px;
  height: 92px;
  border-radius: 24px;
  background: #e7f5fb;
  color: #1688b9;
  font-size: 58px;
}

.portrait-card strong {
  display: block;
  margin-top: 6px;
  color: #111a23;
  font-size: 28px;
  font-weight: 800;
}

.portrait-card p {
  margin: 8px 0 0;
  color: #647586;
  line-height: 1.55;
}

.portrait-card ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.portrait-card li {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-height: 32px;
  color: #405363;
  font-size: 14px;
  font-weight: 700;
}

.portrait-card li .n-icon {
  color: #1688b9;
  font-size: 20px;
}

.load-note {
  border-top: 1px solid #e4ebf0;
  padding-top: 14px;
  color: #9a642c !important;
  font-size: 13px;
}

.public-grid,
.closing-band {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.public-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.92fr 0.92fr;
  gap: 12px;
  padding-bottom: 26px;
}

.public-grid article {
  display: grid;
  align-content: start;
  min-height: 230px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 24px;
}

.public-grid article > .n-icon {
  color: #1688b9;
  font-size: 32px;
}

.public-grid h2 {
  margin: 24px 0 0;
  color: #111a23;
  font-size: 24px;
  font-weight: 800;
}

.public-grid p {
  margin: 12px 0 0;
  color: #647586;
  font-size: 15px;
  line-height: 1.65;
}

.closing-band {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  border-top: 1px solid #d8e0e7;
  padding: 32px 0 56px;
}

.closing-band h2 {
  max-width: 620px;
  margin: 8px 0 0;
  color: #111a23;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.25;
}

.login-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  border: 1px solid #bfd0dc;
  border-radius: 8px;
  color: #1688b9;
  padding: 0 16px;
  font-weight: 800;
}

@media (max-width: 900px) {
  .top-nav {
    width: min(100% - 32px, 560px);
  }

  .menu-button {
    display: inline-flex;
  }

  .nav-links {
    position: absolute;
    top: 78px;
    right: 0;
    left: 0;
    z-index: 2;
    display: none;
    border: 1px solid #d8e0e7;
    border-radius: 8px;
    background: #ffffff;
    padding: 16px;
  }

  .nav-links.open {
    display: grid;
    gap: 14px;
  }

  .hero-section,
  .public-grid,
  .closing-band {
    width: min(100% - 32px, 560px);
  }

  .hero-section {
    grid-template-columns: 1fr;
    gap: 28px;
    min-height: auto;
    padding: 38px 0;
  }

  h1 {
    font-size: clamp(34px, 11vw, 48px);
  }

  .summary {
    font-size: 16px;
  }

  .actions {
    display: grid;
  }

  .actions :deep(.n-button) {
    width: 100%;
  }

  .public-grid {
    grid-template-columns: 1fr;
  }

  .closing-band {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import { Menu2 } from '@vicons/tabler'

import TerminalPanel from '../components/TerminalPanel.vue'
import { fetchPublicSiteConfig, type SiteConfig } from '../api/site'

const navOpen = ref(false)
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

onMounted(() => {
  void loadSite()
})

async function loadSite() {
  error.value = ''
  try {
    site.value = await fetchPublicSiteConfig()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公开配置加载失败'
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
        <a v-if="site.profileVisible" href="#about">About</a>
        <a v-if="site.blogVisible" href="#blog">Blog</a>
        <a v-if="site.projectsVisible" href="#projects">Projects</a>
        <router-link to="/login">Login</router-link>
        <router-link v-if="site.workspaceEntryVisible" to="/workspace">Workspace</router-link>
      </div>
    </nav>

    <section class="hero-section">
      <section class="intro-column" aria-label="个人主页">
        <p class="eyebrow">{{ site.ownerRole || 'Personal homepage' }}</p>
        <h1>{{ site.ownerName }}</h1>
        <p class="summary">{{ site.heroSubtitle || site.heroTitle }}</p>

        <div class="link-row" aria-label="主要入口">
          <n-button v-if="site.blogVisible" text tag="a" href="#blog">Blog</n-button>
          <n-button v-if="site.projectsVisible" text tag="a" href="#projects">Projects</n-button>
          <n-button v-if="site.githubUrl" text tag="a" :href="site.githubUrl" target="_blank" rel="noreferrer">
            GitHub
          </n-button>
          <n-button text tag="router-link" to="/login">Login</n-button>
        </div>

        <p v-if="error" class="load-note">{{ error }}，当前显示默认内容。</p>
      </section>

      <TerminalPanel />
    </section>

    <section class="quiet-links" aria-label="公开内容">
      <a v-if="site.profileVisible" id="about" href="#about">
        <span>About</span>
        <strong>{{ site.ownerRole || 'Personal notes' }}</strong>
      </a>
      <a v-if="site.blogVisible" id="blog" href="#blog">
        <span>Blog</span>
        <strong>Notes and essays</strong>
      </a>
      <a v-if="site.projectsVisible" id="projects" href="#projects">
        <span>Projects</span>
        <strong>Work in public</strong>
      </a>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 100dvh;
  overflow-x: hidden;
  background: #f7f8f8;
  color: #17212b;
}

.top-nav {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
  padding: 28px 0 18px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #17212b;
  font-size: 14px;
  font-weight: 800;
}

.brand span {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #1b83a8;
  color: #ffffff;
}

.nav-links,
.link-row {
  display: flex;
  align-items: center;
  gap: 22px;
}

.nav-links {
  color: #667583;
  font-size: 13px;
  font-weight: 800;
}

.nav-links a {
  transition: color 180ms ease;
}

.nav-links a:hover {
  color: #1b83a8;
}

.menu-button {
  display: none;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  color: #17212b;
  cursor: pointer;
  font-size: 22px;
  padding: 8px 10px;
}

.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 0.72fr) minmax(380px, 0.88fr);
  align-items: center;
  gap: 56px;
  width: min(1120px, calc(100% - 48px));
  min-height: calc(100dvh - 96px);
  margin: 0 auto;
  padding: 34px 0 54px;
}

.intro-column {
  display: grid;
  align-content: center;
}

.eyebrow {
  margin: 0;
  color: #1b83a8;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
}

h1 {
  margin: 18px 0 0;
  color: #111a23;
  font-size: clamp(56px, 10vw, 126px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 0.92;
}

.summary {
  max-width: 520px;
  margin: 28px 0 0;
  color: #5c6b78;
  font-size: 17px;
  line-height: 1.7;
}

.link-row {
  flex-wrap: wrap;
  margin-top: 28px;
}

.link-row :deep(.n-button) {
  color: #1b83a8;
  font-size: 14px;
  font-weight: 800;
}

.load-note {
  margin: 20px 0 0;
  color: #9a642c;
  font-size: 13px;
}

.quiet-links {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
  border-top: 1px solid #d8e0e7;
  padding: 22px 0 44px;
}

.quiet-links a {
  display: grid;
  gap: 6px;
  border-left: 1px solid #d8e0e7;
  padding: 8px 22px 8px 18px;
}

.quiet-links a:first-child {
  border-left: 0;
  padding-left: 0;
}

.quiet-links span {
  color: #1b83a8;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.quiet-links strong {
  overflow: hidden;
  color: #17212b;
  font-size: 16px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
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
    top: 76px;
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
  .quiet-links {
    width: min(100% - 32px, 560px);
  }

  .hero-section {
    grid-template-columns: 1fr;
    gap: 28px;
    min-height: auto;
    padding: 38px 0 28px;
  }

  h1 {
    font-size: clamp(48px, 18vw, 86px);
  }

  .summary {
    font-size: 16px;
  }

  .quiet-links {
    grid-template-columns: 1fr;
    padding-bottom: 34px;
  }

  .quiet-links a,
  .quiet-links a:first-child {
    border-top: 1px solid #d8e0e7;
    border-left: 0;
    padding: 14px 0;
  }

  .quiet-links a:first-child {
    border-top: 0;
  }
}
</style>

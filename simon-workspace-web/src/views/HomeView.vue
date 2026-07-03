<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import { NIcon } from 'naive-ui'
import { Menu2 } from '@vicons/tabler'

import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import TerminalPanel from '../components/TerminalPanel.vue'
import { fetchPublicSiteConfig, type SiteConfig } from '../api/site'
import { buildHomeScrollStyle, getHomeScrollProgress } from '../utils/homeScroll'

const { t } = useI18n()
const navOpen = ref(false)
const scrollProgress = ref(0)
const site = ref<SiteConfig>({
  id: 'fallback',
  siteTitle: 'Simon Workspace',
  ownerName: 'Chen Ximeng',
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
const homeStyle = computed(() => buildHomeScrollStyle(scrollProgress.value) as CSSProperties)

let ticking = false

onMounted(() => {
  void loadSite()
  updateScrollProgress()
  window.addEventListener('scroll', requestScrollProgress, { passive: true })
  window.addEventListener('resize', requestScrollProgress)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', requestScrollProgress)
  window.removeEventListener('resize', requestScrollProgress)
})

async function loadSite() {
  try {
    site.value = await fetchPublicSiteConfig()
  } catch {
    // Keep the fallback homepage content when public config is unavailable.
  }
}

function requestScrollProgress() {
  if (ticking) {
    return
  }

  ticking = true
  window.requestAnimationFrame(() => {
    updateScrollProgress()
    ticking = false
  })
}

function updateScrollProgress() {
  const distance = Math.max(360, window.innerHeight * 0.82)
  scrollProgress.value = getHomeScrollProgress(window.scrollY, distance)
}
</script>

<template>
  <main class="home-page" :style="homeStyle">
    <nav class="top-nav" :aria-label="t('home.navAria')">
      <a class="brand" href="/">
        {{ site.siteTitle }}
      </a>
      <div class="nav-controls">
        <div class="nav-links" :class="{ open: navOpen }">
          <a v-if="site.profileVisible" href="#about">{{ t('home.about') }}</a>
          <a v-if="site.blogVisible" href="#blog">{{ t('home.blog') }}</a>
          <a v-if="site.projectsVisible" href="#projects">{{ t('home.projects') }}</a>
          <router-link to="/login">{{ t('home.login') }}</router-link>
          <router-link v-if="site.workspaceEntryVisible" to="/workspace">{{ t('workspace.title') }}</router-link>
        </div>
        <LanguageSwitcher />
        <button class="menu-button" type="button" :aria-label="t('home.menuAria')" @click="navOpen = !navOpen">
          <n-icon :component="Menu2" />
        </button>
      </div>
    </nav>

    <section class="hero-section">
      <section class="intro-column" :aria-label="t('home.pageAria')">
        <p class="intro-kicker">{{ t('home.intro.kicker') }}</p>
        <h1>{{ site.ownerName }}</h1>
        <p class="intro-short">{{ t('home.intro.shortLine') }}</p>
      </section>

      <div class="terminal-stage">
        <TerminalPanel />
      </div>
    </section>

    <section v-if="site.profileVisible" id="about" class="about-section" :aria-label="t('home.about')">
      <div class="about-copy">
        <p class="section-kicker">{{ t('home.intro.kicker') }}</p>
        <h2>{{ t('home.intro.lead') }}</h2>
        <p>{{ t('home.intro.body') }}</p>
        <p>{{ t('home.intro.philosophy') }}</p>
        <a class="contact-link" href="mailto:simon996chen@outlook.com">
          <span>{{ t('home.intro.contactLabel') }}</span>
          <strong>{{ t('home.intro.contactEmail') }}</strong>
        </a>
      </div>

      <div class="tech-grid" :aria-label="t('home.intro.techAria')">
        <article>
          <span>{{ t('home.intro.groups.backendLabel') }}</span>
          <strong>{{ t('home.intro.groups.backendValue') }}</strong>
        </article>
        <article>
          <span>{{ t('home.intro.groups.frontendLabel') }}</span>
          <strong>{{ t('home.intro.groups.frontendValue') }}</strong>
        </article>
        <article>
          <span>{{ t('home.intro.groups.mobileLabel') }}</span>
          <strong>{{ t('home.intro.groups.mobileValue') }}</strong>
        </article>
        <article>
          <span>{{ t('home.intro.groups.focusLabel') }}</span>
          <strong>{{ t('home.intro.groups.focusValue') }}</strong>
        </article>
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 168dvh;
  --home-progress: 0;
  --intro-scale: 1;
  --terminal-offset: 0px;
  --terminal-opacity: 1;
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

.nav-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 22px;
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
  min-height: calc(112dvh - 96px);
  margin: 0 auto;
  padding: 34px 0 64px;
}

.intro-column {
  display: grid;
  align-content: center;
  transform:
    translateX(calc(var(--home-progress) * clamp(0px, 12vw, 160px)))
    translateY(calc(var(--home-progress) * 7vh))
    scale(var(--intro-scale));
  transform-origin: left center;
  transition: transform 120ms linear;
  will-change: transform;
  z-index: 1;
}

.intro-kicker,
.section-kicker {
  margin: 0 0 16px;
  color: #1b83a8;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #111a23;
  font-size: clamp(54px, 9vw, 118px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 0.92;
}

.intro-short {
  max-width: 480px;
  margin: 28px 0 0;
  color: #536675;
  font-size: clamp(18px, 2.2vw, 26px);
  font-weight: 700;
  line-height: 1.55;
}

.terminal-stage {
  transform: translateY(var(--terminal-offset));
  opacity: var(--terminal-opacity);
  transition:
    transform 120ms linear,
    opacity 120ms linear;
  will-change: transform, opacity;
}

.about-section {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 54px;
  align-items: start;
  width: min(1120px, calc(100% - 48px));
  margin: 0 auto;
  border-top: 1px solid #d8e0e7;
  padding: 58px 0 88px;
}

.about-copy {
  display: grid;
  gap: 18px;
}

.about-copy h2 {
  max-width: 720px;
  margin: 0;
  color: #111a23;
  font-size: clamp(30px, 4.5vw, 54px);
  font-weight: 850;
  letter-spacing: 0;
  line-height: 1.05;
}

.about-copy p:not(.section-kicker) {
  max-width: 650px;
  margin: 0;
  color: #536675;
  font-size: 17px;
  line-height: 1.85;
}

.contact-link {
  display: inline-grid;
  justify-self: start;
  gap: 5px;
  margin-top: 8px;
  border-bottom: 1px solid #9ccddd;
  padding-bottom: 8px;
}

.contact-link span,
.tech-grid span {
  color: #668093;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.contact-link strong {
  color: #1b83a8;
  font-size: 16px;
  font-weight: 850;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.tech-grid article {
  display: grid;
  gap: 12px;
  min-height: 132px;
  align-content: space-between;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 20px;
}

.tech-grid strong {
  color: #17212b;
  font-size: 18px;
  font-weight: 850;
  line-height: 1.35;
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
    align-items: stretch;
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
  .about-section {
    width: min(100% - 32px, 560px);
  }

  .hero-section {
    grid-template-columns: 1fr;
    gap: 28px;
    min-height: 108dvh;
    padding: 34px 0 42px;
  }

  .intro-column {
    text-align: center;
    transform: translateY(calc(var(--home-progress) * 8vh)) scale(var(--intro-scale));
    transform-origin: center center;
  }

  .intro-short {
    max-width: 100%;
    margin-top: 22px;
  }

  h1 {
    font-size: clamp(48px, 18vw, 86px);
  }

  .about-section {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 44px 0 64px;
  }

  .about-copy h2 {
    font-size: clamp(28px, 8vw, 42px);
  }

  .tech-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .top-nav {
    padding-top: 20px;
  }

  .brand {
    max-width: 42vw;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .nav-controls {
    gap: 8px;
  }

  .hero-section {
    min-height: 104dvh;
  }

  .intro-kicker {
    margin-bottom: 12px;
  }

  .intro-short {
    font-size: 17px;
  }

  .about-copy p:not(.section-kicker) {
    font-size: 16px;
  }

  .contact-link strong {
    overflow-wrap: anywhere;
  }
}

@media (prefers-reduced-motion: reduce) {
  .intro-column,
  .terminal-stage {
    transform: none;
    opacity: 1;
    transition: none;
  }
}
</style>

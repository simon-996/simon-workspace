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
const viewport = ref({ width: 1200, height: 800 })
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
const homeStyle = computed(() => buildHomeScrollStyle(scrollProgress.value, viewport.value) as CSSProperties)

let ticking = false
let animationFrameId = 0
let targetProgress = 0

onMounted(() => {
  void loadSite()
  updateViewport()
  setScrollTarget(true)
  window.addEventListener('scroll', requestScrollProgress, { passive: true })
  window.addEventListener('resize', requestResizeProgress)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', requestScrollProgress)
  window.removeEventListener('resize', requestResizeProgress)
  if (animationFrameId) {
    window.cancelAnimationFrame(animationFrameId)
  }
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
    setScrollTarget()
    ticking = false
  })
}

function requestResizeProgress() {
  updateViewport()
  setScrollTarget()
}

function setScrollTarget(immediate = false) {
  const distance = Math.max(640, window.innerHeight * 1.15)
  targetProgress = getHomeScrollProgress(window.scrollY, distance)

  if (immediate) {
    scrollProgress.value = targetProgress
    return
  }

  if (!animationFrameId) {
    animationFrameId = window.requestAnimationFrame(animateScrollProgress)
  }
}

function animateScrollProgress() {
  const delta = targetProgress - scrollProgress.value

  if (Math.abs(delta) < 0.001) {
    scrollProgress.value = targetProgress
    animationFrameId = 0
    return
  }

  scrollProgress.value += delta * 0.18
  animationFrameId = window.requestAnimationFrame(animateScrollProgress)
}

function updateViewport() {
  viewport.value = {
    width: window.innerWidth,
    height: window.innerHeight,
  }
}
</script>

<template>
  <main class="home-page" :style="homeStyle">
    <section class="home-scene">
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
        <section id="about" class="intro-column" :aria-label="t('home.pageAria')">
          <p class="intro-kicker">{{ t('home.intro.kicker') }}</p>
          <h1>{{ site.ownerName }}</h1>
          <p class="intro-short">{{ t('home.intro.shortLine') }}</p>

          <div class="intro-details">
            <p class="intro-lead">{{ t('home.intro.lead') }}</p>
            <p>{{ t('home.intro.body') }}</p>
            <p>{{ t('home.intro.philosophy') }}</p>
            <a class="contact-link" href="mailto:simon996chen@outlook.com">
              <span>{{ t('home.intro.contactLabel') }}</span>
              <strong>{{ t('home.intro.contactEmail') }}</strong>
            </a>
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
          </div>
        </section>

        <div class="terminal-stage">
          <TerminalPanel />
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: 225dvh;
  --home-progress: 0;
  --terminal-progress: 0;
  --intro-focus: 0;
  --intro-scale: 1;
  --intro-x: 0px;
  --intro-y: 0px;
  --terminal-x: 0px;
  --terminal-y: 0px;
  --terminal-scale: 1;
  --terminal-opacity: 1;
  --details-opacity: 0;
  --details-y: 20px;
  --details-scale: 1;
  --brief-opacity: 1;
  background: #f7f8f8;
  color: #17212b;
  overscroll-behavior: contain;
}

.home-scene {
  position: fixed;
  inset: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 100dvh;
  height: 100dvh;
  overflow: hidden;
  background:
    radial-gradient(circle at 72% 42%, rgba(27, 131, 168, 0.08), transparent 28%),
    linear-gradient(180deg, #fbfcfc 0%, #f7f8f8 58%, #eef3f5 100%);
}

.top-nav {
  position: relative;
  z-index: 4;
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
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 0.72fr) minmax(380px, 0.88fr);
  align-items: center;
  gap: 56px;
  width: min(1120px, calc(100% - 48px));
  height: 100%;
  min-height: 0;
  margin: 0 auto;
  padding: clamp(18px, 3dvh, 34px) 0 7dvh;
}

.intro-column {
  position: relative;
  z-index: 2;
  display: grid;
  align-content: center;
  max-width: 620px;
  transform: translate3d(var(--intro-x), var(--intro-y), 0) scale(var(--intro-scale));
  transform-origin: left center;
  will-change: transform;
}

.intro-kicker {
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
  font-size: clamp(52px, 8vw, 104px);
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
  opacity: var(--brief-opacity);
  will-change: opacity;
}

.intro-details {
  position: absolute;
  top: calc(100% - 130px);
  left: 0;
  display: grid;
  gap: 10px;
  width: min(820px, 80vw);
  opacity: var(--details-opacity);
  transform: translate3d(0, var(--details-y), 0) scale(var(--details-scale));
  transform-origin: left top;
  will-change: opacity, transform;
}

.intro-details p {
  max-width: 820px;
  margin: 0;
  color: #536675;
  font-size: 13px;
  line-height: 1.58;
}

.intro-details .intro-lead {
  color: #17212b;
  font-size: clamp(17px, 1.7vw, 22px);
  font-weight: 850;
  line-height: 1.35;
}

.terminal-stage {
  z-index: 1;
  transform: translate3d(var(--terminal-x), var(--terminal-y), 0) scale(var(--terminal-scale));
  transform-origin: center center;
  opacity: var(--terminal-opacity);
  will-change: transform, opacity;
}

.contact-link {
  display: inline-grid;
  justify-self: start;
  gap: 5px;
  margin-top: 2px;
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 16px;
  margin-top: 0;
}

.tech-grid article {
  display: grid;
  gap: 6px;
  align-content: start;
  border-top: 1px solid #d8e0e7;
  padding-top: 10px;
}

.tech-grid strong {
  color: #17212b;
  font-size: 14px;
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

  .hero-section {
    width: min(100% - 32px, 560px);
  }

  .hero-section {
    grid-template-columns: 1fr;
    align-content: center;
    gap: 20px;
    padding: 18px 0 8dvh;
  }

  .intro-column {
    justify-items: center;
    max-width: 100%;
    text-align: center;
    transform: translate3d(0, var(--intro-y), 0) scale(var(--intro-scale));
    transform-origin: center center;
  }

  .intro-short {
    max-width: 100%;
    margin-top: 22px;
  }

  h1 {
    font-size: clamp(48px, 18vw, 86px);
  }

  .intro-details {
    right: 0;
    left: 0;
    width: 100%;
    transform: translate3d(0, var(--details-y), 0) scale(var(--details-scale));
    transform-origin: center top;
  }

  .tech-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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
    width: min(100% - 28px, 520px);
  }

  .intro-kicker {
    margin-bottom: 12px;
  }

  .intro-short {
    font-size: 17px;
  }

  .intro-details {
    top: calc(100% + 20px);
    gap: 10px;
  }

  .intro-details p {
    font-size: 13px;
    line-height: 1.58;
  }

  .intro-details .intro-lead {
    font-size: 16px;
  }

  .contact-link strong {
    overflow-wrap: anywhere;
  }
}

@media (prefers-reduced-motion: reduce) {
  .intro-column,
  .terminal-stage,
  .intro-details {
    transform: none;
    opacity: 1;
    transition: none;
  }

  .intro-details {
    position: static;
    margin-top: 22px;
  }
}
</style>

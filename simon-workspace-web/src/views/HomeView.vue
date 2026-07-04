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
const site = ref<SiteConfig | null>(null)
const siteLoadFailed = ref(false)
const homeStyle = computed(() => buildHomeScrollStyle(scrollProgress.value, viewport.value) as CSSProperties)
const introKicker = computed(() => t('home.intro.kicker').trim())
const contactLabel = computed(() => t('home.intro.contactLabel').trim())

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
  siteLoadFailed.value = false
  try {
    site.value = await fetchPublicSiteConfig()
  } catch {
    siteLoadFailed.value = true
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
          <span v-if="site">{{ site.siteTitle }}</span>
          <span v-else class="skeleton-line brand-skeleton" aria-hidden="true"></span>
        </a>
        <div class="nav-controls">
          <div v-if="site" class="nav-links" :class="{ open: navOpen }">
            <a v-if="site.profileVisible" href="#about">{{ t('home.about') }}</a>
            <a v-if="site.blogVisible" href="#blog">{{ t('home.blog') }}</a>
            <a v-if="site.projectsVisible" href="#projects">{{ t('home.projects') }}</a>
            <router-link to="/login">{{ t('home.login') }}</router-link>
            <router-link v-if="site.workspaceEntryVisible" to="/workspace">{{ t('workspace.title') }}</router-link>
          </div>
          <div v-else class="nav-links nav-links-loading" :class="{ open: navOpen }" aria-hidden="true">
            <span class="skeleton-line nav-skeleton"></span>
            <span class="skeleton-line nav-skeleton short"></span>
            <span class="skeleton-line nav-skeleton"></span>
          </div>
          <LanguageSwitcher />
          <button class="menu-button" type="button" :aria-label="t('home.menuAria')" @click="navOpen = !navOpen">
            <n-icon :component="Menu2" />
          </button>
        </div>
      </nav>

      <section class="hero-section">
        <section id="about" class="intro-column" :aria-label="t('home.pageAria')">
          <template v-if="site">
            <p v-if="introKicker" class="intro-kicker">{{ introKicker }}</p>
            <h1>{{ site.ownerName }}</h1>
            <p class="intro-short">{{ t('home.intro.shortLine') }}</p>

            <div class="intro-details">
              <p class="intro-lead">{{ t('home.intro.lead') }}</p>
              <p>{{ t('home.intro.body') }}</p>
              <p>{{ t('home.intro.philosophy') }}</p>
              <a class="contact-link" href="mailto:simon996chen@outlook.com">
                <span v-if="contactLabel">{{ contactLabel }}</span>
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
          </template>

          <div v-else class="intro-skeleton" :class="{ failed: siteLoadFailed }">
            <span class="skeleton-line skeleton-kicker" aria-hidden="true"></span>
            <span class="skeleton-line skeleton-title" aria-hidden="true"></span>
            <span class="skeleton-line skeleton-title narrow" aria-hidden="true"></span>
            <div class="skeleton-brief" aria-hidden="true">
              <span class="skeleton-line"></span>
              <span class="skeleton-line medium"></span>
              <span class="skeleton-line short"></span>
            </div>
            <div class="intro-skeleton-details" aria-hidden="true">
              <span class="skeleton-line detail-title"></span>
              <span class="skeleton-line"></span>
              <span class="skeleton-line medium"></span>
              <span class="skeleton-line short"></span>
              <div class="skeleton-tech-grid">
                <span class="skeleton-line"></span>
                <span class="skeleton-line"></span>
                <span class="skeleton-line"></span>
                <span class="skeleton-line"></span>
              </div>
            </div>
            <p v-if="siteLoadFailed" class="load-note">{{ t('home.configLoadFailed') }}</p>
          </div>
        </section>

        <div class="terminal-stage">
          <TerminalPanel v-if="site" />
          <div v-else class="terminal-skeleton" aria-hidden="true">
            <div class="terminal-skeleton-header">
              <span class="skeleton-line terminal-title"></span>
            </div>
            <div class="terminal-skeleton-body">
              <span class="skeleton-line terminal-line wide"></span>
              <span class="skeleton-line terminal-line"></span>
              <span class="skeleton-line terminal-line short"></span>
            </div>
          </div>
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
  --intro-details-gap: 28px;
  --intro-details-lift: clamp(96px, 10vw, 124px);
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

.skeleton-line {
  display: block;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    rgba(190, 203, 213, 0.34) 0%,
    rgba(255, 255, 255, 0.86) 48%,
    rgba(190, 203, 213, 0.34) 100%
  );
  background-size: 220% 100%;
  animation: skeleton-breathe 2.6s ease-in-out infinite;
}

.brand-skeleton {
  width: 136px;
  height: 16px;
}

.nav-links-loading {
  pointer-events: none;
}

.nav-skeleton {
  width: 56px;
  height: 13px;
}

.nav-skeleton.short {
  width: 42px;
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

.intro-skeleton {
  position: relative;
  display: grid;
  align-content: center;
  width: min(620px, 100%);
}

.skeleton-kicker {
  width: 172px;
  height: 14px;
  margin-bottom: 18px;
}

.skeleton-title {
  width: min(500px, 70vw);
  height: clamp(58px, 8vw, 104px);
  border-radius: 18px;
}

.skeleton-title.narrow {
  width: min(310px, 48vw);
  height: clamp(42px, 6vw, 76px);
  margin-top: 10px;
}

.skeleton-brief {
  display: grid;
  gap: 14px;
  width: min(480px, 100%);
  margin-top: 30px;
  opacity: var(--brief-opacity);
  will-change: opacity;
}

.skeleton-brief .skeleton-line {
  height: 22px;
}

.skeleton-brief .medium {
  width: 82%;
}

.skeleton-brief .short {
  width: 58%;
}

.intro-skeleton-details {
  position: absolute;
  top: calc(100% + var(--intro-details-gap) - var(--intro-details-lift));
  left: 0;
  display: grid;
  gap: 10px;
  width: min(820px, 80vw);
  opacity: var(--details-opacity);
  transform: translate3d(0, var(--details-y), 0) scale(var(--details-scale));
  transform-origin: left top;
  will-change: opacity, transform;
}

.intro-skeleton-details .skeleton-line {
  height: 13px;
}

.intro-skeleton-details .detail-title {
  width: 72%;
  height: 24px;
}

.intro-skeleton-details .medium {
  width: 82%;
}

.intro-skeleton-details .short {
  width: 58%;
}

.skeleton-tech-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 16px;
  margin-top: 4px;
}

.skeleton-tech-grid .skeleton-line {
  height: 44px;
  border-radius: 8px;
}

.load-note {
  margin: 16px 0 0;
  color: #9a642c;
  font-size: 13px;
  font-weight: 800;
}

.intro-details {
  position: absolute;
  top: calc(100% + var(--intro-details-gap) - var(--intro-details-lift));
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

.terminal-skeleton {
  min-height: 410px;
  overflow: hidden;
  border: 1px solid #22303a;
  border-radius: 8px;
  background: #101923;
  box-shadow: 0 24px 70px rgba(17, 26, 35, 0.16);
}

.terminal-skeleton .skeleton-line {
  background: linear-gradient(
    90deg,
    rgba(76, 95, 110, 0.36) 0%,
    rgba(129, 156, 174, 0.58) 48%,
    rgba(76, 95, 110, 0.36) 100%
  );
  background-size: 220% 100%;
}

.terminal-skeleton-header {
  display: flex;
  align-items: center;
  height: 64px;
  border-bottom: 1px solid #22303a;
  padding: 0 22px;
}

.terminal-title {
  width: 112px;
  height: 16px;
}

.terminal-skeleton-body {
  display: grid;
  gap: 16px;
  padding: 96px 24px 24px;
}

.terminal-line {
  width: 60%;
  height: 16px;
}

.terminal-line.wide {
  width: 82%;
}

.terminal-line.short {
  width: 42%;
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
  .home-page {
    --intro-details-gap: 24px;
    --intro-details-lift: 78px;
  }

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

  .intro-skeleton {
    justify-items: center;
    width: 100%;
  }

  .skeleton-kicker {
    margin-bottom: 14px;
  }

  .skeleton-title {
    width: min(430px, 86vw);
  }

  .skeleton-title.narrow {
    width: min(260px, 60vw);
  }

  .skeleton-brief {
    justify-items: center;
    width: 100%;
    margin-top: 24px;
  }

  .intro-skeleton-details {
    right: 0;
    left: 0;
    width: 100%;
    transform: translate3d(0, var(--details-y), 0) scale(var(--details-scale));
    transform-origin: center top;
  }

  .skeleton-tech-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
  }

  .terminal-skeleton {
    min-height: 280px;
  }

  .terminal-skeleton-body {
    padding-top: 66px;
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

  .contact-link {
    justify-self: center;
    max-width: 100%;
    text-align: center;
  }

  .contact-link strong {
    text-align: center;
    overflow-wrap: anywhere;
  }

  .tech-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .home-page {
    --intro-details-gap: 20px;
    --intro-details-lift: 58px;
  }

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
    gap: 10px;
  }

  .intro-skeleton-details {
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

@keyframes skeleton-breathe {
  0%,
  100% {
    background-position: 0% 50%;
    opacity: 0.46;
  }

  50% {
    background-position: 100% 50%;
    opacity: 0.92;
  }
}

@media (prefers-reduced-motion: reduce) {
  .intro-column,
  .terminal-stage,
  .intro-details,
  .intro-skeleton-details {
    transform: none;
    opacity: 1;
    transition: none;
  }

  .skeleton-line {
    animation: none;
  }

  .intro-details {
    position: static;
    margin-top: 22px;
  }

  .intro-skeleton-details {
    position: static;
    margin-top: 22px;
  }
}
</style>

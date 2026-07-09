<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'

import AppHeader from '../components/AppHeader.vue'
import TerminalPanel from '../components/TerminalPanel.vue'
import { usePublicSiteConfig } from '../composables/usePublicSiteConfig'
import { buildHomeScrollStyle, getHomeScrollProgress } from '../utils/homeScroll'

const { t } = useI18n()
const scrollProgress = ref(0)
const viewport = ref({ width: 1200, height: 800 })
const { site, failed: siteLoadFailed, loadSite } = usePublicSiteConfig()
const defaultContactEmail = 'simon996chen@outlook.com'
const homeStyle = computed(() => buildHomeScrollStyle(scrollProgress.value, viewport.value) as CSSProperties)
const introKicker = computed(() => t('home.intro.kicker').trim())
const introShort = computed(() => site.value?.heroSubtitle?.trim() || t('home.intro.shortLine').trim())
const profileBio = computed(() => site.value?.profileBio?.trim() || t('home.intro.body').trim())
const contactLabel = computed(() => t('home.intro.contactLabel').trim())
const contactEmail = computed(() => site.value?.contactEmail?.trim() || defaultContactEmail)
const contactHref = computed(() => `mailto:${contactEmail.value}`)
const detailsInteractive = computed(() => scrollProgress.value > 0.58)
const techStackItems = computed(() => {
  if (site.value?.techStack?.length) {
    return site.value.techStack
  }
  return [
    { label: t('home.intro.groups.backendLabel'), value: t('home.intro.groups.backendValue') },
    { label: t('home.intro.groups.frontendLabel'), value: t('home.intro.groups.frontendValue') },
    { label: t('home.intro.groups.mobileLabel'), value: t('home.intro.groups.mobileValue') },
    { label: t('home.intro.groups.focusLabel'), value: t('home.intro.groups.focusValue') },
  ]
})

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
  <main class="home-page" :class="{ 'details-active': detailsInteractive }" :style="homeStyle">
    <section class="home-scene">
      <AppHeader />

      <section class="hero-section">
        <section id="about" class="intro-column" :aria-label="t('home.pageAria')">
          <template v-if="site">
            <p v-if="introKicker" class="intro-kicker">{{ introKicker }}</p>
            <h1>{{ site.ownerName }}</h1>
            <p v-if="introShort" class="intro-short">{{ introShort }}</p>

            <div class="intro-details">
              <p v-if="profileBio" class="profile-bio">{{ profileBio }}</p>
              <a class="contact-link" :href="contactHref">
                <span v-if="contactLabel">{{ contactLabel }}</span>
                <strong>{{ contactEmail }}</strong>
              </a>
              <div class="tech-grid" :aria-label="t('home.intro.techAria')">
                <article v-for="item in techStackItems" :key="`${item.label}-${item.value}`">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
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
          <TerminalPanel v-if="site" auto-focus />
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
  --brief-opacity: 1;
  background: var(--sw-bg);
  color: var(--sw-text);
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
  background: var(--sw-page-bg);
}

.skeleton-line {
  display: block;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    rgba(190, 203, 213, 0.34) 0%,
    var(--sw-surface-solid) 48%,
    rgba(190, 203, 213, 0.34) 100%
  );
  background-size: 220% 100%;
  animation: skeleton-breathe 2.6s ease-in-out infinite;
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
  padding: clamp(16px, 3dvh, 30px) 0 7dvh;
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
  color: var(--sw-accent);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: var(--sw-text);
  font-size: 84px;
  font-weight: 800;
  letter-spacing: 0;
  line-height: 0.92;
}

.intro-short {
  max-width: 480px;
  margin: 24px 0 0;
  color: var(--sw-muted);
  font-size: 22px;
  font-weight: 700;
  line-height: 1.48;
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
  top: calc(100% + var(--intro-details-gap));
  left: 0;
  display: grid;
  gap: 18px;
  width: min(560px, 80vw);
  opacity: var(--details-opacity);
  pointer-events: none;
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
  color: var(--sw-warning);
  font-size: 13px;
  font-weight: 800;
}

.intro-details {
  position: absolute;
  top: calc(100% + var(--intro-details-gap));
  left: 0;
  display: grid;
  gap: 10px;
  width: min(820px, 80vw);
  opacity: var(--details-opacity);
  pointer-events: none;
  transform: translate3d(0, var(--details-y), 0) scale(var(--details-scale));
  transform-origin: left top;
  will-change: opacity, transform;
}

.home-page.details-active .intro-details {
  pointer-events: auto;
}

.profile-bio {
  max-width: min(620px, 100%);
  margin: 0;
  color: var(--sw-muted);
  font-size: 15px;
  font-weight: 650;
  line-height: 1.75;
}

.terminal-stage {
  z-index: 1;
  pointer-events: auto;
  transform: translate3d(var(--terminal-x), var(--terminal-y), 0) scale(var(--terminal-scale));
  transform-origin: center center;
  opacity: var(--terminal-opacity);
  filter: saturate(1.02);
  will-change: transform, opacity;
}

.home-page.details-active .terminal-stage {
  pointer-events: none;
}

.terminal-skeleton {
  min-height: 410px;
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  box-shadow: var(--sw-shadow);
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
  border-bottom: 1px solid var(--sw-border);
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
  display: inline-flex;
  align-items: center;
  justify-self: start;
  gap: 10px;
  margin-top: 2px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-glass);
  box-shadow: var(--sw-shadow-soft);
  padding: 10px 12px;
  backdrop-filter: blur(16px);
  transition:
    border-color var(--sw-motion-standard),
    background-color var(--sw-motion-standard),
    transform var(--sw-motion-standard);
}

.contact-link:hover {
  border-color: color-mix(in srgb, var(--sw-accent) 32%, var(--sw-border));
  background: var(--sw-panel-bg-hover);
  transform: translate3d(0, -1px, 0);
}

.contact-link span,
.tech-grid span {
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.contact-link strong {
  color: var(--sw-accent);
  font-size: 14px;
  font-weight: 850;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-border-soft);
  box-shadow: var(--sw-shadow-soft);
  margin-top: 0;
}

.tech-grid article {
  display: grid;
  gap: 6px;
  align-content: start;
  background: var(--sw-panel-bg-strong);
  padding: 12px;
}

.tech-grid strong {
  color: var(--sw-text);
  font-size: 14px;
  font-weight: 850;
  line-height: 1.35;
}

@media (max-width: 900px) {
  .home-page {
    --intro-details-gap: 24px;
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
    font-size: 64px;
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
    gap: 12px;
  }

  .intro-skeleton-details {
    gap: 10px;
  }

  h1 {
    font-size: 48px;
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

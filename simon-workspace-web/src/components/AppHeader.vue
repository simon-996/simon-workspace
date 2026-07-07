<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { NAvatar, NButton, NDropdown, NIcon } from 'naive-ui'
import { ChevronDown, Menu2, Moon, Sun } from '@vicons/tabler'

import AccountCenterModal from './AccountCenterModal.vue'
import LanguageSwitcher from './LanguageSwitcher.vue'
import { usePublicSiteConfig } from '../composables/usePublicSiteConfig'
import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import { buildHeaderNavItems } from '../utils/appHeaderNav'
import { resolveAvatarUrl } from '../utils/avatarUrl'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()
const navOpen = ref(false)
const accountCenterOpen = ref(false)
const { site, loading, loadSite } = usePublicSiteConfig()

onMounted(() => {
  void loadSite()
  void auth.restore()
})

watch(
  () => route.fullPath,
  () => {
    navOpen.value = false
  },
)

const brandTitle = computed(() => site.value?.siteTitle?.trim() || 'Simon')
const navItems = computed(() =>
  buildHeaderNavItems({
    path: route.path,
    name: route.name,
    hash: route.hash,
    authenticated: auth.isAuthenticated,
    site: site.value,
  }),
)
const showSkeleton = computed(() => loading.value && !site.value)
const accountLabel = computed(() =>
  auth.user?.nickname?.trim() || auth.user?.username?.trim() || t('common.account'),
)
const accountInitial = computed(() => accountLabel.value.slice(0, 1).toUpperCase())
const accountAvatarUrl = computed(() => resolveAvatarUrl(auth.user?.avatarUrl))
const accountOptions = computed(() => [
  {
    label: t('account.menu.profile'),
    key: 'profile',
  },
  {
    label: t('workspace.logout'),
    key: 'logout',
  },
])

async function selectAccountAction(key: string | number) {
  const action = String(key)
  navOpen.value = false

  if (action === 'profile') {
    accountCenterOpen.value = true
    return
  }

  if (action === 'logout') {
    await auth.logout()
    if (route.path.startsWith('/workspace')) {
      await router.replace('/')
    }
  }
}
</script>

<template>
  <header class="app-header">
    <div class="app-header-inner">
      <RouterLink class="app-brand" to="/">
        <span v-if="!showSkeleton">{{ brandTitle }}</span>
        <span v-else class="header-skeleton brand-skeleton" aria-hidden="true"></span>
      </RouterLink>

      <div class="app-header-controls">
        <nav class="app-nav" :class="{ open: navOpen }" :aria-label="t('home.navAria')">
          <template v-if="!showSkeleton">
            <template v-for="item in navItems" :key="item.key">
              <span
                v-if="item.disabled"
                class="app-nav-link active"
                aria-current="page"
              >
                {{ t(item.labelKey) }}
              </span>
              <RouterLink v-else class="app-nav-link" :to="item.to">
                {{ t(item.labelKey) }}
              </RouterLink>
            </template>
          </template>
          <template v-else>
            <span class="header-skeleton nav-skeleton" aria-hidden="true"></span>
            <span class="header-skeleton nav-skeleton short" aria-hidden="true"></span>
            <span class="header-skeleton nav-skeleton" aria-hidden="true"></span>
          </template>
        </nav>

        <button
          class="theme-toggle"
          type="button"
          :class="{ dark: theme.isDark }"
          aria-label="Toggle theme"
          @click="theme.toggleTheme"
        >
          <span class="theme-toggle-track">
            <span class="theme-toggle-indicator"></span>
            <n-icon class="theme-icon moon" :class="{ active: theme.isDark }" :component="Moon" />
            <n-icon class="theme-icon sun" :class="{ active: !theme.isDark }" :component="Sun" />
          </span>
        </button>

        <n-dropdown
          v-if="auth.isAuthenticated"
          trigger="click"
          :options="accountOptions"
          @select="selectAccountAction"
        >
          <n-button class="account-button" secondary size="small" :aria-label="t('common.account')">
            <template #icon>
              <n-avatar
                v-if="accountAvatarUrl"
                class="account-avatar"
                round
                :size="22"
                :src="accountAvatarUrl"
                :data-avatar-src="accountAvatarUrl"
              />
              <n-avatar v-else class="account-avatar" round :size="22" data-avatar-src="">
                {{ accountInitial }}
              </n-avatar>
            </template>
            <span class="account-name">{{ accountLabel }}</span>
            <n-icon class="account-chevron" :component="ChevronDown" />
          </n-button>
        </n-dropdown>

        <LanguageSwitcher />
        <button class="menu-button" type="button" :aria-label="t('home.menuAria')" @click="navOpen = !navOpen">
          <n-icon :component="Menu2" />
        </button>
      </div>
    </div>
  </header>

  <AccountCenterModal v-model:show="accountCenterOpen" />
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  border-bottom: 1px solid color-mix(in srgb, var(--sw-border) 64%, transparent);
  background: var(--sw-surface-glass);
  color: var(--sw-text);
  user-select: none;
  box-shadow: 0 8px 28px rgba(28, 48, 58, 0.045);
  backdrop-filter: blur(20px);
}

.app-header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: min(1120px, calc(100% - 48px));
  min-height: 66px;
  margin: 0 auto;
}

.app-brand {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  color: var(--sw-text);
  font-size: 14px;
  font-weight: 800;
}

.app-brand span:not(.header-skeleton) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-header-controls {
  display: flex;
  align-items: center;
  gap: 9px;
}

.app-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.app-nav-link {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  border-radius: 8px;
  padding: 0 10px;
  color: var(--sw-muted);
  font-size: 12px;
  font-weight: 800;
  transition:
    background-color var(--sw-motion-standard),
    color var(--sw-motion-standard),
    transform var(--sw-motion-standard);
}

.app-nav-link:hover {
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  transform: translate3d(0, -1px, 0);
}

.app-nav-link.active {
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  cursor: default;
}

.theme-toggle {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 64px;
  height: 32px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--sw-faint);
  cursor: pointer;
  padding: 0;
}

.theme-toggle-track {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  width: 100%;
  height: 100%;
  border: 1px solid color-mix(in srgb, var(--sw-border) 84%, transparent);
  border-radius: 999px;
  background: var(--sw-surface-glass);
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, var(--sw-panel-bg-hover) 38%, transparent),
    var(--sw-shadow-soft);
  padding: 0 9px;
  overflow: hidden;
}

.theme-toggle-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 24px;
  height: 22px;
  border-radius: 50%;
  border: 1px solid color-mix(in srgb, var(--sw-accent) 38%, var(--sw-border));
  background: color-mix(in srgb, var(--sw-accent-soft) 72%, var(--sw-surface-solid));
  box-shadow:
    0 8px 18px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 color-mix(in srgb, var(--sw-panel-bg-hover) 65%, transparent);
  transition:
    transform var(--sw-motion-standard),
    background-color var(--sw-motion-standard),
    border-color var(--sw-motion-standard);
}

.theme-toggle.dark .theme-toggle-indicator {
  transform: translateX(31px);
}

.theme-icon {
  position: relative;
  z-index: 1;
  font-size: 15px;
  opacity: 0.58;
  transition:
    color var(--sw-motion-standard),
    opacity var(--sw-motion-standard);
}

.theme-icon.active {
  color: var(--sw-text);
  opacity: 1;
}

.theme-icon.moon {
  justify-self: start;
}

.theme-icon.sun {
  justify-self: end;
}

.account-button {
  --n-border-radius: 8px !important;
  max-width: 176px;
  border-color: var(--sw-border) !important;
  background: var(--sw-surface-glass) !important;
  color: var(--sw-muted) !important;
  font-weight: 700;
  backdrop-filter: blur(16px);
}

.account-button :deep(.n-button__content) {
  min-width: 0;
  gap: 7px;
}

.account-name {
  display: block;
  overflow: hidden;
  max-width: 112px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-chevron {
  flex: 0 0 auto;
  font-size: 14px;
}

.account-avatar {
  flex: 0 0 auto;
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  font-size: 11px;
  font-weight: 800;
}

.header-skeleton {
  display: block;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    rgba(190, 203, 213, 0.34) 0%,
    var(--sw-surface-solid) 48%,
    rgba(190, 203, 213, 0.34) 100%
  );
  background-size: 220% 100%;
  animation: header-skeleton 2.6s ease-in-out infinite;
}

.brand-skeleton {
  width: 136px;
  height: 16px;
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
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-glass);
  color: var(--sw-text);
  cursor: pointer;
  font-size: 22px;
  padding: 8px 10px;
}

@media (max-width: 900px) {
  .app-header-inner {
    width: min(100% - 32px, 560px);
    min-height: 64px;
  }

  .app-brand {
    max-width: 42vw;
  }

  .app-header-controls {
    gap: 8px;
  }

  .account-name {
    max-width: 24vw;
  }

  .menu-button {
    display: inline-flex;
  }

  .app-nav {
    position: absolute;
    top: 64px;
    right: 16px;
    left: 16px;
    z-index: 2;
    display: none;
    align-items: stretch;
    border: 1px solid var(--sw-border);
    border-radius: 8px;
    background: var(--sw-surface-glass);
    box-shadow: var(--sw-shadow-soft);
    padding: 12px;
    backdrop-filter: blur(18px);
  }

  .app-nav.open {
    display: grid;
    gap: 6px;
  }

  .app-nav-link {
    justify-content: center;
  }
}

@keyframes header-skeleton {
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
  .app-nav-link,
  .header-skeleton {
    animation: none;
    transition: none;
  }
}
</style>

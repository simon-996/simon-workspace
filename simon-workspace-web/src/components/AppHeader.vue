<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { NButton, NDropdown, NIcon } from 'naive-ui'
import { ChevronDown, Menu2, UserCircle } from '@vicons/tabler'

import LanguageSwitcher from './LanguageSwitcher.vue'
import { usePublicSiteConfig } from '../composables/usePublicSiteConfig'
import { useAuthStore } from '../stores/auth'
import { buildHeaderNavItems } from '../utils/appHeaderNav'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const navOpen = ref(false)
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
const accountOptions = computed(() => [
  {
    label: t('workspace.title'),
    key: 'workspace',
    disabled: route.path === '/workspace',
  },
  {
    label: t('workspace.logout'),
    key: 'logout',
  },
])

async function selectAccountAction(key: string | number) {
  const action = String(key)
  navOpen.value = false

  if (action === 'workspace') {
    if (route.path !== '/workspace') {
      await router.push('/workspace')
    }
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

        <n-dropdown
          v-if="auth.isAuthenticated"
          trigger="click"
          :options="accountOptions"
          @select="selectAccountAction"
        >
          <n-button class="account-button" secondary size="small" :aria-label="t('common.account')">
            <template #icon>
              <n-icon :component="UserCircle" />
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
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  border-bottom: 1px solid rgba(223, 231, 235, 0.72);
  background: rgba(251, 252, 252, 0.78);
  color: #17212b;
  backdrop-filter: blur(18px);
}

.app-header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  width: min(1120px, calc(100% - 48px));
  min-height: 74px;
  margin: 0 auto;
}

.app-brand {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  color: #17212b;
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
  gap: 12px;
}

.app-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.app-nav-link {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  border-radius: 8px;
  padding: 0 12px;
  color: #667583;
  font-size: 13px;
  font-weight: 800;
  transition:
    background-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.app-nav-link:hover {
  background: rgba(231, 244, 247, 0.74);
  color: #105c76;
  transform: translate3d(0, -1px, 0);
}

.app-nav-link.active {
  background: #e7f4f7;
  color: #105c76;
  cursor: default;
}

.account-button {
  --n-border-radius: 8px !important;
  max-width: 176px;
  border-color: rgba(223, 231, 235, 0.92) !important;
  background: rgba(255, 255, 255, 0.72) !important;
  color: #536773 !important;
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

.header-skeleton {
  display: block;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    rgba(190, 203, 213, 0.34) 0%,
    rgba(255, 255, 255, 0.86) 48%,
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  color: #17212b;
  cursor: pointer;
  font-size: 22px;
  padding: 8px 10px;
}

@media (max-width: 900px) {
  .app-header-inner {
    width: min(100% - 32px, 560px);
    min-height: 68px;
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
    top: 68px;
    right: 16px;
    left: 16px;
    z-index: 2;
    display: none;
    align-items: stretch;
    border: 1px solid #d8e0e7;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 16px 38px rgba(32, 53, 66, 0.08);
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

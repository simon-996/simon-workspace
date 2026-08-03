<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { NDrawer, NDrawerContent, NIcon } from 'naive-ui'
import { Menu2 } from '@vicons/tabler'

import AppHeader from '../components/AppHeader.vue'
// Workspace routes, including /workspace/posts, are owned by the shared navigation model.
import { buildWorkspaceNavigation } from '../navigation/workspaceNavigation'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const route = useRoute()
const auth = useAuthStore()

const moreOpen = ref(false)
const navigation = computed(() => buildWorkspaceNavigation((permission) => auth.hasPermission(permission)))
const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : 'workspace.title'
  return t(titleKey)
})

const isActive = (to: string) => {
  if (to === '/workspace') {
    return route.path === to
  }

  return route.path === to || route.path.startsWith(`${to}/`)
}

const moreIsActive = computed(() =>
  navigation.value.moreGroups.some((group) => group.items.some((item) => isActive(item.to))),
)

watch(
  () => route.fullPath,
  () => {
    moreOpen.value = false
  },
)
</script>

<template>
  <main class="workspace-shell">
    <AppHeader />

    <section class="workspace-body">
      <aside class="workspace-sider" :aria-label="t('workspace.aria')">
        <nav class="side-nav">
          <section v-for="group in navigation.groups" :key="group.key" class="nav-group">
            <h2 class="nav-group-label">{{ t(group.labelKey) }}</h2>
            <RouterLink
              v-for="item in group.items"
              :key="item.key"
              :to="item.to"
              class="nav-link"
              :class="{ 'nav-link-active': isActive(item.to) }"
              :aria-current="isActive(item.to) ? 'page' : undefined"
            >
              <n-icon :component="item.icon" />
              <span>{{ t(item.labelKey) }}</span>
            </RouterLink>
          </section>
        </nav>
      </aside>

      <section class="workspace-main">
        <header class="workspace-header">
          <div>
            <h1>{{ pageTitle }}</h1>
          </div>
        </header>

        <RouterView />
      </section>
    </section>

    <nav class="mobile-tabs" :aria-label="t('workspace.mobileAria')">
      <RouterLink
        v-for="item in navigation.mobileItems"
        :key="item.key"
        :to="item.to"
        class="mobile-tab"
        :class="{ 'mobile-tab-active': isActive(item.to) }"
        :aria-current="isActive(item.to) ? 'page' : undefined"
      >
        <n-icon :component="item.icon" />
        <span>{{ t(item.labelKey) }}</span>
      </RouterLink>

      <button
        v-if="navigation.moreGroups.length > 0"
        type="button"
        class="mobile-tab"
        :class="{ 'mobile-tab-active': moreIsActive }"
        :aria-expanded="moreOpen"
        aria-controls="workspace-more-navigation"
        @click="moreOpen = true"
      >
        <n-icon :component="Menu2" />
        <span>{{ t('workspace.nav.more') }}</span>
      </button>
    </nav>

    <n-drawer
      id="workspace-more-navigation"
      v-model:show="moreOpen"
      placement="bottom"
      height="min(72dvh, 560px)"
      class="workspace-more-drawer"
    >
      <n-drawer-content :title="t('workspace.nav.more')" closable>
        <div class="more-groups">
          <section v-for="group in navigation.moreGroups" :key="group.key" class="more-group">
            <h2 class="more-group-label">{{ t(group.labelKey) }}</h2>
            <nav class="more-links" :aria-label="t(group.labelKey)">
              <RouterLink
                v-for="item in group.items"
                :key="item.key"
                :to="item.to"
                class="more-link"
                :class="{ 'more-link-active': isActive(item.to) }"
                :aria-current="isActive(item.to) ? 'page' : undefined"
              >
                <n-icon :component="item.icon" />
                <span>{{ t(item.labelKey) }}</span>
              </RouterLink>
            </nav>
          </section>
        </div>
      </n-drawer-content>
    </n-drawer>
  </main>
</template>

<style scoped>
.workspace-shell {
  min-height: 100dvh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
}

.workspace-body {
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  min-height: calc(100dvh - 74px);
}

.workspace-sider {
  position: sticky;
  top: 74px;
  align-self: start;
  display: grid;
  min-height: calc(100dvh - 74px);
  border-right: 1px solid var(--sw-border);
  background: var(--sw-panel-bg);
  color: var(--sw-text);
  backdrop-filter: blur(24px);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 10px;
}

.nav-group {
  display: grid;
  gap: 3px;
}

.nav-group-label,
.more-group-label {
  margin: 0;
  color: var(--sw-muted);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  line-height: 1.4;
  text-transform: uppercase;
}

.nav-group-label {
  padding: 0 12px 5px;
}

.nav-link {
  display: grid;
  grid-template-columns: 24px 1fr;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  border-radius: 8px;
  padding: 0 12px;
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 700;
  transition:
    background-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-link .n-icon {
  font-size: 20px;
}

.nav-link-active,
.nav-link:hover {
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
  transform: translate3d(2px, 0, 0);
}

.workspace-main {
  min-width: 0;
  padding: 26px clamp(18px, 3vw, 34px) 34px;
}

.workspace-header {
  position: sticky;
  top: 86px;
  z-index: 8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 64px;
  margin: -10px 0 22px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  box-shadow: var(--sw-shadow-soft);
  padding: 12px 14px 12px 18px;
  backdrop-filter: blur(20px);
}

h1 {
  margin: 0;
  color: var(--sw-text);
  font-size: clamp(22px, 2.2vw, 30px);
  font-weight: 700;
  line-height: 1.2;
}

.mobile-tabs {
  display: none;
}

.more-groups {
  display: grid;
  gap: 20px;
  padding-bottom: 16px;
}

.more-group {
  display: grid;
  gap: 8px;
}

.more-group-label {
  padding: 0 2px;
}

.more-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
}

.more-link {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-height: 48px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  padding: 0 12px;
  color: var(--sw-muted);
  font-size: 13px;
  font-weight: 700;
  transition:
    border-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    color 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.more-link .n-icon {
  font-size: 20px;
}

.more-link:hover,
.more-link-active {
  border-color: var(--sw-accent);
  background: var(--sw-accent-soft);
  color: var(--sw-accent);
}

@media (max-width: 860px) {
  .workspace-shell {
    padding-bottom: calc(80px + env(safe-area-inset-bottom));
  }

  .workspace-body {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .workspace-sider {
    display: none;
  }

  .workspace-main {
    padding: 14px 12px 22px;
  }

  .workspace-header {
    align-items: flex-start;
    flex-direction: column;
    top: 76px;
    margin-top: 0;
    padding: 14px;
  }

  h1 {
    font-size: 24px;
  }

  .mobile-tabs {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 20;
    display: flex;
    min-height: 64px;
    padding-bottom: env(safe-area-inset-bottom);
    border-top: 1px solid var(--sw-border);
    background: var(--sw-panel-bg-strong);
    backdrop-filter: blur(16px);
  }

  .mobile-tab {
    display: grid;
    flex: 1 1 0;
    place-items: center;
    align-content: center;
    gap: 3px;
    min-width: 0;
    min-height: 64px;
    border: 0;
    background: transparent;
    padding: 6px 3px;
    color: var(--sw-muted);
    cursor: pointer;
    font-family: inherit;
    font-size: 11px;
    font-weight: 700;
    line-height: 1.15;
  }

  .mobile-tab .n-icon {
    font-size: 20px;
  }

  .mobile-tab span {
    max-width: 100%;
    overflow: hidden;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mobile-tab-active {
    color: var(--sw-accent);
  }
}
</style>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { NButton, NIcon } from 'naive-ui'
import {
  Book,
  Calendar,
  CircleCheck,
  FileText,
  Files,
  History,
  Logout,
  Settings,
  Template,
  UserCircle,
  Users,
} from '@vicons/tabler'

import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { to: '/workspace', labelKey: 'workspace.nav.overview', icon: CircleCheck, permission: 'workspace:view' },
  { to: '/workspace/courses', labelKey: 'workspace.nav.courses', icon: Book, permission: 'course:manage' },
  { to: '/workspace/classes', labelKey: 'workspace.nav.classes', icon: FileText, permission: 'class:manage' },
  { to: '/workspace/semesters', labelKey: 'workspace.nav.semesters', icon: Calendar, permission: 'semester:manage' },
  { to: '/workspace/templates', labelKey: 'workspace.nav.templates', icon: Template, permission: 'template:manage' },
  { to: '/workspace/files', labelKey: 'workspace.nav.files', icon: Files, permission: 'file:manage' },
  { to: '/workspace/history', labelKey: 'workspace.nav.history', icon: History, permission: 'generation:history' },
  { to: '/workspace/security', labelKey: 'workspace.nav.security', icon: Users, permission: 'user:manage' },
  { to: '/workspace/site', labelKey: 'workspace.nav.site', icon: Settings, permission: 'site:config' },
]

const visibleNavItems = computed(() => navItems.filter((item) => auth.hasPermission(item.permission)))
const pageTitle = computed(() => {
  const titleKey = typeof route.meta.titleKey === 'string' ? route.meta.titleKey : 'workspace.title'
  return t(titleKey)
})

async function logout() {
  await auth.logout()
  await router.replace('/login')
}
</script>

<template>
  <main class="workspace-shell">
    <aside class="workspace-sider" :aria-label="t('workspace.aria')">
      <RouterLink class="side-brand" to="/workspace">
        <span class="brand-mark">S</span>
        <span>Simon</span>
      </RouterLink>

      <nav class="side-nav">
        <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to" class="nav-link">
          <n-icon :component="item.icon" />
          <span>{{ t(item.labelKey) }}</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="workspace-main">
      <header class="workspace-header">
        <div>
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="user-panel">
          <LanguageSwitcher />
          <span class="user-name">
            <n-icon :component="UserCircle" />
            {{ auth.displayName }}
          </span>
          <n-button tertiary size="small" class="logout-button" @click="logout">
            <template #icon>
              <n-icon :component="Logout" />
            </template>
            {{ t('workspace.logout') }}
          </n-button>
        </div>
      </header>

      <RouterView />
    </section>

    <nav class="mobile-tabs" :aria-label="t('workspace.mobileAria')">
      <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to">
        <n-icon :component="item.icon" />
        <span>{{ t(item.labelKey) }}</span>
      </RouterLink>
    </nav>
  </main>
</template>

<style scoped>
.workspace-shell {
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  min-height: 100dvh;
  background:
    radial-gradient(circle at 88% 8%, rgba(22, 112, 143, 0.08), transparent 28%),
    linear-gradient(180deg, #fbfcfc 0%, #f7f8f8 48%, #eef3f5 100%);
  color: #17212b;
}

.workspace-sider {
  position: sticky;
  top: 0;
  align-self: start;
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100dvh;
  border-right: 1px solid rgba(223, 231, 235, 0.9);
  background: rgba(255, 255, 255, 0.72);
  color: #17212b;
  backdrop-filter: blur(24px);
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 78px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(223, 231, 235, 0.86);
  color: #17212b;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0;
}

.brand-mark {
  display: inline-grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #dfe7eb;
  background: #f7fbfc;
  color: #16708f;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 14px;
  font-weight: 800;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 14px 10px;
}

.nav-link {
  display: grid;
  grid-template-columns: 24px 1fr;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  border-radius: 8px;
  padding: 0 12px;
  color: #667783;
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

.nav-link.router-link-exact-active,
.nav-link:hover {
  background: #e7f4f7;
  color: #105c76;
  transform: translate3d(2px, 0, 0);
}

.workspace-main {
  min-width: 0;
  padding: 26px clamp(18px, 3vw, 34px) 34px;
}

.workspace-header {
  position: sticky;
  top: 0;
  z-index: 8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 64px;
  margin: -10px 0 22px;
  border: 1px solid rgba(223, 231, 235, 0.86);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 12px 36px rgba(32, 53, 66, 0.05);
  padding: 12px 14px 12px 18px;
  backdrop-filter: blur(20px);
}

h1 {
  margin: 0;
  color: #17212b;
  font-size: clamp(22px, 2.2vw, 30px);
  font-weight: 700;
  line-height: 1.2;
}

.user-panel {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.user-name {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  min-height: 34px;
  color: #536773;
  font-size: 13px;
  font-weight: 700;
}

.user-name .n-icon {
  color: #16708f;
  font-size: 20px;
}

.logout-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.mobile-tabs {
  display: none;
}

@media (max-width: 860px) {
  .workspace-shell {
    grid-template-columns: 1fr;
    padding-bottom: 70px;
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
    margin-top: 0;
    padding: 14px;
  }

  h1 {
    font-size: 24px;
  }

  .user-panel {
    width: 100%;
    justify-content: space-between;
  }

  .mobile-tabs {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 20;
    display: flex;
    min-height: 64px;
    border-top: 1px solid #dfe7eb;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(16px);
    overflow-x: auto;
  }

  .mobile-tabs a {
    display: grid;
    flex: 0 0 58px;
    place-items: center;
    align-content: center;
    gap: 3px;
    min-width: 0;
    color: #637783;
    font-size: 11px;
    font-weight: 700;
  }

  .mobile-tabs .n-icon {
    font-size: 20px;
  }

  .mobile-tabs a.router-link-exact-active {
    color: #16708f;
  }
}
</style>

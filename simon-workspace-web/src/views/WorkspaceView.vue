<script setup lang="ts">
import { computed } from 'vue'
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

import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { to: '/workspace', label: '总览', icon: CircleCheck, permission: 'workspace:view' },
  { to: '/workspace/courses', label: '课程', icon: Book, permission: 'course:manage' },
  { to: '/workspace/classes', label: '班级', icon: FileText, permission: 'class:manage' },
  { to: '/workspace/semesters', label: '学期', icon: Calendar, permission: 'semester:manage' },
  { to: '/workspace/templates', label: '模板', icon: Template, permission: 'template:manage' },
  { to: '/workspace/files', label: '文件', icon: Files, permission: 'file:manage' },
  { to: '/workspace/history', label: '记录', icon: History, permission: 'generation:history' },
  { to: '/workspace/security', label: '权限', icon: Users, permission: 'user:manage' },
  { to: '/workspace/site', label: '站点', icon: Settings, permission: 'site:config' },
]

const visibleNavItems = computed(() => navItems.filter((item) => auth.hasPermission(item.permission)))
const pageTitle = computed(() => String(route.meta.title ?? '工作台'))

async function logout() {
  await auth.logout()
  await router.replace('/login')
}
</script>

<template>
  <main class="workspace-shell">
    <aside class="workspace-sider" aria-label="工作台导航">
      <RouterLink class="side-brand" to="/workspace">
        <span class="brand-mark">S</span>
        <span>Simon Workspace</span>
      </RouterLink>

      <nav class="side-nav">
        <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to" class="nav-link">
          <n-icon :component="item.icon" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="workspace-main">
      <header class="workspace-header">
        <div>
          <p class="header-kicker">Workspace</p>
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="user-panel">
          <span class="user-name">
            <n-icon :component="UserCircle" />
            {{ auth.displayName }}
          </span>
          <n-button tertiary size="small" class="logout-button" @click="logout">
            <template #icon>
              <n-icon :component="Logout" />
            </template>
            退出
          </n-button>
        </div>
      </header>

      <RouterView />
    </section>

    <nav class="mobile-tabs" aria-label="移动端导航">
      <RouterLink v-for="item in visibleNavItems" :key="item.to" :to="item.to">
        <n-icon :component="item.icon" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>
  </main>
</template>

<style scoped>
.workspace-shell {
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  min-height: 100dvh;
  background: #f3f6f8;
  color: #14202b;
}

.workspace-sider {
  position: sticky;
  top: 0;
  align-self: start;
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100dvh;
  border-right: 1px solid #d8e0e7;
  background: #111a23;
  color: #d8e5ef;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 72px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(216, 229, 239, 0.12);
  color: #f7fbff;
  font-size: 15px;
  font-weight: 800;
}

.brand-mark {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #20a4d8;
  color: #ffffff;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 12px;
}

.nav-link {
  display: grid;
  grid-template-columns: 24px 1fr;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  border-radius: 8px;
  padding: 0 12px;
  color: #91a5b5;
  font-size: 14px;
  font-weight: 700;
}

.nav-link .n-icon {
  font-size: 20px;
}

.nav-link.router-link-exact-active,
.nav-link:hover {
  background: rgba(32, 164, 216, 0.14);
  color: #47c2f3;
}

.workspace-main {
  min-width: 0;
  padding: 28px;
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 72px;
  margin-bottom: 22px;
  border-bottom: 1px solid #d8e0e7;
  padding-bottom: 18px;
}

.header-kicker {
  margin: 0 0 6px;
  color: #5f7284;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #111a23;
  font-size: 30px;
  font-weight: 800;
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
  color: #465968;
  font-size: 14px;
  font-weight: 700;
}

.user-name .n-icon {
  color: #1688b9;
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
    padding: 20px 14px;
  }

  .workspace-header {
    align-items: flex-start;
    flex-direction: column;
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
    border-top: 1px solid #d8e0e7;
    background: rgba(255, 255, 255, 0.96);
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
    color: #637687;
    font-size: 11px;
    font-weight: 700;
  }

  .mobile-tabs .n-icon {
    font-size: 20px;
  }

  .mobile-tabs a.router-link-exact-active {
    color: #1688b9;
  }
}
</style>

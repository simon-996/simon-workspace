import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import WorkspaceView from '../views/WorkspaceView.vue'
import { useAuthStore } from '../stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/workspace',
      name: 'workspace',
      component: WorkspaceView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth) {
    const authenticated = await auth.restore()
    if (!authenticated) {
      return {
        name: 'login',
        query: {
          redirect: to.fullPath,
        },
      }
    }
  }

  if (to.name === 'login' && await auth.restore()) {
    return { name: 'workspace' }
  }
})
